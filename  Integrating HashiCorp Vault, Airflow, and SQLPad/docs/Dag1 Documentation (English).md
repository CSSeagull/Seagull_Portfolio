# Documentation: Integrating HashiCorp Vault, Airflow, and SQLPad

## 1. Architecture & Data Flow

**Goal:**  
Secrets (database connection details) are stored in Vault (KV v2). Airflow syncs them into SQLPad (MariaDB backend) only if there have been changes, and SQLPad itself uses these connections.

---

## 2. Vault Login via Token

### 2.1. Token Generation
Usually the Vault administrator or the user creates a token via CLI:
```sh
vault login <root_token>
vault token create -policy="sqlpad-policy" -ttl=72h
```
- Here, `sqlpad-policy` is the name of the policy with the required permissions (see section 3).

### 2.2. Using the Token in Code

Token is passed via environment variable or directly in code:
```python
VAULT_URL = os.getenv("VAULT_URL", "http://127.0.0.1:8200")
VAULT_TOKEN = os.getenv("VAULT_TOKEN", "hvs.ssGXxxxxxxx")
vault_client = hvac.Client(url=VAULT_URL, token=VAULT_TOKEN)
if not vault_client.is_authenticated():
    raise Exception("Vault authentication failed!")
```
**IMPORTANT:** The token must have permissions described in your policy.

---

## 3. Permission Setup

### 3.1. Example Policy Code

```hcl
path "secret/data/sqlpad-connections/*" {
  capabilities = ["create", "read", "update", "list"]
}
path "secret/data/sqlpad-encryption" {
  capabilities = ["read"]
}
path "secret/metadata/sqlpad-connections/*" {
  capabilities = ["list"]
}
path "secret/metadata/sqlpad-encryption" {
  capabilities = ["list"]
}
# For Airflow
path "airflow/*" {
  capabilities = ["read", "create", "update", "delete", "list"]
}
# For Vault UI
path "sys/internal/ui/mounts" {
  capabilities = ["read"]
}
```

- **create/read/update/list** — grant both read and write access to secrets.
- If access is only for Airflow, restrict by path and capabilities.

### 3.2. Applying Policy to Token

Via CLI:
```sh
vault policy write sqlpad-policy sqlpad-policy.hcl
vault token create -policy="sqlpad-policy"
```

---

## 4. Vault Settings

### 4.1. KV Secret Engine v2

- Mounted at `secret/` (or another mount_point).
- Connection details stored at:  
  `secret/data/sqlpad-connections/<connection_id>`

### 4.2. Example Secret Structure

```json
{
  "name": "MariaDB Demo",
  "driver": "mysql",
  "host": "127.0.0.1",
  "port": 3306,
  "database": "sqlpad2",
  "username": "sqlpad_user",
  "password": "admin",
  "multi_statement_transaction_enabled": true,
  "idle_timeout_seconds": 300
}
```

---

## 5. Airflow Configuration for Vault Usage

In the `[secrets]` section of `airflow.cfg`:

```ini
[secrets]
backend = airflow.providers.hashicorp.secrets.vault.VaultBackend
backend_kwargs = {"url": "http://127.0.0.1:8200", "token": "hvs.ssGXfxxxxxx", "mount_point": "secret", "kv_engine_version": 2}
```

---

## 6. Airflow DAG: Sync Vault → SQLPad

**vault_to_sqlpad_single_task.py:**

```python
from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta
import mysql.connector
import json
import os
import hashlib
import hvac

# Constants & Settings
DB_CONFIG = {
    'host': '127.0.0.1',
    'user': 'sqlpad_user',
    'password': 'admin',
    'database': 'sqlpad2',
}

VAULT_URL = os.getenv("VAULT_URL", "http://127.0.0.1:8200")
VAULT_TOKEN = os.getenv("VAULT_TOKEN", "hvs.ssGXfxxxxxxx")
KV_MOUNT_POINT = "secret"
VAULT_CONNECTIONS_PATH = "sqlpad-connections"

default_args = {
    'owner': 'airflow',
    'email_on_failure': True,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

vault_client = hvac.Client(url=VAULT_URL, token=VAULT_TOKEN)
if not vault_client.is_authenticated():
    raise Exception("Vault authentication failed!")

def get_vault_connection_keys():
    try:
        response = vault_client.secrets.kv.v2.list_secrets(
            path=VAULT_CONNECTIONS_PATH,
            mount_point=KV_MOUNT_POINT
        )
        return response['data']['keys']
    except hvac.exceptions.InvalidPath:
        return []

def get_current_vault_hash():
    """Generates a hash of all secrets contents to track changes."""
    keys = get_vault_connection_keys()
    data_combined = ''
    for key in sorted(keys):
        secret = vault_client.secrets.kv.v2.read_secret_version(
            path=f"{VAULT_CONNECTIONS_PATH}/{key}",
            mount_point=KV_MOUNT_POINT
        )
        data_str = json.dumps(secret['data']['data'], sort_keys=True)
        data_combined += data_str
    return hashlib.sha256(data_combined.encode()).hexdigest()

def bool_to_int(value):
    if isinstance(value, bool):
        return int(value)
    if isinstance(value, str):
        val = value.strip().lower()
        if val == "true":
            return 1
        elif val == "false":
            return 0
    if isinstance(value, int):
        return value
    return 0

def format_data_for_sqlpad(vault_data):
    return {
        "host": vault_data.get("host", "127.0.0.1"),
        "port": str(vault_data.get("port", "3306")),
        "database": vault_data.get("database", ""),
        "username": vault_data.get("username", ""),
        "password": vault_data.get("password", "")
    }

def sync_if_changed():
    # 1. Check sync_state table
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS sync_state (
            id INT PRIMARY KEY,
            last_hash VARCHAR(64)
        )
    """)
    cursor.execute("SELECT last_hash FROM sync_state WHERE id = 1")
    result = cursor.fetchone()
    current_hash = get_current_vault_hash()

    # 2. Compare hashes: sync or not
    if result is None:
        cursor.execute("INSERT INTO sync_state (id, last_hash) VALUES (1, %s)", (current_hash,))
        conn.commit()
        print("First-time sync: processing all Vault connections.")
    elif result['last_hash'] == current_hash:
        print("No changes in Vault. Skipping sync.")
        cursor.close()
        conn.close()
        return
    else:
        cursor.execute("UPDATE sync_state SET last_hash = %s WHERE id = 1", (current_hash,))
        conn.commit()
        print("Vault changes detected: syncing.")

    # 3. Get Vault keys and prepare for sync
    cursor = conn.cursor()
    keys = get_vault_connection_keys()

    synced = 0
    skipped = 0

    # 4. Process each Vault secret
    for key in keys:
        secret = vault_client.secrets.kv.v2.read_secret_version(
            path=f"{VAULT_CONNECTIONS_PATH}/{key}",
            mount_point=KV_MOUNT_POINT
        )
        vault_data = secret['data']['data']

        # 4.1 Skip if missing required field 'name'
        if not vault_data.get('name'):
            print(f"Skipping Vault key '{key}' — missing required field: 'name'")
            skipped += 1
            continue

        multi_stmt_enabled = bool_to_int(vault_data.get('multi_statement_transaction_enabled'))
        idle_timeout = vault_data.get('idle_timeout_seconds')

        formatted_data = format_data_for_sqlpad(vault_data)

        # 4.2 UPSERT (add/update connection)
        insert_query = """
            INSERT INTO connections (
                id, name, description, driver,
                multi_statement_transaction_enabled,
                idle_timeout_seconds, data,
                created_at, updated_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                description = VALUES(description),
                driver = VALUES(driver),
                multi_statement_transaction_enabled = VALUES(multi_statement_transaction_enabled),
                idle_timeout_seconds = VALUES(idle_timeout_seconds),
                data = VALUES(data),
                updated_at = NOW()
        """

        cursor.execute(insert_query, (
            key,
            vault_data.get('name'),
            vault_data.get('description'),
            vault_data.get('driver'),
            multi_stmt_enabled,
            idle_timeout,
            json.dumps(formatted_data)
        ))
        synced += 1

    conn.commit()
    cursor.close()
    conn.close()
    print(f"Synced {synced} connections. Skipped {skipped} due to missing 'name'.")

with DAG(
    dag_id='vault_to_sqlpad_single_task',
    default_args=default_args,
    description='Sync Vault connections to SQLPad only if changed, with properly formatted JSON and int booleans.',
    schedule_interval='@daily',
    start_date=datetime(2025, 6, 1),
    catchup=False,
    tags=['vault', 'sqlpad', 'encryption'],
) as dag:

    single_sync_task = PythonOperator(
        task_id='sync_vault_if_changed',
        python_callable=sync_if_changed
    )
```
---

### Detailed DAG Explanation:

- **Imports & Constants:**  
  Required libraries (`airflow`, `mysql.connector`, `hvac`, etc.) and connection settings are initialized.

- **vault_client:**  
  Vault client is created from URL and token. Auth check performed.

- **get_vault_connection_keys():**  
  Returns list of all connection ids in Vault under `sqlpad-connections`.

- **get_current_vault_hash():**  
  Generates a hash of all secrets (id → data) to detect any changes (add/update/delete connection).

- **bool_to_int():**  
  Converts boolean or "true"/"false" strings to 1 or 0, as required by MariaDB fields.

- **format_data_for_sqlpad():**  
  Creates a dictionary with only the needed fields for the `data` column in SQLPad's connections table.

- **sync_if_changed():**  
  Main DAG task:
  1. Creates (if not exists) the `sync_state` table and retrieves last saved hash.
  2. Compares Vault hash to the saved one. If equal, no sync needed; exit.
  3. Updates hash in `sync_state` if changes detected.
  4. Iterates through all Vault keys:
     - Gets secret.
     - Skips if missing `name` field.
     - Formats and coerces field types.
     - UPSERTs to SQLPad connections table.
  5. Saves changes and prints result (how many processed/skipped).

- **DAG & PythonOperator:**  
  DAG runs on schedule (`@daily`). The task uses the `sync_if_changed()` function.

---

## 7. How DAG Handles Connection Deletion

### 7.1. Overall Process

1. **DAG starts** (scheduled daily).
2. **Hashes all Vault secrets** at path `secret/sqlpad-connections/*`.
   - If hash matches previous, DAG ends (nothing changed).
   - If new hash, sync runs.
3. **For each secret:**
   - Reads data, formats for SQLPad (`data` is JSON of host, port, db, user, pwd).
   - Updates or inserts record to SQLPad’s connections table via UPSERT.
   - If Vault secret lacks `name`, it is skipped.
4. **Sync state (hash) is saved** in `sync_state` table to avoid unnecessary syncs.

### 7.2. Why It Doesn't Delete from Vault

- **DAG never deletes secrets from Vault** — for security and safety.
- If you manually delete a connection from MariaDB or via SQLPad, it will be restored at next sync if it remains in Vault.
- This ensures:
  - Protection from accidental deletion (Vault = source of truth).
  - Easy recovery: just rerun DAG if connection remains in Vault.

### 7.3. What if You Delete Connection in SQLPad?

- **Vault secret remains.**
- On next Airflow DAG sync, connection will be recreated in SQLPad.
- To delete “forever”, remove from both SQLPad and Vault.

### 7.4. Code Details

- **Hashing** avoids unnecessary DB operations (fast DAG, low DB load).
- **Formatting:** always coerces types as needed (strings, ints).
- **UPSERT** (`ON DUPLICATE KEY UPDATE`): updates if id exists, creates if not.
- **Skip logic:** missing required fields = skip, no error.

---

## 8. SQLPad: Configuration & Features

### 8.1. Environment Variables
(`../sqlpad/server/models/connections.js`)

```env
SQLPAD_PORT=3010
SQLPAD_BASE_URL="/sqlpad"
SQLPAD_DB_PATH=../db
SQLPAD_APP_LOG_LEVEL=debug
SQLPAD_WEB_LOG_LEVEL=debug
SQLPAD_SERVICE_TOKEN_SECRET=secr3t
SQLPAD_DEFAULT_CONNECTION_ID=mariadb
SQLPAD_BACKEND_DB_URI=mariadb://sqlpad_user:admin@127.0.0.1:3306/sqlpad2
SQLPAD_DB_IN_MEMORY=false
SQLPAD_SESSION_STORE=database
SQLPAD_QUERY_RESULT_STORE=database
# Example default connection
SQLPAD_CONNECTIONS__mariadb__name=MariaDB Demo
SQLPAD_CONNECTIONS__mariadb__driver=mysql
SQLPAD_CONNECTIONS__mariadb__host=127.0.0.1
SQLPAD_CONNECTIONS__mariadb__port=3306
SQLPAD_CONNECTIONS__mariadb__database=sqlpad2
SQLPAD_CONNECTIONS__mariadb__username=sqlpad_user
SQLPAD_CONNECTIONS__mariadb__password=admin
SQLPAD_AUTH_DISABLED="true"
SQLPAD_AUTH_DISABLED_DEFAULT_ROLE="admin"
SQLPAD_PASSPHRASE="At least the sensitive bits won't be plain text?"
```

### 8.2. Working with the `data` Column

Code modifications in SQLPad (`../sqlpad/server/models/connections.js`):
- Does not encrypt the `data` field; stores as JSON string.
- Decodes on read via `JSON.parse`.
- Can work without Cryptr if DB field is not encrypted.

Code example (fragment):
```javascript
decipherConnection(connection) {
  if (connection.data && typeof connection.data === 'string') {
    try {
      // Skip decryption; assume data is plain JSON
      connection.data = JSON.parse(connection.data);
    } catch (err) {
      console.warn('Failed to parse connection.data as JSON:', err);
    }
  }
  return connection;
}
```

---

## 9. UIs and Ports

- Airflow UI: usually `http://localhost:8080`
- SQLPad UI: `http://localhost:3010/sqlpad`
- Vault UI: `http://localhost:8200`

---

## 10. Example Vault Login and Permission Check Code

```python
import hvac
import os

VAULT_URL = os.getenv("VAULT_URL", "http://127.0.0.1:8200")
VAULT_TOKEN = os.getenv("VAULT_TOKEN", "hvs.xxxxxxxx")

vault_client = hvac.Client(url=VAULT_URL, token=VAULT_TOKEN)
if not vault_client.is_authenticated():
    raise Exception("Vault authentication failed!")

# Check access (list, read)
vault_client.secrets.kv.v2.list_secrets(path="sqlpad-connections", mount_point="secret")
secret = vault_client.secrets.kv.v2.read_secret_version(path="sqlpad-connections/mariadb", mount_point="secret")
```

---

## 11. How to Properly Delete a Connection

- **Delete from SQLPad:** just via UI or directly in DB.
- **Delete from Vault:** via Vault UI or CLI:
  ```sh
  vault kv delete secret/sqlpad-connections/mariadb
  ```
- After this, the next sync will NOT restore that connection in SQLPad.

---

## 12. Notes and Best Practices

- Do NOT store secrets in plain text for production.
- Use separate Vault tokens for Airflow and SQLPad with minimal privileges.
- All Vault changes are tracked via data hash, preventing unnecessary SQLPad DB load.

---

## 13. UI, API, and Connection Deletion/Edit Restrictions

### 13.1. How the "Delete" Button Works in UI (React)

In component `../client/src/connections/ConnectionList.tsx` (React):

- **"Delete" button** appears for all users, but for admins only if:
  - User has `admin` role
  - Connection has property `deletable: true`
- Server sets `deletable`:
  - For MariaDB connections: `deletable: true`
  - For Vault/config connections: `deletable: false`
- Delete call:
  - On click, calls `api.deleteConnection(item.id)`, then updates list via `mutate()`.
  - If server returns error, message is displayed.

```jsx
if (currentUser?.role === 'admin' && item.deletable) {
  actions.push(
    <DeleteConfirmButton
      key="delete"
      confirmMessage="Delete connection?"
      onConfirm={() => deleteConnection(item.id)}
      style={{ marginLeft: 8 }}
    >
      Delete
    </DeleteConfirmButton>
  );
}
```

**Note:**  
- For Vault/config connections, button does not appear—they cannot be deleted via UI, as Vault is the source of truth.

---

### 13.2. Why Add/Edit Connections via UI/API Is Impossible

In backend (Express router):

- **Add/edit via API always returns error:**
(`../server/routes/connections.js`)

  ```js
  async function createConnection(req, res) {
    return res.utils.error('Creating connections via API is disabled. Use config files or environment variables.');
  }
  async function updateConnection(req, res) {
    return res.utils.error('Editing connections via API is disabled. Use config files or environment variables.');
  }
  ```
- This prevents "desynchronization": all changes must go via Vault (or config), never via UI/REST.

---

### 13.3. How Server Determines What Can Be Deleted

In connections model (`../server/models/connections.js`):

- **findAll()** returns a list where:
  - For MariaDB connections: `deletable: true`, `editable: false`
  - For vault-config connections: `deletable: false`, `editable: false`
- UI respects these flags.

```javascript
const dbConnectionsJson = dbConnections.map((conn) => {
  const jsonConn = conn.toJSON();
  jsonConn.deletable = true;
  jsonConn.editable = false;
  return this.decorateConnection(jsonConn);
});
const vaultConnections = this.config.getConnections().map((conn) => {
  return this.decorateConnection({
    ...conn,
    deletable: false,
    editable: false,
  });
});
```

---

### 13.4. How Deletion Works

- **Only MariaDB connections can be deleted** (`deletable: true`).
- When deleted via UI/API, only record in MariaDB is removed.
- On next Airflow DAG sync, all connections still present in Vault will be restored.
- To delete forever, remove from both MariaDB and Vault.

---

### 13.5. Why Vault Connections Can't Be Deleted via UI

- Such connections are "immutable" for UI/REST API.
- Source of truth is Vault, not the DB.
- All changes only via Vault (CLI, Vault UI, Vault API).

---

### 13.6. Example Behavior and Best Practices

- To remove a connection:
  1. Delete it from Vault (e.g., `vault kv delete secret/sqlpad-connections/myconn`).
  2. Then delete from SQLPad via UI (if still shown).
  3. On next sync, it won't reappear.

- All Vault changes applied automatically at next DAG run.

---

### 13.7. Summary

- **UI fully obeys `deletable` and `editable` flags set by server.**
- **Add/edit via UI is forbidden to prevent desync with Vault.**
- **Delete allowed only for MariaDB connections, and only until next sync (if still in Vault, will be restored).**
- **For production, always manage connections via Vault and CI/CD (Airflow DAG). UI is for viewing and diagnostics only!**

---

## 14. Strict Control Over Add/Edit Connections (UI, API, Server)

### 14.1. Complete Removal of "Add" and "Edit" Buttons in UI

**Major UI change:**  
"Add connection" and "Edit" buttons are absent for all users, including admins:

- **Components removed:**  
  `ConnectionForm` and `ConnectionEditDrawer` completely removed from project.
- **Connection list component:**  
  No "Add"/"Edit" buttons; only "Delete" is rendered for allowed connections.
- **Button hiding via CSS/JS:**  
  - In `index.css`:
    ```css
    button._btn_13cvw_1._primary_13cvw_47[type="button"] {
      display: none !important;
    }
    ```
  - In `index.html`:
    ```html
    <script>
      function hideAddConnectionButton() {
        document.querySelectorAll('button._btn_13cvw_1._primary_13cvw_47').forEach(btn => {
          if (btn.textContent.trim() === 'Add connection') {
            btn.style.display = 'none';
          }
        });
      }
      document.addEventListener('DOMContentLoaded', hideAddConnectionButton);
      setInterval(hideAddConnectionButton, 1000);
    </script>
    ```
  - Ensures the button never appears, even if leftover code tries to render it.

---

### 14.2. API Blocking on Server

- **In Express router (`server/routes/connections.js`):**
  Any attempt to create/edit connection via API always returns error:
  ```js
  async function createConnection(req, res) {
    return res.utils.error('Creating connections via API is disabled. Use config files or environment variables.');
  }
  async function updateConnection(req, res) {
    return res.utils.error('Editing connections via API is disabled. Use config files or environment variables.');
  }
  ```

---

### 14.3. Blocking in Model Layer

- **Connections model (`server/models/connections.js`):**
  Any attempt to create/update connection via code throws exception:
  ```js
  async create(connection) {
    throw new Error(
      'Connection creation is disabled. Use Vault-based configuration.'
    );
  }
  async update(id, connection) {
    throw new Error(
      'Connection update is disabled. Use Vault-based configuration.'
    );
  }
  ```

---

### 14.4. Why Strictly Block UI & API

- **Single source of truth** — all connections must come only from Vault (and be synced via Airflow DAG).
- **Separation of responsibilities:**  
  - UI — only for viewing and deleting (in MariaDB, if allowed).
  - Vault — for managing and storing secrets.
- **Avoid desynchronization:**  
  - No creation/edit outside Vault, avoiding orphan or unmanaged connections.

---

### 14.5. How to Update Frontend for New Builds (Hash-based Assets)

- After each frontend build, Vite creates files with hashes (e.g., `index-b8f3ab8a.js`).  
  - Do NOT manually update links in `index.html`!
  - **Best practice:**  
    - Copy the entire fresh `build/` to the internal directory that serves static files:
      ```sh
      cp -R /Users/vladchaika/sqlpad/client/build/* /Users/vladchaika/sqlpad/server/public/
      ```
    - Ensures the latest JS/CSS is always used, no manual edits.
  - Express server should serve static files from this directory (`public/`).

---

### 14.6. Final Example of index.html for Server

```html
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SQLPad</title>
  <link rel="shortcut icon" href="/favicon.ico">
  <!-- tauCharts css must be in known path for image exports -->
  <link rel="stylesheet" href="/sqlpad/javascripts/vendor/tauCharts/tauCharts.min.css" type="text/css" />
  <link rel="stylesheet" href="/assets/index-97bc0071.css">
  <script type="module" crossorigin src="/assets/index-b8f3ab8a.js"></script>
</head>

<body class="sans-serif">
  <div id="root"></div>
  <script>
    function hideAddConnectionButton() {
      document.querySelectorAll('button._btn_13cvw_1._primary_13cvw_47').forEach(btn => {
        if (btn.textContent.trim() === 'Add connection') {
          btn.style.display = 'none';
        }
      });
    }
    document.addEventListener('DOMContentLoaded', hideAddConnectionButton);
    setInterval(hideAddConnectionButton, 1000);
  </script>
</body>
</html>
```

---

### 14.7. Summary

- **Adding and editing connections is fully blocked at UI and API level.**
- **Only MariaDB connections can be deleted, and only if not synced from Vault.**
- **All connection management is via Vault (and CI/CD).**
- **Frontend should always be fully updated by copying fresh build, to avoid hash file issues.**
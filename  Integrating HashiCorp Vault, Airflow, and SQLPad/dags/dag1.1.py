from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime, timedelta
import mysql.connector
import json
import os
import hashlib
import hvac

# Constants
DB_CONFIG = {
    'host': '127.0.0.1',
    'user': 'sqlpad_user',
    'password': 'admin',
    'database': 'sqlpad2',
}

VAULT_URL = os.getenv("VAULT_URL", "http://127.0.0.1:8200")
VAULT_TOKEN = os.getenv("VAULT_TOKEN", "hvs.ssGXxxxxxxxxx")
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
    """Generate a consistent hash of all connection data in Vault."""
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
    # Build the JSON structure expected by SqlPad in the 'data' column
    return {
        "host": vault_data.get("host", "127.0.0.1"),
        "port": str(vault_data.get("port", "3306")),
        "database": vault_data.get("database", ""),
        "username": vault_data.get("username", ""),
        "password": vault_data.get("password", "")
    }

def sync_if_changed():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)

    # Create state table if missing
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS sync_state (
            id INT PRIMARY KEY,
            last_hash VARCHAR(64)
        )
    """)

    cursor.execute("SELECT last_hash FROM sync_state WHERE id = 1")
    result = cursor.fetchone()
    current_hash = get_current_vault_hash()

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

    cursor = conn.cursor()
    keys = get_vault_connection_keys()

    synced = 0
    skipped = 0

    for key in keys:
        secret = vault_client.secrets.kv.v2.read_secret_version(
            path=f"{VAULT_CONNECTIONS_PATH}/{key}",
            mount_point=KV_MOUNT_POINT
        )
        vault_data = secret['data']['data']

        if not vault_data.get('name'):
            print(f"Skipping Vault key '{key}' — missing required field: 'name'")
            skipped += 1
            continue

        multi_stmt_enabled = bool_to_int(vault_data.get('multi_statement_transaction_enabled'))
        idle_timeout = vault_data.get('idle_timeout_seconds')

        # Format the 'data' JSON to match SqlPad expected format
        formatted_data = format_data_for_sqlpad(vault_data)

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
    print(f"✔ Synced {synced} connections. Skipped {skipped} due to missing 'name'.")

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

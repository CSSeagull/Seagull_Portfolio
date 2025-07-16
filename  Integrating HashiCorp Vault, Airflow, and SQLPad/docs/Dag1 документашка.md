# Документация: Интеграция HashiCorp Vault, Airflow и SQLPad

## 1. Архитектура и Поток Данных

**Цель:**  
Секреты (детали подключений к БД) хранятся в Vault (KV v2), Airflow синхронизирует их в SQLPad (MariaDB backend) только если были изменения, а сам SQLPad использует эти подключения.

---

## 2. Логин в Vault через Token

### 2.1. Генерация токена
Обычно администратор Vault или вы сами создаёте токен через CLI:
```sh
vault login <root_token>
vault token create -policy="sqlpad-policy" -ttl=72h
```
- Здесь `sqlpad-policy` — имя политики, которой вы дали нужные permissions (см. пункт 3).

### 2.2. Использование токена в коде

Токен передаётся через переменную окружения или прямо в коде:
```python
VAULT_URL = os.getenv("VAULT_URL", "http://127.0.0.1:8200")
VAULT_TOKEN = os.getenv("VAULT_TOKEN", "hvs.ssGXxxxxxxx")
vault_client = hvac.Client(url=VAULT_URL, token=VAULT_TOKEN)
if not vault_client.is_authenticated():
    raise Exception("Vault authentication failed!")
```
**ВАЖНО:** Токен должен иметь права, описанные вашей политикой.

---

## 3. Настройка permissions для доступа

### 3.1. Код политики (пример)

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
# Для Airflow
path "airflow/*" {
  capabilities = ["read", "create", "update", "delete", "list"]
}
# Для UI Vault
path "sys/internal/ui/mounts" {
  capabilities = ["read"]
}
```

- **create/read/update/list** — позволяют как читать, так и изменять секреты.
- Если даёте доступ только Airflow, ограничьте по пути и возможностям.

### 3.2. Применение политики к токену

В CLI:
```sh
vault policy write sqlpad-policy sqlpad-policy.hcl
vault token create -policy="sqlpad-policy"
```

---

## 4. Настройки Vault

### 4.1. KV-Secret Engine v2

- Монтируется на `secret/` (или другой mount_point).
- Данные подключений хранятся по пути:  
  `secret/data/sqlpad-connections/<connection_id>`

### 4.2. Пример структуры секрета

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

## 5. Конфиг Airflow для использования Vault

В `airflow.cfg` секция `[secrets]`:

```ini
[secrets]
backend = airflow.providers.hashicorp.secrets.vault.VaultBackend
backend_kwargs = {"url": "http://127.0.0.1:8200", "token": "hvs.ssGXfxxxxxx", "mount_point": "secret", "kv_engine_version": 2}
```

---

## 6. DAG Airflow: Синхронизация Vault → SQLPad

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

# Константы и настройки
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
    """Генерирует хэш всего содержимого секрета для отслеживания изменений."""
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
    # --- 1. Проверка состояния sync_state ---
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

    # --- 2. Сравнение хэша -- синхронизировать или нет?
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

    # --- 3. Получение списка ключей Vault и подготовка к синхронизации ---
    cursor = conn.cursor()
    keys = get_vault_connection_keys()

    synced = 0
    skipped = 0

    # --- 4. Обработка каждого секрета Vault ---
    for key in keys:
        secret = vault_client.secrets.kv.v2.read_secret_version(
            path=f"{VAULT_CONNECTIONS_PATH}/{key}",
            mount_point=KV_MOUNT_POINT
        )
        vault_data = secret['data']['data']

        # 4.1 Пропуск, если нет обязательного поля 'name'
        if not vault_data.get('name'):
            print(f"Skipping Vault key '{key}' — missing required field: 'name'")
            skipped += 1
            continue

        multi_stmt_enabled = bool_to_int(vault_data.get('multi_statement_transaction_enabled'))
        idle_timeout = vault_data.get('idle_timeout_seconds')

        formatted_data = format_data_for_sqlpad(vault_data)

        # 4.2 UPSERT (добавление/обновление подключения)
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

### Подробное пояснение частей DAG:

- **Импорт библиотек и настройка констант**  
  Импортируются необходимые библиотеки (`airflow`, `mysql.connector`, `hvac`, и др.).  
  В переменных задаются параметры подключения к БД, Vault, mount_point, путь к secret-ам, аргументы DAG.

- **vault_client**  
  Создаётся клиент для Vault на основе url и токена. Проверяется аутентификация.

- **get_vault_connection_keys()**  
  Функция возвращает список всех ключей (id подключений) в Vault в пути `sqlpad-connections`.

- **get_current_vault_hash()**  
  Генерирует общий хэш по всем секретам (id → данные), чтобы понять, были ли изменения (например, добавили/изменили/удалили коннект).

- **bool_to_int()**  
  Приводит значения типа bool или строку "true"/"false" к 1 или 0, как требуют поля MariaDB.

- **format_data_for_sqlpad()**  
  Собирает словарь только с нужными данными для поля `data` в таблице connections SQLPad.

- **sync_if_changed()**  
  Основная функция задачи DAG:
  1. **Создаёт (если нет) таблицу `sync_state`** и получает последний сохранённый хэш.
  2. **Сравнивает хэш Vault** с сохранённым. Если совпадает — синхронизация не нужна, выход.
  3. **Обновляет хэш** в `sync_state` если изменения есть.
  4. **Проходит по всем ключам Vault:**  
     - Получает секрет.
     - Пропускает, если нет поля `name`.
     - Форматирует и приводит типы полей.
     - Выполняет UPSERT в таблицу `connections` (добавляет или обновляет).
  5. **Сохраняет изменения и выводит результат** (сколько записей обработано/пропущено).

- **DAG и задача PythonOperator**  
  DAG запускается по расписанию (`@daily`). В качестве задачи используется функция `sync_if_changed()`.

---

## 7. Как работает DAG и что происходит при удалении connection

### 7.1. Общий процесс

1. **DAG стартует** (по расписанию каждый день).
2. **Хэшируется содержимое всех secrets Vault** по пути `secret/sqlpad-connections/*`.
   - Если хэш совпадает с предыдущим — DAG завершает работу (ничего не менялось).
   - Если хэш новый — запускается синхронизация.
3. **Для каждого секрета:**
   - Читает данные, форматирует их под SQLPad (`data` — это JSON с host, port, db, user, pwd).
   - Обновляет или добавляет запись в таблицу `connections` SQLPad (MariaDB) через UPSERT.
   - Если в vault нет поля `name`, secret пропускается.
4. **Состояние (hash) сохраняется** в таблице `sync_state`, чтобы избежать лишних синхронизаций.

### 7.2. Почему не удаляет из Vault?

- **DAG никогда не удаляет secrets из Vault** — это принцип безопасности и сохранности.
- Если вы вручную или через SQLPad удалите подключение из MariaDB, оно всё равно будет восстановлено при следующей синхронизации, если оно есть в Vault.
- Это обеспечивает:
  - Защиту от случайного удаления подключений (Vault — источник истины).
  - Удобный откат: можно восстановить подключение, просто запустив DAG, если оно осталось в Vault.

### 7.3. Что если удалить connection в SQLPad?

- **В Vault секрет останется**.
- При следующей синхронизации Airflow DAG снова создаст подключение в SQLPad.
- Для удаления “навсегда” — нужно удалить и в SQLPad, и в Vault.

### 7.4. Детали кода

- **Хэширование** защищает от лишних операций (DAG быстрый, не нагружает БД).
- **Форматирование**: всегда приводит типы к нужному формату (строки, ints).
- **UPSERT** (`ON DUPLICATE KEY UPDATE`): если такой id уже есть, обновляет, если нет — создает новую запись.
- **Логика пропуска**: если в секрете нет обязательных полей — не будет ошибки, просто пропуск.

---

## 8. SQLPad: Конфигурация и особенности

### 8.1. Переменные окружения
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
# Пример дефолтного подключенаия
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

### 8.2. Особенности работы с колонкой `data`

Модификации кода SQLPad (`../sqlpad/server/models/connections.js`):
- Не шифрует поле `data`, а сохраняет его как строку JSON.
- Декодирует при чтении через `JSON.parse`.
- Позволяет работать без Cryptr, если поле в базе не зашифровано.

Пример кода (фрагмент):
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

## 9. UI и порты

- Airflow UI: обычно `http://localhost:8080`
- SQLPad UI: `http://localhost:3010/sqlpad`
- Vault UI: `http://localhost:8200`

---

## 10. Пример кода логина и проверки permissions

```python
import hvac
import os

VAULT_URL = os.getenv("VAULT_URL", "http://127.0.0.1:8200")
VAULT_TOKEN = os.getenv("VAULT_TOKEN", "hvs.xxxxxxxx")

vault_client = hvac.Client(url=VAULT_URL, token=VAULT_TOKEN)
if not vault_client.is_authenticated():
    raise Exception("Vault authentication failed!")

# Проверка доступа (list, read)
vault_client.secrets.kv.v2.list_secrets(path="sqlpad-connections", mount_point="secret")
secret = vault_client.secrets.kv.v2.read_secret_version(path="sqlpad-connections/mariadb", mount_point="secret")
```

---

## 11. Как правильно удалять connection

- **Удалить из SQLPad:** просто через интерфейс или из БД.
- **Удалить из Vault:** через UI или CLI:
  ```sh
  vault kv delete secret/sqlpad-connections/mariadb
  ```
- После этого синхронизация не вернёт это подключение в SQLPad.

---

## 12. Примечания и лучшие практики

- Для production не храните секреты в виде plain text.
- Используйте отдельные токены Vault для Airflow и SQLPad с минимальными правами.
- Все изменения в Vault автоматически трекаются по хэшу данных, что предотвращает лишнюю нагрузку на базу SQLPad.

---

## 13. UI, API и ограничения по удалению/редактированию connections

### 13.1. Как работает кнопка удаления в UI (React)

В компоненте `../client/src/connections/ConnectionList.tsx` (React):

- **Кнопка "Delete"** появляется для всех клинетов, но и для админов только если:
  - Пользователь с ролью `admin`
  - Подключение имеет свойство `deletable: true`
- Свойство `deletable` расставляет сервер:
  - Для подключений из базы (`MariaDB`): `deletable: true`
  - Для подключений, пришедших из Vault/конфига: `deletable: false`
- Вызов удаления:
  - При нажатии вызывается `api.deleteConnection(item.id)`, после чего список обновляется через `mutate()`.
  - Если сервер вернул ошибку, на экране появится сообщение.

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

**Важно:**  
- Для подключений из Vault/конфига кнопка не появляется — они не удаляемы через интерфейс, так как источник истины — Vault.

---

### 13.2. Почему создание/редактирование connections через UI/API невозможно

В бэкенде (Express router):

- **Создание и редактирование через API всегда возвращают ошибку:**
(`../server/routes/connections.js`)

  ```js
  async function createConnection(req, res) {
    return res.utils.error('Creating connections via API is disabled. Use config files or environment variables.');
  }
  async function updateConnection(req, res) {
    return res.utils.error('Editing connections via API is disabled. Use config files or environment variables.');
  }
  ```
- Это защищает от "рассинхронизации": любые изменения должны идти только через Vault (или конфиг), а не через UI/REST.

---

### 13.3. Как сервер определяет, что можно удалять

В модели connections (`../server/models/connections.js`):

- **findAll()** возвращает список, где:
  - Для подключений из MariaDB: `deletable: true`, `editable: false`
  - Для vault-конфигов: `deletable: false`, `editable: false`
- UI ориентируется только на эти флаги.

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

### 13.4. Как работает удаление

- **Удалить можно только те подключения, что есть в MariaDB** (`deletable: true`).
- При удалении через UI или API, запись удаляется только из MariaDB.
- При следующей синхронизации Airflow DAG все подключения, которые по-прежнему есть в Vault, будут восстановлены.
- Чтобы удалить навсегда, нужно удалить и из MariaDB, и из Vault.

---

### 13.5. Почему нельзя удалить vault-коннекты через UI

- Такие подключения считаются "immutable" с точки зрения UI/REST API.
- Источник истины — Vault, не база.
- Любое изменение — только через Vault (CLI, UI Vault, API Vault).

---

### 13.6. Пример поведения и best practices

- Если вы хотите убрать подключение:
  1. Удалите его из Vault (например, `vault kv delete secret/sqlpad-connections/myconn`).
  2. После этого удалите из SQLPad через UI (если он ещё отображается).
  3. При следующей синхронизации оно не появится вновь.

- Все изменения в Vault применяются автоматически при следующем запуске DAG.

---

### 13.7. Резюме

- **UI полностью подчиняется флагам `deletable` и `editable`, выставляемым сервером.**
- **Создание и редактирование через UI запрещены, чтобы избежать рассинхронизации с Vault.**
- **Удаление разрешено только для тех connections, что из MariaDB, и только до следующей синхронизации (если они остались в Vault — появятся снова).**
- **Для production всегда управляйте подключениями только через Vault и CI/CD (DAG Airflow) — UI только для просмотра и диагностики!**

---

## 14. Жёсткое управление добавлением/редактированием connections (UI, API, сервер)

### 14.1. Полное отключение кнопок "Add" и "Edit" в UI

**Важное изменение интерфейса:**  
Кнопки "Add connection" и "Edit" отсутствуют для всех пользователей, включая админов:

- **Удалены компоненты:**  
  `ConnectionForm` и `ConnectionEditDrawer` полностью удалены из проекта.
- **Код компонента списка подключений:**  
  Нет кнопок "Add" и "Edit"; рендерится только "Delete" для доступных коннектов.
- **Скрытие кнопки через CSS и JS:**  
  - В `index.css`:
    ```css
    button._btn_13cvw_1._primary_13cvw_47[type="button"] {
      display: none !important;
    }
    ```
  - В `index.html`:
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
  - Это гарантирует, что даже если кнопка появится из‑за старого кода, она не будет видна.

---

### 14.2. Блокировка API на сервере

- **В маршрутизаторе Express (`server/routes/connections.js`):**
  Любая попытка создать или отредактировать подключение через API всегда возвращает ошибку:
  ```js
  async function createConnection(req, res) {
    return res.utils.error('Creating connections via API is disabled. Use config files or environment variables.');
  }
  async function updateConnection(req, res) {
    return res.utils.error('Editing connections via API is disabled. Use config files or environment variables.');
  }
  ```

---

### 14.3. Блокировка на уровне модели

- **Модель `Connections` (`server/models/connections.js`):**
  Любая попытка создать или обновить подключение через код вызовет исключение:
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

### 14.4. Почему так важно жёстко блокировать UI и API?

- **Единый источник истины** — все подключения должны поступать только через Vault (и синхронизироваться через Airflow DAG).
- **Разделение ответственности:**  
  - UI — только для просмотра и удаления (в MariaDB, если разрешено).
  - Vault — для управления и хранения секретов.
- **Исключение рассинхронизации:**  
  - Нельзя создать или отредактировать подключение мимо Vault, чтобы не появилось "висячих" или неуправляемых коннектов.

---

### 14.5. Как обновлять фронтенд для новых билдов (hash-based assets)

- После каждой сборки фронтенда Vite создает файлы с хэшем (например, `index-b8f3ab8a.js`).  
  - Не рекомендуется вручную менять ссылки в `index.html`!
  - **Лучший способ:**  
    - Копировать полностью свежий `build/` во внутренний каталог, откуда сервер раздаёт статику:
      ```sh
      cp -R /Users/vladchaika/sqlpad/client/build/* /Users/vladchaika/sqlpad/server/public/
      ```
    - Таким образом, всегда используются самые новые версии JS/CSS без ручных правок.
  - Сервер Express должен отдавать статику из этого каталога (`public/`).

---

### 14.6. Финальный пример index.html для сервера

```html
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SQLPad</title>
  <link rel="shortcut icon" href="/favicon.ico">
  <!-- tauCharts css must be в known path we can ref for image exports -->
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

### 14.7. Итог

- **Добавление и редактирование подключений полностью заблокированы на уровне UI и API.**
- **Удалять можно только MariaDB-коннекты, и только если они не синхронизируются из Vault.**
- **Все действия по управлению подключениями — только через Vault (и CI/CD).**
- **Фронтенд всегда следует обновлять целиком, копируя свежий build, чтобы не было проблем с хэшами файлов.**
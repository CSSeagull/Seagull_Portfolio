vault secrets enable -path=airflow -version=2 kv

vault kv put -mount=airflow variables/my_new_secret value=super_secret_123
vault kv put -mount=airflow connections/airflow_db conn_uri="postgresql+psycopg2://vladchaika@localhost:5432/airflow"


#For the variables
vault kv get -mount=airflow variables/my_new_secret

#For connections
vault kv get -mount=airflow connections/airflow_db




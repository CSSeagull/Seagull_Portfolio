vault auth enable approle

vault policy write atro_policy - <<EOF
path "airflow/*" {
     capabilities = ["create", "read", "update", "patch", "delete", "list"]
}

EOF

vault write auth/approle/role/astro_role \
    role_id=atro_role \
    secret_id_ttl=0 \
    secret_id_num_uses=0 \
    token_num_uses=0 \
    token_ttl=24h \
    token_max_ttl=24h \
    token_policies=astro_policy

vault write -f auth/approle/role/astro_role/secret-id

# Existing SQLPad permissions
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

# === Add this block for Airflow access ===
path "airflow/*" {
  capabilities = ["read", "create", "update", "delete", "list"]
}

# Optional: Airflow UI needs this for mount visibility
path "sys/internal/ui/mounts" {
  capabilities = ["read"]
}

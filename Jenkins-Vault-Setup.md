# Jenkins Agents Integration with HashiCorp Vault

This document provides a complete setup guide for integrating Jenkins agents with HashiCorp Vault for secret management. It includes Vault configuration, Jenkins host setup, and systemd service management.

---

## 1. Prerequisites

You must have the following:

* A **Kubernetes cluster** (kubeadm-based)
* **Vault** installed and running (HA or single-node)
* **Jenkins master (controller)**
* **Jenkins agents** (via SSH, Docker, or Kubernetes)

---

## 2. Setup Overview

1. Create a Vault AppRole for Jenkins with short TTLs and limited access.
2. Store the AppRole credentials (`RoleID`, `SecretID`) securely on the Jenkins host.
3. Configure and run `vault agent` as a systemd service on the Jenkins host.
4. Use the Vault Agent to auto-authenticate and render an environment file containing a short-lived token.
5. Configure Jenkins to use that environment file for Vault authentication.

### Security Guidelines

* Keep `role_id` and `secret_id` readable only by **vault** or **root**.
* Set strict permissions for Vault token sink and environment files.
* Use short TTLs and single-use `secret_id` if automation is available.
* Test all configurations in a staging environment before production.

---

## 3. Configure Vault for Jenkins

### A. Enable and Configure AppRole

Run these commands on the Vault server or an authenticated admin machine.

#### Enable AppRole Authentication

```bash
vault auth enable approle
```

#### Create a Jenkins Policy

```bash
cat > jenkins-policy.hcl <<'EOF'
path "secret/data/jenkins/*" {
  capabilities = ["read"]
}
EOF
vault policy write jenkins-policy jenkins-policy.hcl
```

#### Create the Jenkins AppRole

```bash
vault write auth/approle/role/jenkins-role \
  token_ttl=5h \
  token_max_ttl=24h \
  secret_id_ttl=2h \
  secret_id_num_uses=150 \
  token_num_uses=100 \
  token_type=service \
  policies=jenkins-policy
```

#### Retrieve AppRole Credentials

```bash
vault read -field=role_id auth/approle/role/jenkins-role/role-id > role_id.txt
vault write -f -format=json auth/approle/role/jenkins-role/secret-id | jq -r '.data.secret_id' > secret_id.txt
```

Keep these files secure.

---

## 4. Jenkins Host Setup

### A. Install Vault Agent

```bash
wget -O - https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(grep -oP '(?<=UBUNTU_CODENAME=).*' /etc/os-release || lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install vault -y
vault -v
```

### B. Create Vault Agent Directory and Secure Files

```bash
sudo mkdir -p /var/lib/vault-agent/
sudo chown -R vault:vault /var/lib/vault-agent/
sudo chmod 750 /var/lib/vault-agent

sudo cp /path/to/role_id.txt /var/lib/vault-agent/role_id
sudo cp /path/to/secret_id.txt /var/lib/vault-agent/secret_id

sudo chown vault:vault /var/lib/vault-agent/role_id /var/lib/vault-agent/secret_id
sudo chmod 600 /var/lib/vault-agent/role_id /var/lib/vault-agent/secret_id
```

---

## 5. Vault Agent Configuration

### A. Create Agent Configuration File

Path: `/etc/vault/agent.hcl`

```bash
pid_file = "/run/vault/vault-agent.pid"
listener "tcp" {
  address = "127.0.0.1:8250"
  tls_disable = true
}

auto_auth {
  method "approle" {
    mount_path = "auth/approle"
    config = {
      role_id_file_path = "/var/lib/vault-agent/role_id"
      secret_id_file_path = "/var/lib/vault-agent/secret_id"
      remove_secret_id_file_after_reading = false
    }
  }

  sink "file" {
    config = {
      path = "/var/lib/vault-agent/.vault-token"
      mode = 0644
    }
  }
}

template {
  source      = "/var/lib/vault-agent/templates/token_env.tpl"
  destination = "/var/lib/jenkins/vault/vault_env"
  perms       = "0640"
  command     = "systemctl try-restart jenkins.service"
}

vault {
  address = "https://172.26.44.182:8200"
  tls_skip_verify = true
}
```

### B. Create Template File

```bash
sudo tee /var/lib/vault-agent/templates/token_env.tpl > /dev/null <<'EOF'
VAULT_TOKEN={{ with secret "auth/token/lookup-self" }}{{ .Data.id }}{{ end }}
EOF
```

### C. Set Permissions

```bash
sudo chown vault:vault /etc/vault/agent.hcl
sudo chmod 640 /etc/vault/agent.hcl

sudo chown -R vault:vault /var/lib/vault-agent
sudo chmod 750 /var/lib/vault-agent
sudo chmod 750 /var/lib/vault-agent/templates
sudo chmod 640 /var/lib/vault-agent/templates/token_env.tpl

sudo mkdir -p /var/lib/jenkins/vault
sudo chown vault:jenkins /var/lib/jenkins/vault
sudo chmod 770 /var/lib/jenkins/vault
```

---

## 6. Configure Vault Agent as a Systemd Service

### Create Unit File

Path: `/etc/systemd/system/vault-agent.service`

```bash
sudo tee /etc/systemd/system/vault-agent.service > /dev/null <<'EOF'
[Unit]
Description=Vault Agent (auto-auth for Jenkins)
After=network.target

[Service]
Type=simple
ExecStart=/usr/bin/vault agent -config=/etc/vault/agent.hcl
PIDFile=/run/vault/vault-agent.pid
Restart=on-failure
User=vault
Group=vault
RuntimeDirectory=vault
RuntimeDirectoryMode=0755
ProtectSystem=full
ProtectHome=read-only

[Install]
WantedBy=multi-user.target
EOF
```

### Enable and Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now vault-agent.service
sudo journalctl -u vault-agent -f --no-pager
```

### Verify

Check token sink and environment file:

```bash
sudo ls -l /var/lib/vault-agent/.vault-token
sudo cat /var/lib/vault-agent/.vault-token
sudo cat /var/lib/jenkins/vault/vault_env
```

Ensure correct ownership:

```bash
sudo chown vault:jenkins /var/lib/jenkins/vault/vault_env
sudo chmod 770 /var/lib/jenkins/vault
```

The Vault Agent automatically renews tokens and re-renders the environment file. The Jenkins service restarts whenever the token updates.

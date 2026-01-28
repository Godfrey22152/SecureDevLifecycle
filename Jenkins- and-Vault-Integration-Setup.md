# Jenkins Agents Integration with HashiCorp Vault

This document provides a comprehensive guide for setting up HashiCorp Vault for secret management and integrating it with Jenkins agents. It covers Vault installation and initialization on a production server, Vault configuration for Jenkins, setup on the Jenkins host, and management of the Vault Agent as a systemd service.

---

## 1. Prerequisites

Before proceeding, ensure you have the following:

* An **Ubuntu-based server** for hosting Vault (production-ready, with sufficient resources for high availability if needed).
* A **Kubernetes cluster** (kubeadm-based) for Jenkins agents if using Kubernetes plugin.
* **Jenkins master (controller)** installed and operational.
* **Jenkins agents** configured (via SSH, Docker, or Kubernetes plugin).
* Administrative access (root or sudo) on all relevant hosts.
* Basic knowledge of Vault concepts, such as AppRoles, policies, and transit engines.
* A valid DNS record for the Vault server hostname (required for TLS certificate verification).
* Replace placeholders like `<hostname>`, `<loopbackIP>`, and `<host>` with your actual values throughout the guide.

**Note:** This guide assumes a single-node Vault setup for simplicity. For production, refer to HashiCorp's [production hardening guide](https://learn.hashicorp.com/tutorials/vault/production-hardening) for high availability (HA), Raft storage, and additional security measures.

---

## 2. Install and Initialize Vault Server in a Production-like manner 

Install Vault on your Ubuntu-based production server and configure it for secure operation. This includes adding the HashiCorp repository, installing the package, generating TLS certificates, configuring the server, and initializing Vault.

### A. Install Vault Server in Prod Mode

Visit the **[Vault official installation link](https://developer.hashicorp.com/vault/install#linux)**
Run the following commands to add the HashiCorp repository and install Vault:

```bash
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(grep -oP '(?<=UBUNTU_CODENAME=).*' /etc/os-release || lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install vault
```

Verify the installation:

```bash
vault -v
```

This installs Vault and creates a system user/group (`vault`) along with a default configuration file at **`/etc/vault/vault.hcl`**.

### B. Generate TLS Certificates

For production, use self-signed or CA-signed certificates. Here's how to generate a self-signed certificate:

```bash
sudo mkdir -p /opt/vault/{tls,data}
sudo chown -R vault:vault /opt/vault
cd /opt/vault/tls
openssl req -out tls.crt -new -keyout tls.key -newkey rsa:4096 -nodes -sha256 -x509 -subj "/O=HashiCorp/CN=Vault" -addext "subjectAltName = IP:<loopbackIP>,DNS:<host>" -days 3650
```

- Replace `<loopbackIP>` with `0.0.0.0` (or your server's loopback IP).
- Replace `<host>` with your Vault server's hostname (e.g., `vault.com`).

Set permissions:

```bash
sudo chown vault:vault /opt/vault/tls/*
sudo chmod 600 /opt/vault/tls/*
```

### C. Configure Vault Server

Edit or create the Vault configuration file at `/etc/vault/vault.hcl` with the following content (adjust as needed for your environment):

```hcl
# Full configuration options: https://www.vaultproject.io/docs/configuration

ui = true

storage "file" {
  path = "/opt/vault/data"
}

# HTTPS listener
listener "tcp" {
  address       = "0.0.0.0:8200"
  tls_cert_file = "/opt/vault/tls/tls.crt"
  tls_key_file  = "/opt/vault/tls/tls.key"
}
```

This enables the Vault UI, uses file-based storage (replace with Raft for HA), and configures TLS listening on all interfaces.

### D. Start Vault Service

Start and enable the Vault systemd service:

```bash
sudo systemctl start vault
sudo systemctl enable vault
sudo systemctl status vault
```

### E. Initialize Vault

Set environment variables for interacting with Vault:

```bash
export VAULT_ADDR='https://<hostname>:8200'
export VAULT_CACERT="/opt/vault/tls/tls.crt"
```

- Replace `<hostname>` with your Vault server's hostname (e.g., `vault.example.com`).

Initialize Vault to generate unseal keys and the root token:

```bash
vault operator init
```

- This outputs 5 unseal keys (by default) and a root token. Securely store these (e.g., in a password manager). You need at least 3 keys to unseal Vault.
- Alternatively, use the Vault UI at `https://<hostname>:8200` and enter 5 for key shares and 3 for the threshold.

### F. Unseal and Login to Vault

Unseal Vault by providing the required number of keys (repeat for each key):

```bash
vault operator unseal --ca-cert=/opt/vault/tls/tls.crt
```

Check Vault status:

```bash
vault status
```

Login with the root token:

```bash
vault login
```

Vault is now initialized and ready for configuration. For enhanced security, revoke the root token after setup and use admin policies instead.

**Note:** Ensure your DNS record for `<hostname>` is configured correctly, as TLS verification will fail otherwise. Refer to the [production hardening tutorial](https://learn.hashicorp.com/tutorials/vault/production-hardening) for additional steps like enabling audit logs, using Raft storage, and implementing mlock.

---

## 3. Setup Overview

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

## 4. Configure Vault for Jenkins

With Vault running, configure it specifically for Jenkins integration. Run these commands on the Vault server or an authenticated admin machine using the root token or an admin policy.

### A. Enable and Configure AppRole

Enable the AppRole authentication method:

```bash
vault auth enable approle
```

### B. Enable Transit Engine

The Transit engine is used for cryptographic operations, such as signing and verification with **[cosign](https://docs.sigstore.dev/cosign/signing/signing_with_containers/)**. The URI format for Hashicorp Vault KMS is: `hashivault://$keyname`. It requires standard Vault environment variables (`VAULT_ADDR`, `VAULT_TOKEN`) is set correctly.

#### 1. Enable the Transit Engine

```bash
vault secrets enable transit
```

Verify if it's already enabled:

```bash
vault secrets list
```

#### 2. Create a Signing Key

Create a key named `cosign`:

```bash
vault write -f transit/keys/cosign type=ecdsa-p256
```

#### 3. List and View Keys

```bash
# List all transit keys
vault list transit/keys

# Read the cosign key details
vault read transit/keys/cosign
```

You now have the key name: **cosign**.

### C. Create a Jenkins Policy

This policy grants access to KV secrets and the Transit engine for Cosign operations.

```bash
cat > jenkins-policy.hcl <<'EOF'
## KV secrets access
path "secret/data/jenkins/*" {
  capabilities = ["read"]
}

## Cosign Transit Engine Access
# Allow reading the transit key metadata and public key
path "transit/keys/cosign" {
  capabilities = ["read", "list"]
}

# Allow signing operations
path "transit/sign/cosign" {
  capabilities = ["create", "update"]
}

path "transit/sign/cosign/*" {
  capabilities = ["create", "update"]
}

# Allow verification operations
path "transit/verify/cosign" {
  capabilities = ["create", "update"]
}

path "transit/verify/cosign/*" {
  capabilities = ["create", "update"]
}

# Allow HMAC operations (optional)
path "transit/hmac/cosign" {
  capabilities = ["create", "update"]
}

path "transit/hmac/cosign/*" {
  capabilities = ["create", "update"]
}

# CRITICAL: Allow export of public key (required for cosign)
path "transit/export/signing-key/cosign" {
  capabilities = ["read"]
}

# Allow reading transit mount info
path "transit/" {
  capabilities = ["read", "list"]
}
EOF

vault policy write jenkins-policy jenkins-policy.hcl
```

### D. Create the Jenkins AppRole

Attach the policy to the AppRole:

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

### E. Retrieve AppRole Credentials

```bash
vault read -field=role_id auth/approle/role/jenkins-role/role-id > role_id.txt
vault write -f -format=json auth/approle/role/jenkins-role/secret-id | jq -r '.data.secret_id' > secret_id.txt
```

Secure these files (`role_id.txt` and `secret_id.txt`) and transfer them to the Jenkins host.

---

## 5. Vault Agent Setup on the Jenkins Server

On the Jenkins host, install the Vault binary (which includes the agent) and set up secure storage for credentials.

### A. Install Vault Agent

```bash
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
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

Replace `/path/to/` with the actual location of the credential files.

---

## 6. Vault Agent Configuration

Configure the Vault Agent to authenticate using AppRole and generate a token environment file for Jenkins.

### A. Create Agent Configuration File

Create `/etc/vault/agent.hcl`:

```hcl
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

Update the `vault.address` to match your Vault server's address.

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
sudo chown -R vault:jenkins /var/lib/jenkins/vault
sudo chmod 770 /var/lib/jenkins/vault
```

---

## 7. Configure Vault Agent as a Systemd Service

Run the Vault Agent as a background service for automatic token renewal.

### A. Create Unit File

Create `/etc/systemd/system/vault-agent.service`:

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

### B. Enable and Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now vault-agent.service
sudo journalctl -u vault-agent -f --no-pager
```

### C. Verify Setup

Check the token sink and environment file:

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

The Vault Agent will automatically renew tokens and re-render the environment file, triggering a Jenkins restart if needed.

---

## 8. Next Steps

- Integrate the `vault_env` file into Jenkins (e.g., source it in Jenkins jobs or use the Jenkins Vault plugin).
- Store secrets in Vault's KV engine under `secret/data/jenkins/*`.
- Test Cosign signing in Jenkins using the Transit key (e.g., `hashivault://cosign`).
- Monitor Vault and Agent logs for issues.
- For advanced setups, enable Vault HA, audit logging, and integrate with Kubernetes for dynamic secrets.

If you encounter issues, check Vault logs (`journalctl -u vault`) and refer to official documentation.

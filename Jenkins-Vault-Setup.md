# Jenkins Agents Integration with HashiCorp Vault

This guide explains how to configure Jenkins and its agents to use HashiCorp Vault for secret management. It covers Vault setup, Jenkins configuration, and best practices for integrating both securely.

---

## 1. Prerequisites

You need the following components already running:

* A **Kubernetes cluster** (kubeadm-based)
* **Vault** installed (HA or single-node)
* **Jenkins master** (controller)
* **Jenkins agents** (via SSH, Docker, or Kubernetes)

---

## 2. Configure Vault for Jenkins

### Enable AppRole Authentication

```bash
vault auth enable approle
```

### Create a Vault Policy for Jenkins

```bash
cat > jenkins-policy.hcl <<'EOF'
path "secret/data/jenkins/*" {
  capabilities = ["read"]
}
EOF
vault policy write jenkins-policy jenkins-policy.hcl
```

### Create AppRole Credentials
```bash
vault write auth/approle/role/jenkins-role \
  token_ttl=30m \
  token_max_ttl=60m \
  secret_id_ttl=60m \
  secret_id_num_uses=1 \
  token_num_uses=1 \
  policies=jenkins-policy
```
### Extract the AppRole ID at `auth/approle/role/jenkins-role/role-id` 

### Get the App RoleID `auth/approle/role/jenkins-role/role-id ` and create one SecretID from `auth/approle/role/jenkins-role/secret-id`

```
vault read -field=role_id auth/approle/role/jenkins-role/role-id > role_id.txt
vault write -f -format=json auth/approle/role/jenkins-role/secret-id | jq -r '.data.secret_id' > secret_id.txt
```
You now have two files stored at **`role_id.txt`** and **`secret_id.txt`**. Keep them safe.




### Enable Secrets
```bash
vault secrets enable -path=secret kv-v2
```
Vault creates a mount point called **`secret`**.
Inside it, every secret you store lives under:
```bash
secret/data/<your_secret_path>
```
### Store the SSH Private Key in Vault

You’ll store the Jenkins agent private key here.
```bash
vault kv put secret/jenkins/agent_key private_key="$(cat /home/jenkins/.ssh/private_key)" 
```


## 3. Store Secrets in Vault

Create secrets that Jenkins will use:

```bash
vault kv put secret/jenkins/docker username=docker_user password=docker_pass
vault kv put secret/jenkins/aws access_key=AKIAXXXX secret_key=xxxx
```

---

## 4. Configure Jenkins to Use Vault

### Install the Vault Plugin

1. Go to **Manage Jenkins → Plugins → Available**.
2. Install **HashiCorp Vault Plugin**.

### Add Vault Credentials

1. Navigate to **Manage Jenkins → Credentials → System → Global credentials**.
2. Add a new **Vault AppRole Credential**:

   * Role ID: `<your-role-id>`
   * Secret ID: `<your-secret-id>`
   * Vault URL: `http://<vault-loadbalancer-ip>:8200`
   * Skip TLS verification if Vault runs with `tlsDisable: true`.

### Configure Vault in Jenkins

1. Go to **Manage Jenkins → Configure System**.
2. Find the **Vault Plugin** section.
3. Enable **Use Vault Credential** and select your AppRole credential.
4. Test the connection.

---

## 5. Using Vault Secrets in Pipelines

Example Jenkinsfile:

```groovy
pipeline {
  agent any
  environment {
    DOCKER_USER = vault path: 'secret/data/jenkins/docker', key: 'username'
    DOCKER_PASS = vault path: 'secret/data/jenkins/docker', key: 'password'
  }
  stages {
    stage('Build') {
      steps {
        sh 'echo $DOCKER_USER'
        sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
      }
    }
  }
}
```

Jenkins fetches the secrets dynamically at runtime from Vault.

---

## 6. Jenkins Agents Integration

### Option A: Agents Use Vault Through Controller *(Recommended)*

The Jenkins controller fetches secrets from Vault and injects them into agent jobs during execution.

### Option B: Agents Authenticate Directly to Vault

Each agent connects to Vault using its own AppRole.

#### Example Setup for Agents

```bash
vault write auth/approle/login role_id=<role_id> secret_id=<secret_id> > token.json
export VAULT_TOKEN=$(jq -r .auth.client_token token.json)
vault kv get -field=password secret/jenkins/docker
```

Agents can fetch secrets directly for isolated or dynamic environments.

---

## 7. Optional: Auto-Unseal Vault with Kubernetes

To simplify restarts, configure Vault to auto-unseal using Kubernetes Secrets.

```yaml
server:
  extraEnvironmentVars:
    VAULT_UNSEAL_K8S_SECRET_NAME: vault-unseal
```

Vault will automatically unseal on pod restart.

---

## 8. Validation

Run:

```bash
kubectl get pods -n vault
kubectl logs <vault-pod>
```

Ensure Vault pods are running and unsealed.

In Jenkins, run a test pipeline to confirm secrets load from Vault.

---

## 9. Summary

This setup:

* Keeps secrets centralized in Vault
* Uses AppRole for secure Jenkins access
* Supports dynamic secret retrieval
* Reduces exposure of sensitive data in Jenkins

You can extend this with dynamic database credentials, AWS IAM secrets, or PKI certificates as needed.

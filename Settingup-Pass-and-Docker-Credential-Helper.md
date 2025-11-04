# Setting up `pass` and Docker Credential Helper on Jenkins Agent

This guide explains how to configure **`pass`** and **`docker-credential-helpers`** on a Jenkins agent server. This setup ensures Docker credentials are stored securely, avoiding the unencrypted password warning when running `docker login` in your Jenkins pipelines even though our credentials are securely stored and managed in vault through Jenkins. 
The Docker Engine can keep user credentials in an external credential store, such as the native keychain of the operating system. Using an external store is more secure than storing credentials in the Docker configuration file.

To use a credential store, you need an external helper program to interact with a specific keychain or external store. 

---

## Why You Need This

When you run `docker login` inside a Jenkins job, you often see this warning:

```
WARNING! Your password will be stored unencrypted in /home/$USER/.docker/config.json.
Configure a credential helper to remove this warning.
See https://docs.docker.com/engine/reference/commandline/login/#credential-stores
```
When you verify this further at **`/home/$USER/.docker/config.json`** you see: 

If Using the Github Container Registry:

```bash
{
        "auths": {
                "ghcr.io": {
                        "auth": "WW91ci1Vc2VybmFtZTpZb3VyLVBhc3N3b3JkLW9yLVRva2Vu"
                }
        }
}

```
Or if using DockerHub:

```bash
{
        "auths": {
                "https://index.docker.io/v1/": {
                        "auth": "WW91ciBQYXNzd29yZCBvciBQZXJzb25hbCBBY2Nlc3MgVG9rZW4="
                }
        }
}
```

This means Docker saves your authentication token in base64 encoded text which is not a best practice. To secure these credentials, Docker recommends using a **credential helper**. The helper encrypts and retrieves credentials from a secure store, in this case **[Pass](https://www.passwordstore.org)**, a GPG-based password manager. You can checkout other helpers at **[credential helpers](https://docs.docker.com/engine/reference/commandline/login/#credential-stores)**

---

## Step 1: Install Dependencies

Run these commands on the Jenkins agent or on your server:

```bash
sudo apt update
sudo apt install -y pass gnupg2 
```

`pass` uses **GPG** for encryption, so GPG must be configured first.

---

## Step 2: Generate a GPG Key

Generate a GPG key for your user (e.g., `vagrant` or `jenkins`):

```bash
gpg --generate-key
```

Follow the prompts:

* Choose RSA and RSA
* Set key size to **4096**
* Set expiration (or none)
* Enter your name and email

After generation, list the key:

```bash
gpg --list-secret-keys --keyid-format LONG
```

Copy the **GPG_KEY_ID** from the output, for example:

```
sec   rsa4096/AB12CD34EF56GH78 2025-11-03 [SC]
```

Here, `AB12CD34EF56GH78` is the **GPG_KEY_ID**.

---

## Step 3: Initialize `pass`

```bash
pass init AB12CD34EF56GH78
```

Replace `AB12CD34EF56GH78` with your actual GPG key ID.

This command sets up the password store under `~/.password-store`.

---

## Step 4: Install `docker-credential-pass`

If the helper is not installed, install it from source:

```bash
PASS_VERSION="v0.9.4"
curl -fsSL "https://github.com/docker/docker-credential-helpers/releases/download/${PASS_VERSION}/docker-credential-pass-${PASS_VERSION}.linux-amd64" \
    -o docker-credential-pass

# Make it executable and move to bin
sudo chmod +x docker-credential-pass
sudo mv docker-credential-pass /usr/local/bin/

# Verify installation
docker-credential-pass version
```

Verify installation:

```bash
docker-credential-pass version
which docker-credential-pass
docker-credential-pass list
```

---

## Step 5: Configure Docker to Use `pass`

Edit or create `~/.docker/config.json`:

```bash
mkdir -p ~/.docker
cat > ~/.docker/config.json <<'EOF'
{
  "credsStore": "pass"
}
EOF
```

---

## Step 6: Test the Setup

Run a Docker login and confirm no warning appears:

```bash
echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GITHUB_USER" --password-stdin
```

Check credentials are stored securely:

```bash
pass
```

If successful, your credentials will now be encrypted securely:

```
Password Store
└── docker-credential-helpers
    └── Z2hjci5pbw==
        └── Username2025
```

When you check the config file **`/home/$USER/.docker/config.json`** again you will see:
```
{
        "auths": {
                "ghcr.io": {}
        },
        "credsStore": "pass"
}
```

## Step 7: Inspect the existing entry

```bash
pass ls docker-credential-helpers
pass show docker-credential-helpers/Z2hjci5pbw==/Username2025
```

This shows what is stored now. **`Z2hjci5pbw==`** is base64 for **ghcr.io**.

- **Ask the helper what it returns**

```bash
printf "ghcr.io" | docker-credential-pass get
```
Expected output in this format:
```bash
{"ServerURL":"ghcr.io","Username":"WW91ciBVc2VybmFtZQ==","Secret":"WW91ciBQYXNzd29yZCBvciBQZXJzb25hbCBBY2Nlc3MgVG9rZW4="}
```

## Step 8: You can also store credentials using the helper API (correct format)

```bash
printf '{"ServerURL":"ghcr.io","Username":"Your-Username","Secret":"Your-Secret"}' \
  | docker-credential-pass store
```

- **Verify storage**

```bash
pass ls docker-credential-helpers
pass show docker-credential-helpers/Z2hjci5pbw==
docker-credential-pass list
printf "ghcr.io" | docker-credential-pass get
```

Expected: `docker-credential-pass list` includes ghcr.io and `get` prints the JSON with ServerURL, Username, Secret.

## Step 9: Test push from any environment example Jenkins

```bash
docker push ${env.CONTAINER_REGISTRY}/${env.USER_NAME}/${env.IMAGE_NAME}:${env.TAG}
```
Your credentials will no longer be stored unencrypted in **`/home/$USER/.docker/config.json`**

## Step 10: passwords can also be removed:

```
jenkins-agent@server ~ $ pass rm docker-credential-helpers/Z2hjci5pbw==/Username2025
rm: remove regular file ‘/home/jenkins-agent/.password-store/docker-credential-helpers/Z2hjci5pbw\=\=/Username.gpg’? y
removed ‘/home/jenkins-agent/.password-store/docker-credential-helpers/Z2hjci5pbw\=\=/Username2025.gpg’
```

---

## References

* Docker Docs: [Credential Store Configuration](https://docs.docker.com/engine/reference/commandline/login/#credential-stores)
* Docker Credential Helpers: [GitHub Repository](https://github.com/docker/docker-credential-helpers)
* Pass Documentation: [https://www.passwordstore.org/](https://www.passwordstore.org/)

---

## Troubleshooting Notes:

### Notes for Jenkins

* Always ensure `pass` and `docker-credential-pass` are installed on every agent that executes Docker commands.
* If using ephemeral agents, consider automating GPG key import and pass initialization within the agent startup script.

---

### GPG Passphrase Lock 

You will frequently be asked for the passphrase during Jenkins builds or after idle time, hence causing authentication errors like this one: **`unauthorized: unauthenticated: User cannot be authenticated with the token provided.`**.
Your **GPG key used by `pass` is locked**, and Docker cannot read credentials until you manually unlock it by entering your **GPG passphrase**. Once unlocked, everything works until the GPG agent times out again.

Here’s how to fix it permanently.

---

#### **1. Set GPG agent to cache your passphrase longer**

Edit or create this file:

```bash
nano ~/.gnupg/gpg-agent.conf
```

Add these lines:

```
default-cache-ttl 86400
max-cache-ttl 31536000
```

* `default-cache-ttl 86400` keeps it unlocked for 24 hours. 
* `max-cache-ttl 31536000` allows up to one year if the agent stays active.
* **NB: Modify the way you want.**

Then reload the agent:

```bash
gpgconf --kill gpg-agent
gpgconf --launch gpg-agent
```

---

#### **2. Unlock once manually**

Run:

```bash
pass show docker-credential-helpers/Z2hjci5pbw==/Username2025
```

Enter your passphrase one last time.
This unlocks the key and caches it in memory according to the timeout you set above and you are good to go.

---

#### **3. To make sure `pinentry` is non-interactive**

If Jenkins or your automation runs without a TTY, you must stop GPG from prompting for the passphrase.

Edit or create:

```bash
nano ~/.gnupg/gpg.conf
```

Add:

```
use-agent
```

Then edit:

```bash
nano ~/.gnupg/gpg-agent.conf
```

Add (or keep) these lines:

```
pinentry-program /usr/bin/pinentry-curses
allow-preset-passphrase
```

If Jenkins runs under a headless account, you can pre-cache the passphrase using:

```bash
echo "PASSPHRASE" | gpg-preset-passphrase --preset <KEYGRIP>
```

Find `<KEYGRIP>` with:

```bash
gpg --list-secret-keys --with-keygrip
```

---

#### **4. Test**

After applying these settings:

* Restart the GPG agent (`gpgconf --kill gpg-agent`).
* Run `pass show docker-credential-helpers/Z2hjci5pbw==/Username2025` once manually.
* Then retry `docker push`.

You should no longer be asked for the passphrase during Jenkins builds or after idle time.

---
**Thanks for your time, hope this was informative, like, share, and Star the Repo**



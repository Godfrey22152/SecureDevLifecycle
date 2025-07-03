## 🏗️ Infrastructure Setup: Jenkins, SonarQube, and Nexus Servers

This section outlines how to set up the required infrastructure—**Jenkins**, **SonarQube**, and **Nexus**—for the Secure and Compliant QA Pipeline. You can set these up either **locally** (e.g., on-premises or VM) or in the **cloud** (e.g., AWS EC2).

---

### ☁️ Cloud Setup: AWS EC2 Instances

You can host Jenkins, SonarQube, and Nexus each on a separate EC2 instance. Below are three options:

#### 📘 Option 1: Using AWS Management Console

Launch **three Ubuntu-based EC2 instances** from the AWS Console:

* **Jenkins**
* **SonarQube**
* **Nexus Repository Manager**

##### 📋 How to Add User Data During EC2 Instance Creation (AWS Console)

Follow these steps to correctly add a startup script (User Data) during EC2 instance setup:

1. **Log in to the AWS Management Console**

   * Visit [https://console.aws.amazon.com/ec2](https://console.aws.amazon.com/ec2) and sign in.

2. **Navigate to EC2 Service**

   * In the AWS Services dashboard, click **EC2**.

3. **Launch a New Instance**

   * Click **“Launch Instance”**.

4. **Choose an Amazon Machine Image (AMI)**

   * For example: *Ubuntu Server 22.04 LTS (HVM), SSD Volume Type*.

5. **Select an Instance Type**

   * Choose at least **t2.medium** or larger (recommended for Jenkins).

6. **Configure Instance Details**

   * Scroll down to the **Advanced Details** section.

7. **Add Your User Data Script**

   * In the **User data** field, paste the appropriate script (e.g., Jenkins, SonarQube, or Nexus setup script).
   * Ensure the script begins with `#!/bin/bash`.

8. **Add Storage**

   * Set the root volume to at least **16–32 GB**, especially for Jenkins.

9. **Configure Security Group**

   * Create or select a Security Group that allows the required inbound ports:

     * `22` (SSH)
     * `8080` (Jenkins)
     * `9000` (SonarQube)
     * `8081` (Nexus)

10. **Add Tags** *(optional)*

    * Add Name tag, e.g., `Name: Jenkins-Server`.

11. **Review and Launch**

    * Select a key pair or create a new one for SSH access.
    * Click **Launch Instance**.

12. **Access the Instance**

    * Once running, use the **Public IP** to access your service:

      * Jenkins: `http://<public-ip>:8080`
      * SonarQube: `http://<public-ip>:9000`
      * Nexus: `http://<public-ip>:8081`

##### 🔧 Jenkins User Data Script

```bash
 #!/bin/bash

 # Update the package list
 sudo apt-get update -y

 # Install Java
 sudo apt-get install -y openjdk-17-jre-headless

 # Jenkins installation process
 echo "Installing Jenkins package..."
 sudo wget -O /usr/share/keyrings/jenkins-keyring.asc https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
 echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
 sudo apt-get update -y
 sudo apt-get install -y jenkins

 # Docker installation
 echo "Installing Docker..."
 sudo apt-get install -y docker.io
 sudo systemctl start docker
 sudo systemctl enable docker
 sudo usermod -aG docker $USER
 sudo chmod 666 /var/run/docker.sock

 sudo usermod -aG docker jenkins
```

##### 🧪 SonarQube User Data Script

```bash
 #!/bin/bash
 sudo apt-get update

 ## Install Docker
 yes | sudo apt-get install docker.io
 sudo systemctl start docker
 sudo systemctl enable docker
 sudo usermod -aG docker $USER
 sudo chmod 666 /var/run/docker.sock
 echo "Waiting for 30 seconds before runing Sonarqube Docker container..."
 sleep 30

 ## Runing Sonarqube in a docker container
 docker run -d -p 9000:9000 --name sonarqube-container sonarqube:lts-community
```

##### 📦 Nexus User Data Script

```bash
 #!/bin/bash
 sudo apt-get update

 ## Install Docker
 yes | sudo apt-get install docker.io
 sudo systemctl start docker
 sudo systemctl enable docker
 sudo usermod -aG docker $USER
 sudo chmod 666 /var/run/docker.sock
 echo "Waiting for 30 seconds before running Nexus Docker container..."
 sleep 30

 ## Runing Nexus in a docker container
 docker run -d -p 8081:8081 --name nexus-container sonatype/nexus3:latest
```

---

#### 🖥️ Option 2: Using AWS CLI

Use the AWS CLI to launch instances with a User Data script:

```bash
aws ec2 run-instances --image-id ami-12345678 --instance-type t2.medium \
--key-name your-key-pair --security-group-ids sg-12345678 \
--subnet-id subnet-12345678 --user-data file://userdata.sh
```

📝 **Notes**:

* ▶️ `ami-12345678`: Replace with appropriate AMI ID.
* ▶️ `t2.medium`: Adjust size based on tool requirements.
* ▶️ `userdata.sh`: Path to the user data shell script.
* ▶️ `your-key-pair`: Replace with the name of your key pair for SSH access to the instance.
* ▶️ `file://userdata.sh`: Supplies the user data script (`userdata.sh` in this case) which will run automatically when the instance starts. If the `userdata.sh` is in the same directory where you’re running the command, you can reference it as `file://userdata.sh`. Otherwise, provide the full path, e.g., `file:///home/user/scripts/userdata.sh`.


---


#### ⚙️ Option 3: Fully Automated Setup with Terraform

Use the Terraform automation scripts in this GitHub repo to provision and bootstrap Jenkins, SonarQube, and Nexus:

➡️ [Terraform EC2 Setup Repo](https://github.com/Godfrey22152/automation-of-aws-infra-using-terraform-via-Gitlab)


---


### 🖥️ Local Setup (On-Prem / VM / Localhost)

Install all required tools manually or via Docker.

#### Jenkins (Manual)

* **Create and run the script jenkins.sh**, then:

```bash
 #!/bin/bash

 # Update the package list
 sudo apt-get update -y

 # Install Java
 sudo apt-get install -y openjdk-17-jre-headless

 # Jenkins installation process
 echo "Installing Jenkins package..."
 sudo wget -O /usr/share/keyrings/jenkins-keyring.asc https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
 echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
 sudo apt-get update -y
 sudo apt-get install -y jenkins
```

#### SonarQube (Docker)

```bash
docker run -d -p 9000:9000 --name sonarqube-container sonarqube:lts-community
```

#### Nexus (Docker)

```bash
docker run -d -p 8081:8081 --name nexus-container sonatype/nexus3:latest
```

---

### 🔗 Accessing the Tools

#### Jenkins

* URL: `http://<public-ip>:8080`
* Unlock:

```bash
sudo cat /var/jenkins_home/secrets/initialAdminPassword
```

* Install suggested plugins & create admin user.

#### SonarQube

* URL: `http://<public-ip>:9000`
* Default Login: `admin / admin`

#### Nexus

* URL: `http://<public-ip>:8081`
* Access initial admin password:

```bash
docker exec nexus-container cat /nexus-data/admin.password
```

---

### 🧰 Configure Jenkins Tools & Global Settings


#### 1. 🧰 Required Jenkins Plugins

Plugins are installed under **Manage Jenkins → Plugins → Available Plugins**:

Install Required Plugins:

   - **Blue Ocean**
   - **Pipeline: Stage View Plugin**
   - **OWASP Dependency-Check plugin**
   - **Warnings-Next Generation Plugin**
   - **jacoco Plugin**
   - **JUnit Plugin**
   - **Allure Jenkins Plugin**
   - **Slack Notification Plugin**
   - **Eclipse Temurin installer:** enables installation of different versions of JDK
   - **SonarQube Scanner for Jenkins**
   - **Config File Provider Plugin:** allows Jenkins to inject Nexus-related configuration files (like `settings.xml`) into build jobs for seamless artifact management.
   - **Maven Integration**
   - **Pipeline Maven Integration**

▶️ **See **[Install Required Plugins](./Infrastructure-Setup/Pipeline-Setup)** for detailed guidance.


---


#### 2. ⚙️ Configure Tools (Global Tool Configuration):

These tools are configured under **Manage Jenkins → Global Tool Configuration**:

   * JDK 17 (label: `jdk17`) > `JDK installations`
   * Maven 3 (label: `maven3`) > `Maven installations`
   * Sonar Scanner (label: `sonar-scanner`) > `SonarQube Scanner installations`
   * Dependency-Check (`OWASP-Dependency-Check`) > `Dependency-Check installations`
   * Allure Report (`Allure Commandline`) > `Allure Commandline installations`

▶️ **See **[Configure Global Tools](./Infrastructure-Setup/Pipeline-Setup)** for detailed guidance.


---


#### 3. 🧩 Create Managed Config File for Nexus:

Config File is configured under **Manage Jenkins → Managed files → Config File Management**

   * **Create New Config** → **Type:** `Global Maven settings.xml`
   * **`maven-settings`** (ID for Maven `settings.xml`)

▶️ **See **[Create Nexus Config file](./Infrastructure-Setup/Pipeline-Setup)** for detailed guidance.


---


#### 4. 🔑 Add Jenkins Credentials:

Add Credentials under **Manage Jenkins → Credentials**

   * Git credentials (ID: `git-cred`)
   * Nexus deployment credentials (if using secured repo)
   * SonarQube Token (ID: `sonar-token`)
   * Slack Notification Credentials (ID: `slack-cred`)

▶️ **See **[How to add Credentials](./Infrastructure-Setup/Pipeline-Setup)** for detailed guidance


---

Kindly visit: 

➡️ **[Pipeline Setup](./Infrastructure-Setup/Pipeline-Setup)** for detailed guidance on how to setup the entire pipeline.

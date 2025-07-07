# 🧰 Jenkins Tools & Configuration Guide for Secure and Compliant QA CI Pipeline

This README provides a step-by-step guide to set up and configure all necessary Jenkins tools and settings required to run the **Secure and Compliant QA Pipeline**.

---

## 1️⃣ 🧩 Install Required Jenkins Plugins

Plugins allow Jenkins to integrate various tools used in this pipeline.

### 🔧 How to Install Plugins

1. Go to **`Manage Jenkins` → `Plugin Manager`**
2. Navigate to the **Available** tab
3. Search and check each of the following plugins
4. Click **Install without restart**

### ✅ Required Plugins

* **Blue Ocean** – Modern UI for Jenkins pipelines
* **Pipeline: Stage View Plugin** – Enables visual stage breakdown
* **OWASP Dependency-Check plugin** – Enables security scan in Jenkins jobs
* **Warnings Next Generation Plugin** – Parses output from code quality tools like SpotBugs & Checkstyle
* **JaCoCo Plugin** – Code coverage report parser
* **JUnit Plugin** – Publishes test results from JUnit
* **Allure Jenkins Plugin** – Visual reports for test execution
* **Slack Notification Plugin** – Enables Slack messages on pipeline success/failure
* **Eclipse Temurin installer** – Adds Java tool installers to Jenkins
* **SonarQube Scanner for Jenkins** – Static code analysis support
* **Config File Provider Plugin** – Manages config files like Maven `settings.xml`
* **Maven Integration Plugin** – Integrates Maven for builds
* **Pipeline Maven Integration Plugin** – Supports advanced Maven-Pipeline usage

---

## 2️⃣ ⚙️ Configure Global Tools

These tools are configured under **`Manage Jenkins` → `Global Tool Configuration`**.

### 🔹 Java (JDK 17)

* Scroll to **JDK installations** section
* Click **`Add JDK`** and provide the following details:

  * Name: `jdk17`
  * Check **Install automatically** and Click **Add Installer**
  * Then select **`Install from adoptium.net`** from the list of Installers.
  * Proceed to select recent JDK Version of your choice to install Under the **`OpenJDK 17-Hotspot`** 
  

### 🔹 Maven (Maven 3)

* Scroll to **Maven installations** section
* Click **`Add Maven`** and provide the following details:

  * Name: `maven3`
  * Check **Install automatically** and Click **Add Installer**
  * Select **`Install from Apache`** from the list of installers.
  * Proceed to select a recent Apache Version from the list.


### 🔹 SonarQube Scanner

* Scroll to **SonarQube Scanner installations** section
* Click **Add SonarQube Scanner** and provide the following details:

  * Name: `sonar-scanner`
  * Check **Install automatically** and Click **Install from Maven Central**
  * Select a recent Version of SonarQube Scanner from the list to install.
  

### 🔹 OWASP Dependency-Check

* Scroll to **Dependency-Check installations**
* Click **Add Dependency-Check** and provide the following details:

  * Name: `OWASP-Dependency-Check`
  * Check **Install automatically** and Click **Install from github.com**
  * Select a recent Version of Dependency-Check from the list to install.


### 🔹 Allure Commandline

* Scroll to **Allure Commandline installations**
* Click **Add Allure Commandline** and provide the following details:

  * Name: `Allure Commandline`or Version name: `2.330`
  * Check **Install automatically** and Click **Install from Maven Central**
  * Select a recent Version from the list to install.
    
* **Click `Save or Apply` on the page to apply the changes.**

---

## 3️⃣ 🧩 Create and Configure Nexus Config File (Nexus `settings.xml`)

### 🔹 How to Add Nexus Settings File

1. Go to **`Manage Jenkins` → `Managed Files`**
2. Click **Add a new Config**
3. Choose Type **Global Maven settings.xml**
4. Set:

   * ID: `maven-settings`
   * Name: `MyGlobalSettings`
   * Add your `settings.xml` content under the **`Server Credentials`** (with Nexus server info, credentials ID, mirrors, etc.)

5. See example of how to add the content below under the server section: 

  ```bash
  <!-- servers
    | This is a list of authentication profiles, keyed by the server-id used within the system.
    | Authentication profiles can be used whenever maven must make a connection to a remote server.
    |-->
    <servers>
      
      <server>
        <id>maven-releases</id>
        <username>admin</username>
        <password>your-password</password>
      </server>
      
      <server>
        <id>maven-snapshots</id>
        <username>admin</username>
        <password>your-password</password>
      </server>
      
      
      <!-- Another sample, using keys to authenticate.
      <server>
        <id>siteServer</id>
        <privateKey>/path/to/private/key</privateKey>
        <passphrase>optional; leave empty if not used.</passphrase>
      </server>
      -->
    </servers>
  ```

---


## 4️⃣ 🔑 Add Jenkins Credentials

Credentials are needed for Git access, SonarQube analysis, and Slack notifications.

### 🔹 How to Add Credentials

1. Go to **`Manage Jenkins` → `Credentials` → `(global)` → `Add Credentials`**

### ✅ Required Credentials

* **Git Credentials**

  * Kind: Username with password or SSH key
  * ID: `git-cred`

* **SonarQube Personal Access token**

  * Kind: Secret text
  * ID: `sonar-token`
  * Generate from SonarQube: **`Administration` → `Security` → `Users` → `Tokens` → `Generate Token`**

* **Slack Webhook Credentials**

  * Kind: Secret text
  * ID: `slack-cred`
  * Generated from Slack App configuration. 
  * ⏩ See how to generate **[Webhook Token](#-slack-configuration-in-jenkins)** below.


---


## 5️⃣ 📊 SonarQube Server Configuration in Jenkins

After installing the **[SonarQube Scanner Plugin](#1--install-required-jenkins-plugins)** and configuring the **[SonarQube Scanner](#-sonarqube-scanner)**, configure your SonarQube server:

1. Go to **Manage Jenkins → System**
2. Scroll to **SonarQube servers**
3. Click **Add SonarQube**
4. Fill in the following:

   * **Name**: `sonar-server`
   * **Server URL**: `http://<your-sonarqube-ip>:9000`
   * **Server authentication token**:

     * Click **Add**, choose **Secret text**
     * Paste the token generated from SonarQube dashboard
     * Give it an ID (e.g., `sonar-token`)
   * Select the token from dropdown
5. Save the configuration.


---


## 6️⃣ 📣 Slack Configuration in Jenkins

After installing **Slack Notification Plugin**, configure Slack integration:

1. 🔧 **Create a Slack Channel and configure Jenkins with Slack Channel Token:**

   * ⏩ Access your **Slack account → `Workspace` → `Channel` → `Add Channel` → `Create New Channel`** to create a chennel if you have now yet.
   * ⏩ In your Slack workspace, navigate to **`Tools & Settings` → `Administration` → `Manage apps`** then use the `Search Slack marketplace`to search for **`Jenkins CI`**.
   * ⏩ Click **`Add to Slack`** to add the Jenkins CI to slack.
   * ⏩ Click the dropdown on the page to **`Choose a Channel`** the newly crated channel or your existing channel, then proceed to click **`Add Jenkins CI Integration`**
   * ⏩ On the Integration Page, scroll to **`Step 3`** and copy your **`Team Subdomain`** and **`Integration Token Credential ID`** which you will use to configure Slack Notification in Jenkins and save the settings.


2. 🔧 **In Jenkins:**

   * ⏩ Go to **`Manage Jenkins` → `Configure System`**
   * ⏩ Scroll to **Slack**
   * ⏩ Fill in the fields:

     * **Workspace**: Add your Slack workspace (e.g., your copied **`Team Subdomain`** from Slack Jenkins CI Integration).
     * **Integration Token Credential ID**: Create secret text credential with your copied **`Integration Token Credential ID`** (ID: `slack-cred`)
     * **Channel**: `#your-channel-name`
     * Click **Test Connection**. A test message should appear in your Slack channel.
   * Save the configuration

> ✅ Done! Jenkins can now send build notifications directly to your Slack channel.


---


## 7️⃣ 🧪 Maven Project Dependencies & Plugins Setup in project's **[`pom.xml`](https://github.com/Godfrey22152/SecureDevLifecycle/blob/quality-assurance/pom.xml)**

Your **[`pom.xml`](https://github.com/Godfrey22152/SecureDevLifecycle/blob/quality-assurance/pom.xml)** should include the following plugins and dependencies to support Jenkins stages:

### 🔹 Plugin Sections (under `<build><plugins>`):

* **Maven Compiler Plugin** – Sets Java version to compile (required for JDK 17)
* **WAR Plugin** – Packages your app into a WAR for deployment
* **Surefire Plugin** – Executes unit tests
* **OWASP Dependency-Check** – Identifies vulnerabilities in dependencies
* **SpotBugs Plugin** – Performs static analysis
* **Checkstyle Plugin** – Validates code style (Google checks used)
* **JaCoCo Plugin** – Generates code coverage reports
* **Allure Plugin (via BOM)** – Generates visual test reports
* **Maven Dependency Plugin** – Pulls in tools like `webapp-runner` for embedded deployments

---

### 🔹🧬 Dependencies Used (under `<dependencies>`): 

#### For robust unit testing.
  * **JUnit 5** – Testing framework
  * **Mockito** – Mocking for tests
  * **AssertJ** – Fluent assertions 

#### For weaving, reporting, and utility functions.
  * **AspectJ Weaver** – Required for Allure runtime instrumentation
  * **Apache Commons IO** – Added for file I/O functionality and vulnerability patching
  * **Allure Dependency** - Allure for Reporting

#### For project runtime dependencies.
  * **MongoDB Driver** – Core dependency for database communication
  * **Servlet API** – Supports servlet-based WAR packaging


---


### 🚀 Nexus Deployment Configuration

This config ensures Maven knows where to deploy artifacts:

```xml
<distributionManagement>
    <repository>
        <id>maven-releases</id>
        <name>maven-releases</name>
        <url>http://<your-nexus-server-ip>:8081/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>maven-snapshots</id>
        <name>maven-snapshots</name>
        <url>http://<your-nexus-server-ip>:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

* `maven-releases`: For publishing stable release artifacts.
* `maven-snapshots`: For publishing ongoing development builds.

> ⚙️ To get this block:
>
> * Refer to the [Nexus 3 Documentation](https://help.sonatype.com/repomanager3) under "Repositories" → "Maven Hosted Repository" section.
> * You define your own hosted `release` and `snapshot` repos in Nexus. Each will generate a URL. Use those URLs in your `<distributionManagement>` block.
> * [Apache Maven Docs](https://maven.apache.org/pom.html#distributionmanagement) also gives context on structuring this section.

> ⚠️ Ensure the Jenkins job has the correct credentials and that `maven-settings` config includes credentials and repository URLs.


---


### 🔹 Where to Find Plugin/Dependency Blocks

Use the official repositories:

* **Maven Central Repository:** [https://mvnrepository.com](https://mvnrepository.com)
* **Maven Central Repository:** [https://search.maven.org](https://search.maven.org)
* **Allure:** [https://allurereport.org/docs/junit5/#specify-description-links-and-other-metadata](https://allurereport.org/docs/junit5/#specify-description-links-and-other-metadata)
* **OWASP DC Plugin:** [https://jeremylong.github.io/DependencyCheck/dependency-check-maven/](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/)
* **SpotBugs:** [https://spotbugs.github.io/spotbugs-maven-plugin/usage.html](https://spotbugs.github.io/spotbugs-maven-plugin/usage.html)
* **Checkstyle** [https://maven.apache.org/plugins/maven-checkstyle-plugin/checkstyle-mojo.html](https://maven.apache.org/plugins/maven-checkstyle-plugin/checkstyle-mojo.html)
* **Jacoco Maven Plugin:** [https://mvnrepository.com/artifact/org.jacoco/jacoco-maven-plugin](https://mvnrepository.com/artifact/org.jacoco/jacoco-maven-plugin)
* **Maven Surefire Plugin** [https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-surefire-plugin](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-surefire-plugin)
* **[SonarQube Test Code Coverage](https://docs.sonarsource.com/sonarqube-server/10.8/analyzing-source-code/test-coverage/java-test-coverage/)**

> 📌 These dependencies improve testability, quality, and reporting.
> 🧾 Visit [Maven Central](https://search.maven.org/) to get the latest artifact versions.


---


With this configuration in your Jenkins and `pom.xml`, the project is fully integrated for secure, test-driven, and quality-assured continuous integration and delivery.

Your Jenkins setup is now ready to run the Secure and Compliant QA Pipeline! 🚀


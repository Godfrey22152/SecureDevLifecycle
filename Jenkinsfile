
pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '2'))
    }

    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'quality-assurance', description: 'Branch to build')
    }

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }
    
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        SLACK_CHANNEL = '#devops-projects'
        REPO_URL = 'https://github.com/Godfrey22152/SecureDevLifecycle.git'
    }

    stages {
        stage('Checkout & Initialize') {
            steps {
                git branch: "${params.BRANCH_NAME}",
                     credentialsId: 'git-cred', 
                     url: "${env.REPO_URL}"
                sh 'mvn --version' // Verify environment
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Security & Quality Checks') {
            parallel {
                stage('OWASP Dependency-Check') {
                    steps {
                        dependencyCheck additionalArguments: '''
                            --scan **/target/dependency/**/*.jar 
                            --format XML 
                            --project "TrainBooking-App"
                            --out target/OWASP-dependency-check
                            ''', 
                        odcInstallation: 'OWASP-Dependency-Check'
                        dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
                    }
                }
                stage('SpotBugs Analysis') {
                    steps {
                        sh 'mvn spotbugs:spotbugs'
                    }
                    post {
                        success {
                            recordIssues tools: [spotBugs(pattern: '**/spotbugsXml.xml')]
                            archiveArtifacts artifacts: '**/spotbugsXml.xml'
                        }
                        failure {
                            echo 'SpotBugs failed - build will fail as expected.'
                            error('SpotBugs analysis failed.')
                        }
                    }
                }

                stage('Checkstyle Analysis') {
                    steps {
                        sh 'mvn checkstyle:checkstyle'
                    }
                    post {
                        success {
                            recordIssues tools: [checkStyle(pattern: '**/checkstyle-result.xml')]
                            archiveArtifacts artifacts: '**/checkstyle-result.xml'
                        }
                        failure {
                            echo 'Checkstyle failed - build will fail as expected.'
                            error('Checkstyle analysis failed.')
                        }
                    }
                }
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn jacoco:report -Ddependency-check.skip=true'
            }
            post {
                always {
                    // Publish JUnit test results
                    junit 'target/surefire-reports/*.xml'
                    
                    // Publish Allure report
                    allure([ 
                        includeProperties: false,
                        jdk: '', 
                        results: [[path: 'target/allure-results']],
                        reportBuildPolicy: 'ALWAYS'
                    ])
                    
                    // Publish JaCoCo coverage report
                    jacoco(
                      execPattern: '**/jacoco.exec',        // Coverage data file
                      classPattern: '**/classes',          // Compiled classes
                      sourcePattern: 'src/main/java',          // Source code directory
                      exclusionPattern: '**/test/**/*.class',   // Exclude test classes
                      skipCopyOfSrcFiles: false,                // Include source files in report
                      changeBuildStatus: true                   // Fail build if coverage thresholds are unmet
                    )
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    withSonarQubeEnv('sonar-server') {
                        sh """
                            $SCANNER_HOME/bin/sonar-scanner \
                            -Dsonar.projectName=TrainBooking-App \
                            -Dsonar.projectKey=TrainBooking-App \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.coverage.jacoco.xmlReportPaths=**/jacoco.xml
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Publish Artifacts') {
            steps {
                withMaven(globalMavenSettingsConfig: 'maven-settings', jdk: 'jdk17', maven: 'maven3', mavenSettingsConfig: '', traceability: true) {
                    sh "mvn deploy -Ddependency-check.skip=true"
                }
            }
        }
    }
    
    post {
        success {
            archiveArtifacts artifacts: 'target/*.war, target/**/*.xml, target/**/*.html', fingerprint: true
        
            slackSend channel: "${env.SLACK_CHANNEL}",
                      color: 'good',
                      message: """
                        ✅ *Build Successful* ✅
                        *Job*: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Build URL*: ${env.BUILD_URL}
                        *Duration*: ${currentBuild.durationString}
                        *Artifacts*: ${env.BUILD_URL}artifact/target/
                        *Reports*:
                        - *SpotBugs Report*: ${env.BUILD_URL}target/spotbugs/spotbugsXml.xml
                        - *OwASP Dependency Check Report*: ${env.BUILD_URL}target/OWASP-dependency-check/dependency-check-report.xml
                        - *CheckStyle Report*: ${env.BUILD_URL}target/checkstyle-report/checkstyle-result.xml
                        - *Code Coverage Report*: ${env.BUILD_URL}target/jacoco-report/index.html
                      """
        }
        failure {
            slackSend channel: "${env.SLACK_CHANNEL}",
                      color: 'danger',
                      message: """
                        ❌ *Build Failed* ❌
                        *Job*: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Duration*: ${currentBuild.durationString}
                        *Build URL*: ${env.BUILD_URL}
                      """
        }    
    }
}

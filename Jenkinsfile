pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }
    
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {
        stage('Checkout & Initialize') {
            steps {
                git branch: 'main', 
                     credentialsId: 'git-cred', 
                     url: 'https://github.com/Godfrey22152/DevSecOps.git'
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
                        always {
                            recordIssues tools: [
                                spotBugs(pattern: '**/spotbugsXml.xml')
                            ]
                            archiveArtifacts artifacts: '**/spotbugsXml.xml'
                        }
                    }
                }

                stage('Checkstyle Analysis') {
                    steps {
                        sh 'mvn checkstyle:checkstyle'
                    }
                    post {
                        always {
                            recordIssues tools: [
                                checkStyle(pattern: '**/checkstyle-result.xml')
                            ]
                            archiveArtifacts artifacts: '**/checkstyle-result.xml'
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
        
            slackSend channel: '#devops-projects',
                      color: 'good',
                      message: """
                        ✅ *Build Successful* ✅
                        *Job*: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Build URL*: ${env.BUILD_URL}
                        *Duration*: ${currentBuild.durationString}
                        *Project Artifact*: ${env.BUILD_URL}target/*.war
                        *SpotBugs Report*: ${env.BUILD_URL}target/spotbugs/spotbugsXml.xml
                        *OwASP Dependency Check Report*: ${env.BUILD_URL}target/OWASP-dependency-check/dependency-check-report.xml
                        *CheckStyle Report*: ${env.BUILD_URL}target/checkstyle-report/checkstyle-result.xml
                        *Code Coverage Report*: ${env.BUILD_URL}target/jacoco-report/index.html
                      """
        }
        failure {
            slackSend channel: '#devops-projects',
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


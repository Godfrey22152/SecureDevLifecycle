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
                sh 'mvn --version'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify jacoco:report -Ddependency-check.skip=true'
            }
        }

        stage('Security & Quality Checks') {
            parallel {
                stage('OWASP Dependency-Check') {
                    steps {
                        dependencyCheck additionalArguments: '''
                            --scan . 
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
                            echo 'SpotBugs failed.'
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
                            echo 'Checkstyle failed.'
                            error('Checkstyle analysis failed.')
                        }
                    }
                }
            }
        }

        stage('Unit Tests & Reports') {
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    
                    allure([ 
                        includeProperties: false,
                        jdk: '', 
                        results: [[path: 'target/allure-results']],
                        reportBuildPolicy: 'ALWAYS'
                    ])
                    
                    jacoco(
                        execPattern: '**/jacoco.exec',
                        classPattern: '**/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: '**/test/**/*.class',
                        skipCopyOfSrcFiles: false,
                        changeBuildStatus: true
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    withSonarQubeEnv('sonar-server') {
                        sh """
                            mvn clean verify jacoco:report sonar:sonar \
                            -Dsonar.projectName=TrainBooking-App \
                            -Dsonar.projectKey=TrainBooking-App \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        """
                    }
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
                - SpotBugs: ${env.BUILD_URL}artifact/target/spotbugsXml.xml
                - OWASP Report: ${env.BUILD_URL}artifact/target/OWASP-dependency-check/dependency-check-report.xml
                - Checkstyle: ${env.BUILD_URL}artifact/target/checkstyle-result.xml
                - Coverage: ${env.BUILD_URL}artifact/target/site/jacoco/index.html
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

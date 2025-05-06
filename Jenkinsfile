pipeline {
    agent any

    stages {
        stage('Run All Branch Jobs in Parallel') {
            parallel {
                stage('Trigger Quality Assurance Pipeline') {
                    steps {
                        script {
                            build job: 'SecureDevLifecycle/quality-assurance', wait: true
                        }
                    }
                }
                stage('Trigger Container Security Pipeline') {
                    steps {
                        script {
                            build job: 'SecureDevLifecycle/container-security', wait: true, parameters: [
                                string(name: 'RELEASE_TYPE', value: 'dev')
                            ]
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            slackSend channel: '#devops-projects',
                      color: 'good',
                      message: """
                        ✅ *Parent Pipeline Successful*
                        *Job*: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Build URL*: ${env.BUILD_URL}
                      """
        }
        failure {
            slackSend channel: '#devops-projects',
                      color: 'danger',
                      message: """
                        ❌ *Parent Pipeline Failed*
                        *Job*: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Build URL*: ${env.BUILD_URL}
                      """
        }
    }
}

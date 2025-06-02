pipeline {
    agent any

    environment {
        QA_STATUS = ''
        CONTAINER_STATUS = ''
        DEPLOYMENT_STATUS = ''
    }

    stages {
        stage('Run All Branch Jobs in Parallel') {
            parallel {
                stage('Trigger Quality Assurance Pipeline') {
                    steps {
                        script {
                            try {
                                build job: 'quality-assurance', wait: true
                                env.QA_STATUS = "SUCCESS"
                            } catch (Exception e) {
                                env.QA_STATUS = "FAILED"
                                echo "Quality Assurance Pipeline failed: ${e}"
                            }
                        }
                    }
                }
                stage('Trigger Container Security Pipeline') {
                    steps {
                        script {
                            try {
                                build job: 'container-security', wait: true, parameters: [
                                    string(name: 'RELEASE_TYPE', value: 'dev')
                                ]
                                env.CONTAINER_STATUS = "SUCCESS"
                            } catch (Exception e) {
                                env.CONTAINER_STATUS = "FAILED"
                                echo "Container Security Pipeline failed: ${e}"
                            }
                        }
                    }
                }
            }
        }

        stage('Trigger Deployment Pipeline') {
            when {
                expression { return env.CONTAINER_STATUS == "SUCCESS" }
            }
            steps {
                script {
                    try {
                        echo "✅ Container Security Pipeline passed. Triggering Deployment Pipeline..."
                        build job: 'deployment', wait: true
                        env.DEPLOYMENT_STATUS = "SUCCESS"
                    } catch (Exception e) {
                        env.DEPLOYMENT_STATUS = "FAILED"
                        echo "Deployment Pipeline failed: ${e}"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Build the Slack message
                def message = ""
                if (env.QA_STATUS == "FAILED" || env.CONTAINER_STATUS == "FAILED" || env.DEPLOYMENT_STATUS == "FAILED") {
                    message += "❌ *One or More Child Jobs Failed*\n"
                } else {
                    message += "✅ *All Child Jobs Succeeded*\n"
                }

                message += """
                    *Parent Pipeline Job*: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                    *Build URL*: ${env.BUILD_URL}
                    ---
                    *Quality Assurance Pipeline*: ${env.QA_STATUS ?: 'NOT RUN'}
                    *Container Security Pipeline*: ${env.CONTAINER_STATUS ?: 'NOT RUN'}
                    *Deployment Pipeline*: ${env.DEPLOYMENT_STATUS ?: 'NOT RUN'}
                """

                // Send Slack notification
                slackSend(
                    channel: '#devops-projects',
                    color: (env.QA_STATUS == "FAILED" || env.CONTAINER_STATUS == "FAILED" || env.DEPLOYMENT_STATUS == "FAILED") ? 'danger' : 'good',
                    message: message
                )
            }
        }
    }
}

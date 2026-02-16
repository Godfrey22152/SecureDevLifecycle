pipeline {
    agent { label 'slave-1' }

    environment {
        IMAGE_NAME = 'ghcr.io/godfrey22152/trainbook-app'
        GITHUB_CREDENTIALS_ID = 'git-cred'
        KUBE_CREDENTIALS_ID = 'k8-cred'
        KUBE_NAMESPACE = 'webapps'
        KUBE_CLUSTER_NAME = 'kubernetes'
        KUBE_SERVER_URL = 'https://IP-ADDRESS:6443'
    }

    stages {
        stage('Checkout Deployment Branch') {
            steps {
                git branch: 'deployment',
                    changelog: false,
                    credentialsId: "${GITHUB_CREDENTIALS_ID}",
                    poll: false,
                    url: 'https://github.com/Godfrey22152/SecureDevLifecycle.git'
            }
        }

        stage('Install kubectl') {
            steps {
                sh '''
                    echo "Installing kubectl..."
                    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                    chmod +x kubectl
                    sudo mv kubectl /usr/local/bin/
                    kubectl version --client
                '''
            }
        }

        stage('Deploy Kubernetes Resources') {
            steps {
                script {
                    withKubeConfig([
                        caCertificate: '',
                        clusterName: "${KUBE_CLUSTER_NAME}",
                        contextName: '',
                        credentialsId: "${KUBE_CREDENTIALS_ID}",
                        namespace: "${KUBE_NAMESPACE}",
                        serverUrl: "${KUBE_SERVER_URL}",
                        restrictKubeConfigAccess: false
                    ]) {
                        sh "kubectl apply -f Manifest_Files/ -n ${KUBE_NAMESPACE}"
                    }
                }
            }
        }

        stage('Verify Deployment Resources') {
            steps {
                script {
                    withKubeConfig([
                        caCertificate: '',
                        clusterName: "${KUBE_CLUSTER_NAME}",
                        contextName: '',
                        credentialsId: "${KUBE_CREDENTIALS_ID}",
                        namespace: "${KUBE_NAMESPACE}",
                        serverUrl: "${KUBE_SERVER_URL}",
                        restrictKubeConfigAccess: false
                    ]) {
                        sh """
                            echo "Waiting for deployment to stabilize..."
                            sleep 30
                            kubectl get pods -n ${KUBE_NAMESPACE}
                            kubectl get svc trainbook-service -n ${KUBE_NAMESPACE}
                            kubectl get ingress trainbook-ingress -n ${KUBE_NAMESPACE}
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline execution completed."
        }
        failure {
            echo "❌ Deployment failed. Please check the logs."
        }
        success {
            echo "✅ Deployment and verification completed successfully!"
        }
    }
}

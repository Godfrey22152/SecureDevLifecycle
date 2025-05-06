pipeline {
    agent {label 'slave-1'}
    
    parameters {
        string(name: 'RELEASE_TYPE', description: 'Release type (e.g., prod, staging, dev)', defaultValue: 'dev')
    }
    environment {
        IMAGE_NAME = 'godfrey22152/devsecops'
        TRIVY_TIMEOUT = '15m'
    }
    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'main', 
                    changelog: false, 
                    credentialsId: 'git-cred', 
                    poll: false, 
                    url: 'https://github.com/Godfrey22152/DevSecOps.git'
            }
        }
        
        stage('Set Up Docker Build Variables') {
            steps {
                script {
                    // Generate timestamp
                    def TIMESTAMP = sh(script: 'date "+%Y-%m-%d_%H-%M-%S"', returnStdout: true).trim()
                    
                    // Create tag using parameter and timestamp
                    env.TAG = "${params.RELEASE_TYPE}-${TIMESTAMP}"
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                // Build Docker image using environment variables
                sh "docker build -t ${env.IMAGE_NAME}:${env.TAG} ."
            }
        }
        
        stage('Verify Docker Image') {
            steps {
                script {
                    // Check if image exists locally
                    def imageExists = sh(
                        script: "docker image inspect ${env.IMAGE_NAME}:${env.TAG} > /dev/null 2>&1",
                        returnStatus: true
                    ) == 0
            
                    if (!imageExists) {
                        error("Docker image not found locally. Build failed?")
                    }
            
                    // Get image size in bytes
                    def imageSizeBytes = sh(
                        script: "docker image inspect ${env.IMAGE_NAME}:${env.TAG} --format='{{.Size}}'",
                        returnStdout: true
                    ).trim()
        
                    // Convert bytes to MB (rounded to 2 decimal places)
                    def imageSizeMB = String.format('%.2f', imageSizeBytes.toFloat() / (1024 * 1024))
        
                    echo "Scanning image size: ${imageSizeMB} MB"
                }
            }
        }
        
        stage('Image Security Scans') {
            parallel {
                stage('Trivy Image Scan') {
                    steps {
                        script {
                            sh "mkdir -p trivy-reports"
                            // Added timeout and improved error handling
                            try {
                                sh """
                                    trivy image \
                                        --timeout ${env.TRIVY_TIMEOUT} \
                                        --severity HIGH,CRITICAL \
                                        --exit-code 1 \
                                        --format json \
                                        --output trivy-reports/trivy-scan.json \
                                        ${env.IMAGE_NAME}:${env.TAG}
                                """
                            } catch (ex) {
                                // Preserve scan artifacts for investigation
                                echo "Trivy scan failed: ${ex}"
                                archiveArtifacts artifacts: 'trivy-reports/trivy-scan.json'
                                error("Trivy scan failed: ${ex}")
                            }
                        }
                        archiveArtifacts artifacts: 'trivy-reports/**', allowEmptyArchive: false
                    }
                }
                
                stage('Grype Security Scan') {
                    steps {
                        script {
                            // Create reports directory
                            sh 'mkdir -p grype-reports'
                            // Run Grype scan with failure conditions
                            sh """
                                docker run --rm \
                                    -v /var/run/docker.sock:/var/run/docker.sock \
                                    -v ${WORKSPACE}/grype-reports:/reports \
                                    anchore/grype:latest \
                                    docker:${env.IMAGE_NAME}:${env.TAG} \
                                    --scope all-layers \
                                    --output cyclonedx-json \
                                    --file /reports/grype-scan.cdx.json \
                                    --fail-on=high  
                            """
                        }
                        // Archive the CycloneDX JSON report
                        archiveArtifacts artifacts: 'grype-reports/**', allowEmptyArchive: false
                    }
                }
            }
        }
        
        stage('Finally Done') {
            steps {
                echo 'Congratulations No CRITICAL VULNERABILITIES were found'
            }
        }
    }
}

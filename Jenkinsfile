pipeline {
    agent {label 'slave-1'}
    
    parameters {
        string(name: 'RELEASE_TYPE', description: 'Release type (e.g., prod, staging, dev)', defaultValue: 'dev')
    }
    environment {
        IMAGE_NAME = 'godfrey22152/securedevlifecycle'
        TRIVY_TIMEOUT = '15m'
        REGISTRY = 'ghcr.io'
        GITHUB_CREDENTIALS_ID = 'git-cred'
        COSIGN_PASSWORD_ID = 'cosign-password'
        COSIGN_PRIVATE_KEY_ID = 'cosign-private-key'
        COSIGN_PUBLIC_KEY_ID = 'cosign-public-key' 
    }
    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'container-security', 
                    changelog: false, 
                    credentialsId: 'git-cred', 
                    poll: false, 
                    url: 'https://github.com/Godfrey22152/SecureDevLifecycle.git'
            }
        }

        stage('Lint Dockerfile') {
            steps {
                script {
                    echo "=== Dockerfile Quality Check ==="
        
                    // Run linting with clean output
                    def lintStatus = sh(
                        script: '''
                            set +x  # Disable command echoing
                            docker run --rm -i hadolint/hadolint < Dockerfile \
                                | tee hadolint-results.txt \
                                | grep -E 'DL|SC' \
                                || true
                        ''',
                        returnStatus: true
                    )
                    
                    // Process results
                    def lintResults = readFile('hadolint-results.txt').trim()
                    
                    if (lintResults.isEmpty()) {
                        echo "✅ 🎉 Dockerfile passed all quality checks!"
                    } else {
                        echo "❌ 🔍 Found linting issues:"
                        // Format findings
                        lintResults.split('\n').eachWithIndex { line, index ->
                            // Fix 2: Better ANSI code removal regex
                            def cleanLine = line.replaceAll(/\e\[[0-9;]*m/, '')
                            echo "  ${index + 1}. ${cleanLine.trim()}"
                        }
                        
                        def issueCount = lintResults.count('\n') + 1
                        echo "🚨 Found ${issueCount} linting issue(s) needing attention"
                        
                        archiveArtifacts artifacts: 'hadolint-results.txt', allowEmptyArchive: false
                        error("Dockerfile validation failed with ${issueCount} issues")
                    }  
                }  
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
        
        stage('Login to GitHub Container Registry (GHCR)') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'git-cred', 
                    passwordVariable: 'GITHUB_TOKEN', 
                    usernameVariable: 'GITHUB_USER'
                )]) {
                    sh "docker login ${env.REGISTRY} -u \$GITHUB_USER -p \$GITHUB_TOKEN"
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
        
        stage('Push Image to GitHub Container Registry (GHCR)') {
            steps {
                echo 'CONGRATULATIONS No CRITICAL VULNERABILITIES WERE FOUND, PROCEEDING TO PUSH IMAGE'
                sh "docker push ${env.IMAGE_NAME}:${env.TAG}"
            }
        }

        stage('Sign Container Image with Cosign') {
            steps {
                script {
                    echo "[Cosign] Version Check"
                    sh "cosign version"
                    withCredentials([string(credentialsId: 'cosign-private-key', variable: 'COSIGN_PRIVATE_KEY')]) {
                        sh """
                            set -e  # Exit immediately on error
                            echo "[Cosign] Signing ${env.REGISTRY}/${env.IMAGE_NAME}:${env.TAG}"
                    
                            # Sign with recursive flag
                            cosign sign \\
                                --key "${COSIGN_PRIVATE_KEY}" \\
                                --yes \\
                                --recursive \\
                                "${env.REGISTRY}/${env.IMAGE_NAME}:${env.TAG}"
                    
                            # Capture digest after successful signing
                            DIGEST=\$(cosign triangulate "${env.REGISTRY}/${env.IMAGE_NAME}:${env.TAG}")
                            echo "✅ Signed digest: \${DIGEST}"
                        """
                    }
                }
            }
        }

        stage('Verify Cosign Signature') {
            steps {
                script {
                    echo "[Cosign] Version Check"
                    sh "cosign version"
            
                    withCredentials([string(credentialsId: 'cosign-public-key', variable: 'COSIGN_PUBLIC_KEY')]) {
                        sh """
                            set -e                  
                            echo "[Cosign] Verifying ${env.REGISTRY}/${env.IMAGE_NAME}:${env.TAG}"
                            cosign verify \\
                                --key "${COSIGN_PUBLIC_KEY}" \\
                                "${env.REGISTRY}/${env.IMAGE_NAME}:${env.TAG}"
                                
                            # Capture digest after successful Verification
                            DIGEST=\$(cosign triangulate "${env.REGISTRY}/${env.IMAGE_NAME}:${env.TAG}")
                            echo "✅ Verification succeeded: \${DIGEST}"
                        """
                    }
                }
            }
        }
    }
}

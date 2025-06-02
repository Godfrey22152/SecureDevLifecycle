pipeline {
    agent {label 'slave-1'}

    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
    }
    
    parameters {
        string(name: 'RELEASE_TYPE', description: 'Release type (e.g., prod, staging, dev)', defaultValue: 'dev')
    }
    environment {
        IMAGE_NAME = 'ghcr.io/godfrey22152/trainbook-app'
        TRIVY_TIMEOUT = '15m'
        GITHUB_CREDENTIALS_ID = 'git-cred' 
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

        stage('Docker Setup') {
            parallel {
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

                stage('Login to GitHub Container Registry (GHCR)') {
                    steps {
                        withCredentials([usernamePassword(
                            credentialsId: 'git-cred', 
                            passwordVariable: 'GITHUB_TOKEN', 
                            usernameVariable: 'GITHUB_USER'
                        )]) {
                            sh '''
                                echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GITHUB_USER" --password-stdin
                            '''
                        }
                    }
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
                                    --fail-on=critical  
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
                    sh 'cosign version'
                    
                    withCredentials([
                        file(credentialsId: 'cosign-private-key-file', variable: 'COSIGN_KEY_FILE'),
                        string(credentialsId: 'cosign-password', variable: 'COSIGN_PASSWORD')
                    ]) {
                        sh '''
                            set -e  # Exit immediately on error
                            echo "[Cosign] Signing ${IMAGE_NAME}:${TAG}"

                            # Sign image with digest instead of image tag 
                            DIGEST=\$(crane digest "${IMAGE_NAME}:${TAG}")
                            echo "$COSIGN_PASSWORD" | cosign sign \
                                --key "$COSIGN_KEY_FILE" \
                                --yes \
                                --recursive \
                                "${IMAGE_NAME}@\$DIGEST"
        
                            echo "✅ Signed Image with Digest: ${IMAGE_NAME}@\$DIGEST"
                        '''
                    }
                }
            }
        }

        stage('Verify Cosign Signature') {
            steps {
                script {
                    echo "[Cosign] Version Check"
                    sh 'cosign version'
        
                    withCredentials([file(credentialsId: 'cosign-public-key-file', variable: 'COSIGN_KEY_FILE')]) {
                        sh '''
                            set -e
                            echo "[Cosign] Resolving digest for ${IMAGE_NAME}:${TAG} using crane..."
                            DIGEST=$(crane digest "${IMAGE_NAME}:${TAG}")
                            IMAGE_REF="${IMAGE_NAME}@${DIGEST}"

                            echo "[Cosign] Verifying $IMAGE_REF"
                            cosign verify \
                                --key "$COSIGN_KEY_FILE" \
                                "$IMAGE_REF"
        
                            echo "✅ Verification succeeded: $IMAGE_REF"
                        '''
                    }
                }
            }
        }

        stage('Update Docker Image and Tag in the Deployment manifest file in Container-Security branch') {
            steps {
                script {
                    withCredentials([gitUsernamePassword(credentialsId: 'git-cred', gitToolName: 'Default')]) {
                        sh '''
                            # Git Clone Repository
                            git clone -b deployment --single-branch https://github.com/Godfrey22152/SecureDevLifecycle.git Manifests/ 
                            cd Manifests/Manifest_Files
                            git checkout deployment
                            
                            echo "Before update:"
                            cat trainbook-deployment.yaml

                            # Get image digest
                            DIGEST=$(crane digest "${IMAGE_NAME}:${TAG}")
                            IMAGE_WITH_DIGEST="${IMAGE_NAME}@${DIGEST}"
                            
                            # Use the absolute path for sed
                            sed -i "s|^[[:space:]]*image: .*|        image: ${IMAGE_WITH_DIGEST}|" trainbook-deployment.yaml
                            
                            echo "Updated Trainbook Manifest Deployment File Content:"
                            cat trainbook-deployment.yaml
                            
                            git config user.email "godfreyifeanyi45@gmail.com"
                            git config user.name "Godfrey22152"
                            
                            git add trainbook-deployment.yaml
                            git commit -m "Updated image to digest: ${IMAGE_WITH_DIGEST} by Jenkins" || echo "No changes to commit"
                            git push origin deployment
                        '''
                    }
                }
            }
        } 
    } 
}

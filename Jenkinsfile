pipeline {
    agent {label 'slave-1'}
    
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
                    sh '''
                        echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GITHUB_USER" --password-stdin
                    '''
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

                            # Capture image digest and Sign image with digest instead of image tag 
                            DIGEST=$(cosign triangulate "${IMAGE_NAME}:${TAG}")
                            echo "$COSIGN_PASSWORD" | cosign sign \
                                --key "$COSIGN_KEY_FILE" \
                                --yes \
                                --recursive \
                                "$DIGEST"
        
                            echo "✅ Signed image digest: $DIGEST"
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
                            echo "[Cosign] Verifying ${IMAGE_NAME}:${TAG}"
                            
                            DIGEST=$(cosign triangulate "${IMAGE_NAME}:${TAG}")
                            cosign verify \
                                --key "$COSIGN_KEY_FILE" \
                                "$DIGEST"
        
                            echo "✅ Verification succeeded: $DIGEST"
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
                            git clone -b container-security --single-branch https://github.com/Godfrey22152/SecureDevLifecycle.git Manifest_Files 
                            cd Manifest_Files
                            git checkout container-security
                            
                            # List files to confirm the presence of trainbook-deployment.yaml file in the Manifest_Files folder.
                            ls -l 

                            # Get the absolute path for the current directory
                            repo_dir=$(pwd)
                            
                            # Use the absolute path for sed
                            sed -i "s|^  image: .*|  image: ${IMAGE_NAME}:${TAG}|" "${repo_dir}/trainbook-deployment.yaml"
                        '''
                        
                        // Confirm the change
                        sh '''
                            echo "Updated YAML Manifest File Content:"
                            cat Manifest_Files/trainbook-deployment.yaml

                        '''
                        
                        // Configure Git for committing changes and pushing
                        sh '''
                            cd Manifest_Files   #Ensure you are inside the cloned repo. 
                            git config user.email "godfreyifeanyi45@gmail.com"
                            git config user.name "Godfrey22152"
                        '''
                        
                        // Commit and push Updated YAML file back to the repository
                        sh '''
                            cd Manifest_Files
                            ls
                            git add trainbook-deployment.yaml
                            git commit -m "Updated image tag to ${TAG} by Jenkins"
                            git push origin container-security
                        '''
                    }
                }
            }
        } 
    } 
}

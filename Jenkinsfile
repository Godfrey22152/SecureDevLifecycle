pipeline {
    agent any
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '15', daysToKeepStr: '', numToKeepStr: '2')
    }
    
    stages {
        stage('Run Child Pipelines in Parallel') {
            parallel {
                stage('Quality Assurance & Testing') {
                    steps {
                        build job: 'DevSecOps/Quality-Assurance-and-Automated-Testing', wait: true, propagate: true
                    }
                }
                stage('Container Security Scans') {
                    steps {
                        build job: 'DevSecOps/Container-Security-Scans', wait: true, propagate: true, parameters: [
                            string(name: 'RELEASE_TYPE', value: 'dev')
                        ]
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Both pipelines completed successfully!'
        }
        failure {
            echo '❌ One or more pipelines failed!'
        }
    }
}

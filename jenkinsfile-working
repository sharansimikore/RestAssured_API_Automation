pipeline {
    agent any

    environment {
        IMAGE_NAME = "sharan/api-automation"
        CONTAINER_NAME = "api-tests-${BUILD_NUMBER}"
        DOCKERHUB_REPO = "sharansimikore/api-automation"
    }

    stages {

    
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Run Tests in Docker') {
            steps {
                sh '''
                docker run --name $CONTAINER_NAME \
                -v $WORKSPACE/reports:/app/reports \
                $IMAGE_NAME
                '''
            }
        }

        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: 'reports/**', fingerprint: true, allowEmptyArchive: true
            }
        }

         stage('Publish Test Results') {
            steps {
                echo 'Publishing test results...'
                junit allowEmptyResults: true,
                      testResults: 'reports/**/TEST-*.xml'
            }
        }
        
       stage('Push to Docker Hub') {
            steps {
                script {
                    echo "Pushing to Docker Hub..."
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'USER',
                        passwordVariable: 'PASS'
                    )]) {
                        sh '''
                            echo $PASS | docker login -u $USER --password-stdin
                            docker tag $IMAGE_NAME ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                            docker tag $IMAGE_NAME ${DOCKERHUB_REPO}:latest
                            docker push ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                            docker push ${DOCKERHUB_REPO}:latest
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker rm -f $CONTAINER_NAME || true'
        }

        success {
            echo '✅ Pipeline completed successfully!'
            echo "Image pushed: ${DOCKERHUB_REPO}:${BUILD_NUMBER}"
        }
        failure {
            echo '❌ Pipeline failed!'
            // Archive logs on failure
            archiveArtifacts artifacts: 'reports/**', 
                             allowEmptyArchive: true
        }
    }

    
}

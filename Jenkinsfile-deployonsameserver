pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        IMAGE_NAME = "sharan/api-automation"
        CONTAINER_NAME = "api-tests-${BUILD_NUMBER}"
        DOCKERHUB_REPO = "sharansimikore/api-automation"
        DEPLOY_CONTAINER = "api-app"
    }

    stages {

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Run Tests in Docker') {
            steps {
                retry(2) {
                    sh '''
                    docker run --rm \
                    --name $CONTAINER_NAME \
                    -v $WORKSPACE/reports:/app/reports \
                    $IMAGE_NAME
                    '''
                }
            }
        }

        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: 'reports/**', fingerprint: true, allowEmptyArchive: true
            }
        }

        stage('Publish Test Results') {
            steps {
                junit allowEmptyResults: true,
                      testResults: 'reports/**/TEST-*.xml'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
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

        stage('Deploy on EC2 (Same Server)') {
            steps {
                sh '''
                echo "Deploying latest container..."

                docker pull ${DOCKERHUB_REPO}:latest

                docker stop ${DEPLOY_CONTAINER} || true
                docker rm ${DEPLOY_CONTAINER} || true

                docker run -d \
                --name ${DEPLOY_CONTAINER} \
                -p 8081:8080 \
                ${DOCKERHUB_REPO}:latest
                '''
            }
        }
    }

    post {
        always {
            cleanWs()
            sh 'docker system prune -f || true'
        }

        success {
            echo "✅ Deployment successful!"
        }

        failure {
            echo '❌ Pipeline failed!'
        }
    }
}

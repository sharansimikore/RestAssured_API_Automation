pipeline {
    agent any

    environment {
        IMAGE_NAME = "sharan/api-automation"
        TEST_CONTAINER_NAME = "api-tests-${BUILD_NUMBER}"
        DEPLOY_CONTAINER_NAME = "api-app" // persistent container
        DOCKERHUB_REPO = "sharansimikore/api-automation"
        REPORTS_DIR = "${WORKSPACE}/reports"
        DEPLOY_PORT = 8081
    }

    stages {
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Run API Tests in Docker') {
            steps {
                sh """
                mkdir -p $REPORTS_DIR
                docker run --name $TEST_CONTAINER_NAME \
                    -v $REPORTS_DIR:/app/target/surefire-reports \
                    $IMAGE_NAME
                """
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

        stage('Push Docker Image to Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'USER',
                        passwordVariable: 'PASS'
                    )]) {
                        sh """
                            echo \$PASS | docker login -u \$USER --password-stdin
                            docker tag $IMAGE_NAME ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                            docker tag $IMAGE_NAME ${DOCKERHUB_REPO}:latest
                            docker push ${DOCKERHUB_REPO}:${BUILD_NUMBER}
                            docker push ${DOCKERHUB_REPO}:latest
                        """
                    }
                }
            }
        }

        stage('Deploy to EC2 (Persistent Container)') {
            steps {
                sh """
                # Stop and remove old deployment container if exists
                docker stop $DEPLOY_CONTAINER_NAME || true
                docker rm $DEPLOY_CONTAINER_NAME || true

                # Run new container in detached mode, keep alive
                docker run -d --name $DEPLOY_CONTAINER_NAME -p $DEPLOY_PORT:8081 $IMAGE_NAME tail -f /dev/null
                """
            }
        }
    }

    post {
        always {
            // Remove only test containers
            sh 'docker rm -f $TEST_CONTAINER_NAME || true'
        }

        success {
            echo "✅ Pipeline completed successfully! Docker image pushed and deployed."
            echo "🌐 Access your API container on port $DEPLOY_PORT"
        }

        failure {
            echo "❌ Pipeline failed!"
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
        }
    }
}

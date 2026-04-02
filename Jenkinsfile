pipeline {
    agent any

    environment {
        IMAGE_NAME = "sharan/api-automation"
        CONTAINER_NAME = "api-app" // deploy container
        DOCKERHUB_REPO = "sharansimikore/api-automation"
        REPORTS_DIR = "${WORKSPACE}/reports"
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
                docker run --name api-tests-${BUILD_NUMBER} \
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

        stage('Deploy to EC2 (Same Server)') {
            steps {
                sh """
                # Stop old container if exists
                docker stop $CONTAINER_NAME || true
                docker rm $CONTAINER_NAME || true

                # Run new container with mapped port (8081)
                docker run -d --name $CONTAINER_NAME -p 8081:8080 $IMAGE_NAME
                """
            }
        }
    }

    post {
        always {
            // Clean up test containers only
            sh 'docker rm -f api-tests-${BUILD_NUMBER} || true'
        }

        success {
            echo "✅ Pipeline completed successfully! Docker image pushed and deployed."
        }

        failure {
            echo "❌ Pipeline failed!"
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
        }
    }
}

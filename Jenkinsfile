pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'ENV', choices: ['dev', 'qa', 'prod'], description: 'Deployment Environment')
    }

    environment {
        IMAGE_NAME = "sharan/api-automation"
        DOCKERHUB_REPO = "sharansimikore/api-automation"
        TEST_CONTAINER_NAME = "api-tests-${BUILD_NUMBER}"
        DEPLOY_CONTAINER_NAME = "api-app-${ENV}"
        REPORTS_DIR = "${WORKSPACE}/reports"
        DEPLOY_PORT = "8081"
        TAG = "${BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    }

    triggers {
        githubPush()
    }

    stages {

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    retry(2) {
                        sh "docker build -t $IMAGE_NAME ."
                    }
                }
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
            post {
                unsuccessful {
                    error("❌ Tests failed. Stopping pipeline.")
                }
            }
        }

        stage('Publish Test Results') {
            steps {
                junit allowEmptyResults: true,
                      testResults: 'reports/**/TEST-*.xml'
            }
        }

        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: 'reports/**',
                                 fingerprint: true,
                                 allowEmptyArchive: true
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    sh 'mvn sonar:sonar'
                }
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
                        docker tag $IMAGE_NAME ${DOCKERHUB_REPO}:${TAG}
                        docker tag $IMAGE_NAME ${DOCKERHUB_REPO}:latest
                        docker push ${DOCKERHUB_REPO}:${TAG}
                        docker push ${DOCKERHUB_REPO}:latest
                        """
                    }
                }
            }
        }

        stage('Deploy to EC2 (Pull & Run)') {
            steps {
                sh """
                docker pull ${DOCKERHUB_REPO}:latest

                docker stop $DEPLOY_CONTAINER_NAME || true
                docker rm $DEPLOY_CONTAINER_NAME || true

                docker run -d \
                    --name $DEPLOY_CONTAINER_NAME \
                    -p $DEPLOY_PORT:8081 \
                    ${DOCKERHUB_REPO}:latest
                """
            }
        }

        stage('Health Check') {
            steps {
                sh """
                sleep 10
                curl -f http://localhost:$DEPLOY_PORT || exit 1
                """
            }
        }
    }

    post {
        always {
            sh 'docker rm -f $TEST_CONTAINER_NAME || true'
        }

        success {
            echo "✅ Pipeline SUCCESS"
            echo "🌐 App running on port $DEPLOY_PORT"
        }

        failure {
            echo "❌ Pipeline FAILED"
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
        }
    }
}

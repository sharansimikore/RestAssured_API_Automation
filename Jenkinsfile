pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        IMAGE_NAME = "sharan/api-automation"
        DOCKERHUB_REPO = "sharansimikore/api-automation"
        TEST_CONTAINER_NAME = "api-tests-${BUILD_NUMBER}"
        REPORTS_DIR = "${WORKSPACE}/reports"
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

        stage('Publish & Archive Reports') {
            steps {
                junit allowEmptyResults: true, testResults: 'reports/**/TEST-*.xml'
                archiveArtifacts artifacts: 'reports/**', fingerprint: true, allowEmptyArchive: true
            }
        }

                stage('Checkstyle Analysis') {
            steps{
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage("Sonar Code Analysis") {
            environment {
                scannerHome = tool 'sonar6.2'
            }
            steps {
              withSonarQubeEnv('sonarserver') {
                sh '''${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=vprofile \
                   -Dsonar.projectName=vprofile \
                   -Dsonar.projectVersion=1.0 \
                   -Dsonar.sources=src/ \
                   -Dsonar.java.binaries=target/test-classes/com/visualpathit/account/controllerTest/ \
                   -Dsonar.junit.reportsPath=target/surefire-reports/ \
                   -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                   -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml'''
              }
            }
        }



        stage('Push to Registry') {
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

        stage('Deploy to Dev') {
            environment {
                DEPLOY_CONTAINER_NAME = "api-app-dev"
                DEPLOY_PORT = "8081"
            }
            steps {
                sh """
                docker pull ${DOCKERHUB_REPO}:latest
                docker stop $DEPLOY_CONTAINER_NAME || true
                docker rm $DEPLOY_CONTAINER_NAME || true
                docker run -d --name $DEPLOY_CONTAINER_NAME -p $DEPLOY_PORT:8081 ${DOCKERHUB_REPO}:latest
                """
            }
        }

        stage('Deploy to QA') {
            environment {
                DEPLOY_CONTAINER_NAME = "api-app-qa"
                DEPLOY_PORT = "8082"
            }
            steps {
                sh """
                docker pull ${DOCKERHUB_REPO}:latest
                docker stop $DEPLOY_CONTAINER_NAME || true
                docker rm $DEPLOY_CONTAINER_NAME || true
                docker run -d --name $DEPLOY_CONTAINER_NAME -p $DEPLOY_PORT:8081 ${DOCKERHUB_REPO}:latest
                """
            }
        }

        stage('Deploy to Prod') {
            environment {
                DEPLOY_CONTAINER_NAME = "api-app-prod"
                DEPLOY_PORT = "8083"
            }
            steps {
                input message: 'Approve deployment to Production?', ok: 'Deploy!', submitter: 'admin'
                sh """
                docker pull ${DOCKERHUB_REPO}:latest
                docker stop $DEPLOY_CONTAINER_NAME || true
                docker rm $DEPLOY_CONTAINER_NAME || true
                docker run -d --name $DEPLOY_CONTAINER_NAME -p $DEPLOY_PORT:8081 ${DOCKERHUB_REPO}:latest
                """
            }
        }
    }   // <-- Closes 'stages' block

    post {
        always {
            sh 'docker rm -f $TEST_CONTAINER_NAME || true'
        }
        success {
            echo "✅ Pipeline SUCCESS"
        }
        failure {
            echo "❌ Pipeline FAILED..."
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
        }
    }
}   // <-- Closes 'pipeline' block

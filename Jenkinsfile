pipeline {
    agent any

    environment {
        IMAGE_NAME = "sharan/api-automation"
        CONTAINER_NAME = "api-tests-${BUILD_NUMBER}"
    }

    stages {

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

       
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
                archiveArtifacts artifacts: 'reports/**', fingerprint: true
            }
        }

        stage('Push to Docker Hub') {
    steps {
        sh '''
        docker tag $IMAGE_NAME sharan/api-automation
        docker push sharansimikore/api-automation
        '''
    }
}
    }

    post {
        always {
            sh 'docker rm -f $CONTAINER_NAME || true'
        }
    }
}

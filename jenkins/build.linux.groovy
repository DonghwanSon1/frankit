pipeline {
    agent any

    stages {
        stage('git clone') {
            steps {
                git branch: 'main', credentialsId: 'gitHub-token', url: 'https://github.com/DonghwanSon1/frankit.git'
            }
        }

        stage('Docker Container Stop And Remove') {
            steps {
                sh 'docker rm -f frankit-service'
            }
        }

        stage('Clean and Build') {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('Build and Run with Docker Compose') {
            steps {
                dir('./frankit') {
                    sh 'docker-compose up --build -d'
                }
            }
        }

        stage('Image Remove') {
            steps {
                sh 'docker image prune -f'
            }
        }
    }
}

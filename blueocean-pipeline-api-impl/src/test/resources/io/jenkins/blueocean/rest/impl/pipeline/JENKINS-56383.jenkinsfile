
pipeline {
  agent any
  stages {
    stage('Top1') {
      parallel {
        stage ('TOP1-P1') {
          steps {
            echo "TEST"
          }
        }
        
        stage('TOP1-P2') {
          steps {
            echo "TEST"
          }
        }
      }
    }
    stage ('TOP2') {
      parallel {
        stage('TOP2-P1') {
          steps {
            echo "TEST"
          }
        }
        stage('TOP2-P2') {
          steps {
            echo "TEST"
          }
        }
      }
    }
    stage('TOP3') {
      steps {
        echo "TEST"
      }
    }
  }
}

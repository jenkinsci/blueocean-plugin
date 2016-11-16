pipeline {
  agent:docker "cloudbees/java-build-tools"
  stages {
    stage('build') {
      steps {
        sh 'mvn clean install' 
      }
    }
    post {
      archive '**/*.hpi'
      junit '**/*.xml'
    }
  }
}

pipeline {
  agent docker:'cloudbees/java-build-tools'
  stages {
    stage('build') {
      steps {
        sh 'mvn clean install' 
      }
    }
  }
  postBuild {
    success {
      archive 'target/blueocean-pipeline-editor.hpi'
    }
    always {
      junit 'target/**/*.xml'
    }
  }
}

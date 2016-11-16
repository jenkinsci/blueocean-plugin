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
    always {
      archive '**/blueocean-pipeline-editor.hpi'
      junit '**/*.xml'
    }
  }
}

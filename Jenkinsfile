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
      archive 'target/blueocean-pipeline-editor.hpi'
      junit '**/*.xml'
    }
  }
}

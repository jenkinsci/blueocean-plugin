node {
  docker.image('cloudbees/java-build-tools').inside {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
      writeFile file: 'settings.xml', text: "<settings><localRepository>${pwd()}/.m2repo</localRepository></settings>"
      sh "mvn clean install -s settings.xml -B -DcleanNode -Dmaven.test.failure.ignore"
      step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
      step([$class: 'ArtifactArchiver', artifacts: '*/target/*.hpi'])
    }
  }
}

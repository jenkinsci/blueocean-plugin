node {
  deleteDir()
  checkout scm
  
  docker.image('cloudbees/java-build-tools').inside {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
      try {
        sh "mvn clean install -B -DcleanNode -Dmaven.test.failure.ignore"
        step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
        step([$class: 'ArtifactArchiver', artifacts: '*/target/*.hpi'])
      } catch(err) {
        currentBuild.result = "FAILURE"
      } finally {
        sendhipchat()
        deleteDir()
      }
    }
  }
}

def sendhipchat() {
    res = currentBuild.result
    if(currentBuild.result == null) {
      res = "SUCCESS"
    }
    message = "${env.JOB_NAME} #${env.BUILD_NUMBER}, status: ${res} (<a href='${currentBuild.absoluteUrl}'>Open</a>)"
    color = null
    if(currentBuild.result == "UNSTABLE") {
        color = "YELLOW"
    } else if(currentBuild.result == "SUCCESS" || currentBuild.result == null){
        color = "GREEN"
    } else if(currentBuild.result == "FAILURE") {
        color = "RED"
    }
    if(color != null) {
        hipchatSend message: message, color: color
    }
}


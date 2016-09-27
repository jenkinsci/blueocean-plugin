node {
  deleteDir()
  checkout scm

  docker.image('cloudbees/java-build-tools').inside {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
      try {
        sh "mvn clean install -B -DcleanNode -Dmaven.test.failure.ignore"
        sh "node checkdeps.js"
        step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
        step([$class: 'ArtifactArchiver', artifacts: '*/target/*.hpi'])

        // Trigger the ATH, but don't wait for it.
        def postBuildResult = currentBuild.result;
        try {
          echo "Will attempt to run the ATH with the same branch name i.e. '${env.BRANCH_NAME}'.".
          build (job: "ATH-Jenkinsfile/${env.BRANCH_NAME}", parameters: [string(name: 'BLUEOCEAN_BRANCH_NAME', value: "${env.BRANCH_NAME}")], wait: false)
        } catch (Exception e) {
          echo "Failed to run the ATH with the same branch name i.e. '${env.BRANCH_NAME}'. Will try running the ATH 'master' branch.".
          build (job: 'ATH-Jenkinsfile/master', parameters: [string(name: 'BLUEOCEAN_BRANCH_NAME', value: "${env.BRANCH_NAME}")], wait: false)
          // Reset the build status in case the first attempt to build the branch failed.
          currentBuild.result = postBuildResult;
        }

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

#!groovy

// only 20 builds
properties([buildDiscarder(logRotator(artifactNumToKeepStr: '20', numToKeepStr: '20'))])

node {

  deleteDir()
  checkout scm
  sh 'docker build -t blueocean_build_env --build-arg GID=$(id -g ${USER}) --build-arg UID=$(id -u ${USER}) - < Dockerfile.build'

  configFileProvider([configFile(fileId: 'blueocean-maven-settings', targetLocation: 'settings.xml')]) {

  docker.image('blueocean_build_env').inside {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
      try {
        sh 'npm --prefix ./blueocean-core-js install'
        sh 'npm --prefix ./blueocean-core-js run gulp'
        sh "mvn clean install -B -DcleanNode -Dmaven.test.failure.ignore -s settings.xml -Dmaven.artifact.threads=30"
        sh "node ./bin/checkdeps.js"
        sh "node ./bin/checkshrinkwrap.js"
        step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
        step([$class: 'ArtifactArchiver', artifacts: '*/target/*.hpi'])

        triggerATH();
      } catch(err) {
        if (err.toString().contains('AbortException')) {
            currentBuild.result = "ABORTED"
        } else {
            currentBuild.result = "FAILURE"
        }
      } finally {
        sendhipchat()
        deleteDir()
      }
    }
  }

  }
}

def triggerATH() {
    // Assemble and archive the HPI plugins that the ATH should use.
    // The ATH build can copy this artifact and use it, saving the time it
    // would otherwise spend building and assembling again.
    sh 'cd blueocean && tar -czvf target/ath-plugins.tar.gz target/plugins'
    archiveArtifacts artifacts: 'blueocean/target/ath-plugins.tar.gz'

    // Trigger the ATH, but don't wait for it.
    try {
        echo "Will attempt to run the ATH with the same branch name i.e. '${env.BRANCH_NAME}'."
        build(job: "ATH-Jenkinsfile/${env.BRANCH_NAME}",
                parameters: [string(name: 'BLUEOCEAN_BRANCH_NAME', value: "${env.BRANCH_NAME}"),
                             string(name: 'BUILD_NUM', value: "${env.BUILD_NUMBER}")],
                wait: false)
    } catch (e1) {
        echo "Failed to run the ATH with the same branch name i.e. '${env.BRANCH_NAME}'. Will try running the ATH 'master' branch."
        build(job: "ATH-Jenkinsfile/master",
                parameters: [string(name: 'BLUEOCEAN_BRANCH_NAME', value: "${env.BRANCH_NAME}"),
                             string(name: 'BUILD_NUM', value: "${env.BUILD_NUMBER}")],
                wait: false)
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
    } else if(currentBuild.result == "FAILURE" || currentBuild.result == "ABORTED") {
        color = "RED"
    }
    if(color != null) {
        hipchatSend message: message, color: color
    }
}

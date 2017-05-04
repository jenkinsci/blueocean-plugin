#!groovy

// only 20 builds
properties([buildDiscarder(logRotator(artifactNumToKeepStr: '20', numToKeepStr: '20'))])

node {
  stage 'Setup'
  deleteDir()
  checkout scm
  sh 'docker build -t blueocean_build_env --build-arg GID=$(id -g ${USER}) --build-arg UID=$(id -u ${USER}) - < Dockerfile.build'

  configFileProvider([configFile(fileId: 'blueocean-maven-settings', targetLocation: 'settings.xml')]) {

  sh "./acceptance-tests/runner/scripts/start-selenium.sh"

  docker.image('blueocean_build_env').inside("--net=container:blueo-selenium") {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
      try {
        stage 'Building BlueOcean'
        sh 'npm --prefix ./blueocean-core-js install'
        sh 'npm --prefix ./blueocean-core-js run gulp'
        sh "mvn clean install -B -DcleanNode -Dmaven.test.failure.ignore -s settings.xml -Dmaven.artifact.threads=30"
        step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
        step([$class: 'ArtifactArchiver', artifacts: '*/target/*.hpi'])
      
        stage 'Sanity check dependancies'
        sh "node ./bin/checkdeps.js"
        stage 'Sanity check shrinkwrap'
        sh "node ./bin/checkshrinkwrap.js"
        stage 'Archive results'
        
        stage 'ATH'
        sh 'cd acceptance-tests && npm install'
        sh "cd acceptance-tests && ./run.sh -a=../blueocean/ --no-selenium"
        step([$class: 'JUnitResultArchiver', testResults: 'acceptance-tests/target/surefire-reports/*.xml'])
        
      } catch(err) {
        currentBuild.result = "FAILURE"

        if (err.toString().contains('AbortException')) {
            currentBuild.result = "ABORTED"
        }
      } finally {
        sendhipchat()
        sh "./acceptance-tests/runner/scripts/stop-selenium.sh"
        deleteDir()
      }
    }
  }

  }
}


def sendhipchat() {
    res = currentBuild.result
    if(currentBuild.result == null) {
      res = "SUCCESS"
    }
    message = "${env.JOB_NAME} #${env.BUILD_NUMBER}, status: ${res} (<a href='${env.RUN_DISPLAY_URL}'>Open</a>)"
    color = null
    if(currentBuild.result == "UNSTABLE") {
        color = "YELLOW"
    } else if(currentBuild.result == "SUCCESS" || currentBuild.result == null){
        color = "GREEN"
    } else if(currentBuild.result == "FAILURE") {
        color = "RED"
    } else if(currentBuild.result == "ABORTED") {
        color = "GRAY"
    }
    if(color != null) {
        hipchatSend message: message, color: color
    }
}

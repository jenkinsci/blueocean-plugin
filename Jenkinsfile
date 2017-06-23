#!groovy

// only 20 builds
properties([buildDiscarder(logRotator(artifactNumToKeepStr: '20', numToKeepStr: '20'))])

node() {
  stage('Setup') {
    deleteDir()
    checkout scm
    sh 'docker build -t blueocean_build_env --build-arg GID=$(id -g ${USER}) --build-arg UID=$(id -u ${USER}) - < Dockerfile.build'
    withCredentials([file(credentialsId: 'blueoceandeploy_ath', variable: 'FILE')]) {
      sh 'mv $FILE acceptance-tests/live.properties'
    }
    configFileProvider([configFile(fileId: 'blueocean-maven-settings', variable: 'MAVEN_SETTINGS')]) {
      sh 'mv $MAVEN_SETTINGS settings.xml'
    }
    sh "./acceptance-tests/runner/scripts/start-selenium.sh"
  }

  docker.image('blueocean_build_env').inside("--net=container:blueo-selenium") {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
      try {
        stage('Building BlueOcean') {
          sh 'npm --prefix ./blueocean-core-js install'
          sh 'npm --prefix ./blueocean-core-js run gulp'
          sh "mvn clean install -B -DcleanNode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.test.failure.ignore -s settings.xml -Dmaven.artifact.threads=30"
          junit '**/target/surefire-reports/TEST-*.xml'
          junit '**/reports/junit.xml'
          archive '*/target/*.hpi'
        }

        stage('Sanity check dependencies') {
          sh "node ./bin/checkdeps.js"
          sh "node ./bin/checkshrinkwrap.js"
        }

        stage('ATH - Jenkins 2.7.3') {
          sh "cd acceptance-tests && ./run.sh --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
          junit 'acceptance-tests/target/surefire-reports/*.xml'
          archive 'acceptance-tests/target/screenshots/*'
        }
        if (env.JOB_NAME =~ 'blueocean-weekly-ath') {
          stage('ATH - Jenkins 2.46.3') {
            sh "cd acceptance-tests && ./run.sh -v=2.46.3 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
            junit 'acceptance-tests/target/surefire-reports/*.xml'
          }

          stage('ATH - Jenkins 2.32.3') {
            sh "cd acceptance-tests && ./run.sh -v=2.32.3 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
            junit 'acceptance-tests/target/surefire-reports/*.xml'
          }

          stage('ATH - Jenkins 2.19.4') {
            sh "cd acceptance-tests && ./run.sh -v=2.19.4 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
            junit 'acceptance-tests/target/surefire-reports/*.xml'
          }
        }


      } catch(err) {
        currentBuild.result = "FAILURE"

        if (err.toString().contains('exit code 143')) {
          currentBuild.result = "ABORTED"
        }
      } finally {
        stage('Cleanup') {
          sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-selenium.sh"
          sendhipchat()
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

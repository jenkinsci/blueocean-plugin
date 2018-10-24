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
    withCredentials([file(credentialsId: 'blueocean-ath-private-repo-key', variable: 'FILE')]) {
      sh 'mv $FILE acceptance-tests/bo-ath.key'
    }
    sh "./acceptance-tests/runner/scripts/start-selenium.sh"
    sh "./acceptance-tests/runner/scripts/start-bitbucket-server.sh"
  }

  docker.image('blueocean_build_env').inside("--net=container:blueo-selenium") {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
      try {
        stage('Sanity check dependencies') {
          sh "node ./bin/checkdeps.js"
          sh "node ./bin/checkshrinkwrap.js"
        }

        stage('Building JS Libraries') {
          sh 'node -v && npm -v'
          sh 'npm --prefix ./js-extensions run build'
        }

        stage('Building BlueOcean') {
          timeout(time: 90, unit: 'MINUTES') {
            sh "mvn clean install -V -B -DcleanNode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.test.failure.ignore -s settings.xml -Dmaven.artifact.threads=30"
          }

          junit '**/target/surefire-reports/TEST-*.xml'
          junit '**/target/jest-reports/*.xml'
          jacoco execPattern: '**/target/jacoco.exec', classPattern : '**/target/classes', sourcePattern: '**/src/main/java', exclusionPattern: 'src/test*'
          archive '*/target/code-coverage/**/*'
          archive '*/target/*.hpi'
          archive '*/target/jest-coverage/**/*'
        }

        stage('ATH - Jenkins 2.121.1') {
          timeout(time: 90, unit: 'MINUTES') {
            sh "cd acceptance-tests && ./run.sh -v=2.121.1 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
            junit 'acceptance-tests/target/surefire-reports/*.xml'
            archive 'acceptance-tests/target/screenshots/**/*'
          }
        }

        if (env.JOB_NAME =~ 'blueocean-weekly-ath') {
          stage('ATH - Jenkins 2.73.2') {
            sh "cd acceptance-tests && ./run.sh -v=2.73.2 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
            junit 'acceptance-tests/target/surefire-reports/*.xml'
          }
          stage('ATH - Jenkins 2.73.3') {
            sh "cd acceptance-tests && ./run.sh -v=2.73.3 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
            junit 'acceptance-tests/target/surefire-reports/*.xml'
          }
          stage('ATH - Jenkins 2.107.2') {
            sh "cd acceptance-tests && ./run.sh -v=2.107.2 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
            junit 'acceptance-tests/target/surefire-reports/*.xml'
          }
          stage('ATH - Jenkins 2.121.1') {
            sh "cd acceptance-tests && ./run.sh -v=2.121.1 --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
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
          sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-bitbucket-server.sh"
          deleteDir()
        }
      }
    }
  }
}


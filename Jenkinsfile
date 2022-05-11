#!groovy

if (JENKINS_URL == 'https://ci.jenkins.io/') {
  buildPlugin(
    configurations: [
      [ platform: "linux", jdk: "8" ],
      [ platform: "linux", jdk: "11" ]
    ],
    // Tests were locking up and timing out on non-aci
    useAci: true,
    timeout: 90
  )
  return
}

properties([
  // only 20 builds,
  buildDiscarder(logRotator(artifactNumToKeepStr: '20', numToKeepStr: '20')),
])

credentials = [
  file(credentialsId: 'blueoceandeploy_ath', variable: 'LIVE_PROPERTIES_FILE'),
  file(credentialsId: 'blueocean-ath-private-repo-key', variable: 'BO_ATH_KEY_FILE')
]

envs = [
  'GIT_COMMITTER_EMAIL=me@hatescake.com',
  'GIT_COMMITTER_NAME=Hates',
  'GIT_AUTHOR_NAME=Cake',
  'GIT_AUTHOR_EMAIL=hates@cake.com'
]

jenkinsVersions = ['2.277.4']

node() {
  withCredentials(credentials) {
    withEnv(envs) {

      stage('Setup') {
        deleteDir()
        checkout scm
        //sh 'docker build -t blueocean_build_env --build-arg GID=$(id -g ${USER}) --build-arg UID=$(id -u ${USER}) - < Dockerfile.build'
        sh 'mv $LIVE_PROPERTIES_FILE acceptance-tests/live.properties'
        configFileProvider([configFile(fileId: 'blueocean-maven-settings', variable: 'MAVEN_SETTINGS')]) {
          sh 'mv $MAVEN_SETTINGS settings.xml'
        }
        sh 'mv $BO_ATH_KEY_FILE acceptance-tests/bo-ath.key'
        sh "./acceptance-tests/runner/scripts/start-selenium.sh"
        sh "./acceptance-tests/runner/scripts/start-bitbucket-server.sh"
      }

      try {
        docker.image('blueocean/blueocean:build_env').inside("--net=container:blueo-selenium") {
          ip = sh(returnStdout: true, script: "hostname -I  | awk '{print \$1}'").trim()
          echo "IP: [${ip}]"
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
              try {
                sh "mvn clean install -T1C -V -B -DcleanNode -ntp -Dmaven.test.failure.ignore -s settings.xml -e -Dmaven.artifact.threads=30"
              } finally {
                junit testResults: '**/target/surefire-reports/TEST-*.xml', allowEmptyResults: true
                junit testResults: '**/target/jest-reports/*.xml', allowEmptyResults: true
                archiveArtifacts artifacts: '*/target/*.hpi',allowEmptyArchive: true
              }
            }
          }

          jenkinsVersions.each { version ->
            stage("ATH - Jenkins ${version}") {
              timeout(time: 150, unit: 'MINUTES') {
                dir('acceptance-tests') {
                  sh "bash -x ./run.sh -v=${version} --host=${ip} --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
                  junit '**/target/surefire-reports/*.xml'
                  archive '**/target/screenshots/**/*'
                }
              }
            }
          }
        }
      } finally {
        stage('Cleanup') {
          catchError(message: 'Suppressing error in Stage: Cleanup') {
            sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-selenium.sh"
            sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-bitbucket-server.sh"
            deleteDir()
          }
        }
      }
    }
  }
}


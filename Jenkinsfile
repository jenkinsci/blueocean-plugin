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
  parameters([
    booleanParam(name: 'USE_SAUCELABS', defaultValue: false)
  ])
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

jenkinsVersions = ['2.176.4']

if (params.USE_SAUCELABS) {
  credentials.add(usernamePassword(credentialsId: 'saucelabs', passwordVariable: 'SAUCE_ACCESS_KEY', usernameVariable: 'SAUCE_USERNAME'))
  withCredentials([usernamePassword(credentialsId: 'saucelabs', passwordVariable: 'SAUCE_ACCESS_KEY', usernameVariable: 'SAUCE_USERNAME')]) {
    envs.add("webDriverUrl=https://${env.SAUCE_USERNAME}:${env.SAUCE_ACCESS_KEY}@ondemand.saucelabs.com/wd/hub")
  }
  envs.add("saucelabs=true")
  envs.add("TUNNEL_IDENTIFIER=${env.BUILD_TAG}")
}

node() {
  withCredentials(credentials) {
    withEnv(envs) {

      stage('Setup') {
        deleteDir()
        checkout scm
        sh 'docker build -t blueocean_build_env --build-arg GID=$(id -g ${USER}) --build-arg UID=$(id -u ${USER}) - < Dockerfile.build'
        sh 'mv $LIVE_PROPERTIES_FILE acceptance-tests/live.properties'
        configFileProvider([configFile(fileId: 'blueocean-maven-settings', variable: 'MAVEN_SETTINGS')]) {
          sh 'mv $MAVEN_SETTINGS settings.xml'
        }
        sh 'mv $BO_ATH_KEY_FILE acceptance-tests/bo-ath.key'
        if (params.USE_SAUCELABS) {
          sh "./acceptance-tests/runner/scripts/start-sc.sh"
        } else {
          sh "./acceptance-tests/runner/scripts/start-selenium.sh"
        }
        sh "./acceptance-tests/runner/scripts/start-bitbucket-server.sh"
      }

      try {
        docker.image('blueocean_build_env').inside("--net=container:blueo-selenium") {
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
              sh "mvn clean install -V -B -DcleanNode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.test.failure.ignore -s settings.xml -Dmaven.artifact.threads=30"
            }

            junit '**/target/surefire-reports/TEST-*.xml'
            junit '**/target/jest-reports/*.xml'
            archive '*/target/*.hpi'
          }

          jenkinsVersions.each { version ->
            stage("ATH - Jenkins ${version}") {
              timeout(time: 90, unit: 'MINUTES') {
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
            if (params.USE_SAUCELABS) {
              sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-sc.sh"
            } else {
              sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-selenium.sh"
            }
            sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-bitbucket-server.sh"
            deleteDir()
          }
        }
      }
    }
  }
}


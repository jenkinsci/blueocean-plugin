#!groovy

// only 20 builds
properties([buildDiscarder(logRotator(artifactNumToKeepStr: '20', numToKeepStr: '20'))])



stage("Begin") {
  echo "Blue ocean build"
}

try {
  stage("Build and test") {
    parallel( 
        "Build and Unit" : {
          buildAndTest()
        },
        "ATH" : {
          acceptanceTests()
        }            
    )    
  }  
} finally {
  stage("Notifying") {
      notifications()
  }
  
}

def buildAndTest() {
  node {    
    prepareEnvironment()    
    docker.image('blueocean_build_env').inside {
      withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
        try {
          fullBuildSteps()        
          runWeeklyPermutationATH()
        } catch(err) {
          handleBuildFailure(err)
        } finally {                      
          deleteDir()          
        }
      }
    }
  }
}

def acceptanceTests() {
  node {    
    prepareEnvironment()
    sh "./acceptance-tests/runner/scripts/start-selenium.sh"    

    docker.image('blueocean_build_env').inside("--net=container:blueo-selenium") {
      withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
        try {
          sh "mvn install -DskipTests"
          runAcceptanceTests()          
        } catch(err) {
          handleBuildFailure(err)
        } finally {
          sh "${env.WORKSPACE}/acceptance-tests/runner/scripts/stop-selenium.sh"
          deleteDir()          
        }
      }
    }
  }
}



def fullBuildSteps() {
  sh 'npm --prefix ./blueocean-core-js install'
  sh 'npm --prefix ./blueocean-core-js run gulp'
  sh "mvn install -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.test.failure.ignore -s settings.xml"
  junit '**/target/surefire-reports/TEST-*.xml'
  junit '**/reports/junit.xml'
  archive '*/target/*.hpi'
  
  sh "node ./bin/checkdeps.js"
  sh "node ./bin/checkshrinkwrap.js"

}

def runAcceptanceTests() {
  sh "cd acceptance-tests && ./run.sh --no-selenium --settings='-s ${env.WORKSPACE}/settings.xml'"
  junit 'acceptance-tests/target/surefire-reports/*.xml'
  archive 'acceptance-tests/target/screenshots/*'
}

def runWeeklyPermutationATH() {
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

}

/**
 * clean checkout, set some properties/files for running live tests, maven settings and more
 */
def prepareEnvironment() {
    deleteDir()
    checkout scm
    sh 'docker build -t blueocean_build_env --build-arg GID=$(id -g ${USER}) --build-arg UID=$(id -u ${USER}) - < Dockerfile.build'
    withCredentials([file(credentialsId: 'blueoceandeploy_ath', variable: 'FILE')]) {
      sh 'mv $FILE acceptance-tests/live.properties'
    }
    configFileProvider([configFile(fileId: 'blueocean-maven-settings', variable: 'MAVEN_SETTINGS')]) {
      sh 'mv $MAVEN_SETTINGS settings.xml'
    }
}

def handleBuildFailure(err) {
  currentBuild.result = "FAILURE"
  if (err.toString().contains('exit code 143')) {
    currentBuild.result = "ABORTED"
  }
}


/**
 * Junk for notification
 */
def notifications() {
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

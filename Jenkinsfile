#!groovy

/**
 * Blue Ocean can be build as a regular maven project, 
 * However, for easy of build reproducibility we are running inside docker.
 */
node("docker") {
  deleteDir()  
  checkout scm    
  docker.image('cloudbees/java-build-tools').inside {
    withEnv(['GIT_COMMITTER_EMAIL=me@hatescake.com','GIT_COMMITTER_NAME=Hates','GIT_AUTHOR_NAME=Cake','GIT_AUTHOR_EMAIL=hates@cake.com']) {
        sh "mvn clean install -B -DcleanNode -Dmaven.test.failure.ignore"
        step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
        archive '*/target/*.hpi'
        deleteDir()
      }
    }    
  }
}

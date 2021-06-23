pipeline{
  agent any
  stages {
    stage ('Sequential') {
      stages {
        stage("Sequential 1 with child in it"){
          steps{
            build "TestJob"
          }
        }
        stage("Sequential 2 with child in it"){
          steps{
            build "TestJob"
          }
        }
        stage("Sequential 3 with child in it using Script"){
          steps{
            build "TestJob"
            sh "exit 1"
          }
        }
        stage('Sequential 4 w/o childs'){
          steps{
            echo "E-mail"
          }
        }
      }
    }
  }
}

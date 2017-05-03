pipeline {
  agent any
  stages {
    stage ('Stage 1'){
      steps{
           sh 'sleep 6; echo `date` Stage 1;'
           sh 'sleep 6; echo `date` Stage 1;'
         }
      }

      stage ('fin'){
        steps{
            sh 'echo `date` fin;sleep 6; echo `date` fin;'
            sh 'echo yeah > foo.txt'
            archiveArtifacts 'foo.txt'
        }
      }
  }

}

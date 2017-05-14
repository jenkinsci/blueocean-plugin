node {
    stage('first') {
      echo 'first step'
      sh 'sleep 1; echo `date` first;'
      echo 'first step end'
      echo 'Second coming up'
      sh 'sleep 1; echo `date` second;'
      echo '9th'      
    }
    stage('second') {
      sh 'sleep 1; echo `date`;'
      echo '10th'
      sh 'sleep 1; echo `date`;'
      echo 'and we are finished'
    }
    stage('final') {
        sh 'echo end; error 1'
    }    
}

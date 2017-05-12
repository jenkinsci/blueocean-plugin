node {
  
  stage ('Build1') {
      sh 'ping -c 1 localhost'
  }
  stage ('Build2') {
      sh 'ping -c 1 localhost'
  }
}

node {
  sh 'ping -c 5 www.spiegel.de || true'
  stage ('Build1') {
      sh 'ping -c 5 www.spiegel.de || true'
  }
  stage ('Build2') {
      sh 'ping -c 5 www.spiegel.de || true'
  }
}

docker.image("cloudbees/java-build-tools").inside {
    checkout scm
    writeFile file: 'settings.xml', text: "<settings><localRepository>${pwd()}/.m2repo</localRepository></settings>"
    sh 'git config --global user.email "you@example.com" && git config --global user.name "Your Name"'
    sh "mvn clean install -s settings.xml -B -DcleanNode -Dmaven.test.failure.ignore"    
    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}

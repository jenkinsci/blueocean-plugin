node {
   env.JAVA_HOME="${tool 'jdk8'}"
   env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
   sh 'java -version'
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Get some code from a GitHub repository
   checkout scm
   // Get the maven tool.
   // ** NOTE: This 'M3' maven tool must be configured
   // **       in the global configuration.           
   def mvnHome = tool 'M3'

   // Mark the code build 'stage'....
   stage 'Build'
   // Run the maven build
   sh "${mvnHome}/bin/mvn clean install"

   step([$class: 'ArtifactArchiver', artifacts: '*/target/*.hpi'])
}

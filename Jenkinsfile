node {

     stage "Prepare environment"
       checkout scm
       def environment  = docker.build 'cloudbees-node'

       environment.inside {
        stage "Checkout and build"
          sh "npm install"

         stage "Test and validate"	
          sh "npm install gulp-cli && ./node_modules/.bin/gulp"
       }

     stage "Cleanup"
     	deleteDir()

}



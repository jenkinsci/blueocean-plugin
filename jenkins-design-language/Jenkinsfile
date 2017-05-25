node {
    stage "Prepare environment"
        checkout scm
        def environment  = docker.build 'cloudbees-node'

        environment.inside {
            stage "Validate deps and shrinkwrap"
                sh "./bin/checkshrinkwrap.js"

            stage "Checkout and build deps"
                sh "npm install"

            stage "Validate types"
                sh "./node_modules/.bin/flow"

            stage "Test and validate"
                sh "npm install gulp-cli && ./node_modules/.bin/gulp"
                step([$class: 'JUnitResultArchiver', testResults: 'reports/**/*.xml'])
        }

    stage "Cleanup"
        deleteDir()
}

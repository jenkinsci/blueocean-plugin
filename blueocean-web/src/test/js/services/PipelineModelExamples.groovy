pipeline {
    agent docker: "httpd:2.4.12", dockerArgs: ""
    stages {
        stage("non-parallel") {
            steps {
                sh 'cat /usr/local/apache2/conf/extra/httpd-userdir.conf'
                echo "This was NOT a parallel stage"
            }
        }
        stage("parallel") {
            steps {
                parallel (
                    "parallel 1": {
                        sh 'cat /usr/local/apache2/conf/extra/httpd-userdir.conf'
                        sh 'echo "I am double quoted"'
                        echo "This was parallel stage 1"
                    },
                    "parallel 2": {
                        sh 'cat /usr/local/apache2/conf/extra/httpd-userdir.conf'
                        sh 'echo "I am double quoted"'
                        echo "This was parallel stage 2"
                    }
                )
            }
        }
    }
}


pipeline {
    agent any
    stages {
        stage("multiple arguments") {
            steps {
                timeout(time: 5, unit: "SECONDS") {
                    echo "hello"
                }
            }
        }
    }
}

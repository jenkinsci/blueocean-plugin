export default {
    "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubRespositoryContainer",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/"
        }
    },
    "repositories": {
        "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubRepositories",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/repositories/"
            }
        },
        "items": [{
            "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubRepository",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/repositories/blog-examples/"
                }
            },
            "defaultBranch": "master",
            "description": "Sample code from stuff on my blog",
            "name": "blog-examples",
            "permissions": {
                "admin": true,
                "push": true,
                "pull": true
            },
            "private": false,
            "fullName": "cliffmeyers/blog-examples"
        }, {
            "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubRepository",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/repositories/blueocean-plugin/"
                }
            },
            "defaultBranch": "master",
            "description": "Blue Ocean UI app",
            "name": "blueocean-plugin",
            "permissions": {
                "admin": true,
                "push": true,
                "pull": true
            },
            "private": false,
            "fullName": "cliffmeyers/blueocean-plugin"
        }, {
            "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubRepository",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/repositories/frontend-maven-plugin/"
                }
            },
            "defaultBranch": "master",
            "description": "\"Maven-node-grunt-gulp-npm-node-plugin to end all maven-node-grunt-gulp-npm-plugins.\" A Maven plugin that downloads/installs Node and NPM locally, runs NPM install, Grunt, Gulp and/or Karma.",
            "name": "frontend-maven-plugin",
            "permissions": {
                "admin": true,
                "push": true,
                "pull": true
            },
            "private": false,
            "fullName": "cliffmeyers/frontend-maven-plugin"
        }, {
            "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubRepository",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/repositories/java-utils/"
                }
            },
            "defaultBranch": "master",
            "description": "Utilities for Java",
            "name": "java-utils",
            "permissions": {
                "admin": true,
                "push": true,
                "pull": true
            },
            "private": false,
            "fullName": "cliffmeyers/java-utils"
        }],
        "lastPage": 3,
        "nextPage": 2,
        "pageSize": 4
    }
}

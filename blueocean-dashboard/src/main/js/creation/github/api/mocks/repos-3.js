/* eslint-disable */
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
                    "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/repositories/mobx-experiments/"
                }
            },
            "defaultBranch": "master",
            "description": null,
            "name": "mobx-experiments",
            "permissions": {
                "admin": true,
                "push": true,
                "pull": true
            },
            "private": false,
            "fullName": "cliffmeyers/mobx-experiments"
        }, {
            "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubRepository",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/organizations/jenkins/scm/github/organizations/cliffmeyers/repositories/repositories/react-experiments/"
                }
            },
            "defaultBranch": "master",
            "description": null,
            "name": "react-experiments",
            "permissions": {
                "admin": true,
                "push": true,
                "pull": true
            },
            "private": false,
            "fullName": "cliffmeyers/react-experiments"
        }],
        "lastPage": null,
        "nextPage": null,
        "pageSize": 4
    }
}

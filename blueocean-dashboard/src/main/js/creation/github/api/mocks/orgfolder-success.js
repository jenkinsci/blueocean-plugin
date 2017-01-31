/* eslint-disable */
export default {
    "_class": "io.jenkins.blueocean.blueocean_github_pipeline.GithubOrganizationFolder",
    "_links": {
        "pipelines": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/pipelines/"
        },
        "activities": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/activities/"
        },
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/actions/"
        },
        "runs": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/runs/"
        },
        "queue": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/queue/"
        }
    },
    "_capabilities": ["jenkins.branch.OrganizationFolder"],
    "actions": [],
    "displayName": "Cliff Meyers",
    "fullDisplayName": "Cliff%20Meyers",
    "fullName": "cliffmeyers",
    "name": "cliffmeyers",
    "organization": "jenkins",
    "parameters": null,
    "permissions": {
        "create": true,
        "read": true,
        "start": true,
        "stop": true
    },
    "numberOfFolders": 0,
    "numberOfPipelines": 0,
    "icon": "https://avatars.githubusercontent.com/u/143466?v=3&s=32",
    "requestedRepos": [],
    "pipelines": ["blueocean-plugin", "jenkins-design-language"],
    "latestRun": {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderRunImpl",
        "_links": {
            "nodes": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/runs/1/nodes/"
            },
            "log": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/runs/1/log/"
            },
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/runs/1/"
            },
            "actions": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/runs/1/actions/"
            },
            "steps": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/runs/1/steps/"
            },
            "artifacts": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/cliffmeyers/runs/1/artifacts/"
            }
        },
        "actions": null,
        "artifactsZipFile": null,
        "changeSet": null,
        "durationInMillis": 5598,
        "enQueueTime": "2017-01-31T10:31:05.545-0500",
        "endTime": null,
        "estimatedDurationInMillis": 5598,
        "id": "1",
        "organization": "jenkins",
        "pipeline": "cliffmeyers",
        "result": "SUCCESS",
        "runSummary": "SUCCESS:FINISHED",
        "startTime": "2017-01-31T10:31:05.545-0500",
        "state": "FINISHED",
        "type": "com.cloudbees.hudson.plugins.folder.computed.FolderComputation"
    }
}

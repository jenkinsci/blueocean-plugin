/* eslint-disable */

// Dumped from in-container execution 2017-02-08
export const testData = {
    "run": {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl",
        "_capabilities": [
            "org.jenkinsci.plugins.workflow.job.WorkflowRun",
            "io.jenkins.blueocean.service.embedded.rest.AbstractRunImpl",
            "io.jenkins.blueocean.rest.model.BlueRun",
            "io.jenkins.blueocean.rest.model.Resource"
        ],
        "_links": {
            "parent": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/passing/"
            },
            "nodes": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/passing/runs/1/nodes/"
            },
            "log": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/passing/runs/1/log/"
            },
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/passing/runs/1/"
            },
            "actions": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/passing/runs/1/actions/"
            },
            "steps": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/passing/runs/1/steps/"
            },
            "artifacts": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/passing/runs/1/artifacts/"
            }
        },
        "changeSet": [
            {
                "_class": "io.jenkins.blueocean.service.embedded.rest.ChangeSetResource",
                "_links": {
                    "self": {
                        "_class": "io.jenkins.blueocean.rest.hal.Link",
                        "href": "/blue/rest/organizations/jenkins/pipelines/blueocean/branches/PR-796/runs/3/changeset/73d7c8c80611b322b8607edd57fbd248cf136296/"
                    }
                },
                "affectedPaths": [
                    "blueocean-dashboard/package.json",
                    "blueocean-core-js/package.json",
                    "blueocean-core-js/npm-shrinkwrap.json",
                    "blueocean-personalization/npm-shrinkwrap.json",
                    "blueocean-web/package.json",
                    "blueocean-web/npm-shrinkwrap.json",
                    "blueocean-dashboard/npm-shrinkwrap.json",
                    "blueocean-personalization/package.json"
                ],
                "author": {
                    "_class": "io.jenkins.blueocean.service.embedded.rest.UserImpl",
                    "_links": {
                        "self": {
                            "_class": "io.jenkins.blueocean.rest.hal.Link",
                            "href": "/blue/rest/users/kzantow/"
                        }
                    },
                    "avatar": null,
                    "email": null,
                    "fullName": "Keith Zantow",
                    "id": "kzantow"
                },
                "commitId": "73d7c8c80611b322b8607edd57fbd248cf136296",
                "msg": "Actually update...",
                "timestamp": "2017-02-08T05:46:47.000+0000",
                "url": "https://github.com/kzantow/blueocean-plugin/commit/73d7c8c80611b322b8607edd57fbd248cf136296"
            }
        ],
        "artifacts": null,
        "durationInMillis": 41024,
        "enQueueTime": "2016-12-22T13:51:59.735+1000",
        "endTime": "2016-12-22T13:52:40.760+1000",
        "estimatedDurationInMillis": 41024,
        "id": "1",
        "organization": "jenkins",
        "pipeline": "passing",
        "result": "SUCCESS",
        "runSummary": "stable",
        "startTime": "2016-12-22T13:51:59.736+1000",
        "state": "FINISHED",
        "type": "WorkflowRun",
        "commitId": "c6ab12eb1425271c1cd1bca10792a2520ce3e4d0",
        "parameters": null,
        "artifactsZipFile": "/job/FP/job/passing/1/artifact/*zip*/archive.zip"
    },
    "pipeline": {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl",
        "_links": {
            "activities": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/activities/"
            },
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/"
            },
            "branches": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/branches/"
            },
            "actions": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/actions/"
            },
            "runs": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/runs/"
            },
            "queue": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/FP/queue/"
            }
        },
        "actions": [
            {
                "_class": "com.cloudbees.plugins.credentials.ViewCredentialsAction",
                "_links": {
                    "self": {
                        "_class": "io.jenkins.blueocean.rest.hal.Link",
                        "href": "/blue/rest/organizations/jenkins/pipelines/FP/credentials/"
                    }
                },
                "stores": {
                    "folder": {
                        "_class": "com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider$FolderCredentialsProperty$CredentialsStoreActionImpl",
                        "_capabilities": [
                            "com.cloudbees.plugins.credentials.CredentialsStoreAction"
                        ]
                    }
                },
                "urlName": "credentials",
                "_capabilities": []
            }
        ],
        "displayName": "FP",
        "fullDisplayName": "FP",
        "fullName": "FP",
        "name": "FP",
        "organization": "jenkins",
        "parameters": null,
        "permissions": {
            "create": true,
            "read": true,
            "start": true,
            "stop": true
        },
        "estimatedDurationInMillis": 6290,
        "numberOfFolders": 0,
        "numberOfPipelines": 9,
        "weatherScore": 0,
        "branchNames": [
            "feature%2Fadd-findbugs",
            "passing",
            "test-pass-fail-stage",
            "master",
            "blockscoped",
            "michaelneale-blockscoped",
            "michaelneale",
            "feature%2Fsomething",
            "fixes-and-breaks"
        ],
        "numberOfFailingBranches": 5,
        "numberOfFailingPullRequests": 0,
        "numberOfSuccessfulBranches": 2,
        "numberOfSuccessfulPullRequests": 0,
        "totalNumberOfBranches": 9,
        "totalNumberOfPullRequests": 0,
        "_capabilities": [
            "jenkins.branch.MultiBranchProject",
            "io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline",
            "io.jenkins.blueocean.rest.model.BluePipelineFolder",
            "com.cloudbees.hudson.plugins.folder.AbstractFolder",
            "io.jenkins.blueocean.rest.model.BluePipeline",
            "io.jenkins.blueocean.rest.model.Resource"
        ]
    }
};

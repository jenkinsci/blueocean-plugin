export default [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.BranchImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/actions/"
        },
        "runs": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/"
        },
        "queue": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/queue/"
        }
    },
    "actions": [{
        "_class": "com.cloudbees.plugins.credentials.ViewCredentialsAction",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/credentials/"
            }
        },
        "stores": {},
        "urlName": "credentials"
    }],
    "displayName": "master",
    "estimatedDurationInMillis": 39110,
    "fullName": "tfprdemo/master",
    "latestRun": {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl",
        "_links": {
            "nodes": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/nodes/"
            },
            "log": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/log/"
            },
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/"
            },
            "actions": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/actions/"
            },
            "steps": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/steps/"
            }
        },
        "actions": [{
            "_class": "jenkins.metrics.impl.TimeInQueueAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/timings/"
                }
            },
            "queuingDurationMillis": 2,
            "totalDurationMillis": 42108,
            "urlName": "timings"
        }, {
            "_class": "hudson.plugins.git.util.BuildData",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/git/"
                }
            },
            "buildsByBranchName": {
                "master": {
                    "_class": "hudson.plugins.git.util.Build",
                    "buildNumber": 3,
                    "buildResult": null,
                    "marked": {
                        "SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
                        "branch": [{"SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64", "name": "master"}]
                    },
                    "revision": {
                        "SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
                        "branch": [{"SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64", "name": "master"}]
                    }
                }
            },
            "lastBuiltRevision": {
                "SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
                "branch": [{"SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64", "name": "master"}]
            },
            "remoteUrls": ["https://github.com/TomTestOrg/PR-demo.git"],
            "scmName": "",
            "urlName": "git"
        }, {
            "_class": "hudson.plugins.git.GitTagAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/tagBuild/"
                }
            },
            "tags": [],
            "urlName": "tagBuild"
        }, {
            "_class": "hudson.plugins.git.util.BuildData",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/git/"
                }
            },
            "buildsByBranchName": {
                "master": {
                    "_class": "hudson.plugins.git.util.Build",
                    "buildNumber": 3,
                    "buildResult": null,
                    "marked": {
                        "SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
                        "branch": [{"SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64", "name": "master"}]
                    },
                    "revision": {
                        "SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
                        "branch": [{"SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64", "name": "master"}]
                    }
                }
            },
            "lastBuiltRevision": {
                "SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
                "branch": [{"SHA1": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64", "name": "master"}]
            },
            "remoteUrls": ["https://github.com/TomTestOrg/PR-demo.git"],
            "scmName": "",
            "urlName": "git"
        }, {
            "_class": "hudson.plugins.git.GitTagAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/tagBuild/"
                }
            },
            "tags": [],
            "urlName": "tagBuild"
        }, {
            "_class": "org.jenkinsci.plugins.workflow.job.views.FlowGraphAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/flowGraph/"
                }
            },
            "nodes": [{"_class": "org.jenkinsci.plugins.workflow.graph.FlowStartNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.graph.FlowEndNode"}],
            "urlName": "flowGraph"
        }],
        "artifacts": [],
        "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
        "changeSet": [{
            "_class": "io.jenkins.blueocean.service.embedded.rest.ChangeSetResource",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/changeset/7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64/"
                }
            },
            "author": {
                "_class": "io.jenkins.blueocean.service.embedded.rest.UserImpl",
                "_links": {
                    "self": {
                        "_class": "io.jenkins.blueocean.rest.hal.Link",
                        "href": "/blue/rest/users/noreply/"
                    }
                },
                "email": "noreply@github.com",
                "fullName": "noreply",
                "id": "noreply"
            },
            "affectedPaths": ["Jenkinsfile"],
            "commitId": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
            "comment": "Update Jenkinsfile\n",
            "date": "2016-07-02 18:24:38 +0100",
            "id": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64",
            "msg": "Update Jenkinsfile",
            "paths": [{"editType": "edit", "file": "Jenkinsfile"}],
            "timestamp": "2016-07-02T18:24:38.000+0100"
        }],
        "durationInMillis": 42106,
        "enQueueTime": "2016-07-02T18:25:06.061+0100",
        "endTime": "2016-07-02T18:25:48.169+0100",
        "estimatedDurationInMillis": 39110,
        "id": "3",
        "organization": "jenkins",
        "pipeline": "master",
        "result": "SUCCESS",
        "runSummary": "stable",
        "startTime": "2016-07-02T18:25:06.063+0100",
        "state": "FINISHED",
        "steps": [{
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/steps/3/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/steps/6/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/steps/7/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/steps/10/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/master/runs/3/steps/11/"
                }
            }
        }],
        "type": "WorkflowRun",
        "commitId": "7d7e6b1589bf8be0ffbdd99bb3fd79581f0e7c64"
    },
    "name": "master",
    "organization": "jenkins",
    "weatherScore": 100,
    "pullRequest": null
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.BranchImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/actions/"
        },
        "runs": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/"
        },
        "queue": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/queue/"
        }
    },
    "actions": [{
        "_class": "com.cloudbees.plugins.credentials.ViewCredentialsAction",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/credentials/"
            }
        },
        "stores": {},
        "urlName": "credentials"
    }],
    "displayName": "quicker",
    "estimatedDurationInMillis": 18710,
    "fullName": "tfprdemo/quicker",
    "latestRun": {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl",
        "_links": {
            "nodes": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/nodes/"
            },
            "log": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/log/"
            },
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/"
            },
            "actions": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/actions/"
            },
            "steps": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/steps/"
            }
        },
        "actions": [{
            "_class": "jenkins.metrics.impl.TimeInQueueAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/timings/"
                }
            },
            "queuingDurationMillis": 2,
            "totalDurationMillis": 18712,
            "urlName": "timings"
        }, {
            "_class": "hudson.plugins.git.util.BuildData",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/git/"
                }
            },
            "buildsByBranchName": {
                "quicker": {
                    "_class": "hudson.plugins.git.util.Build",
                    "buildNumber": 1,
                    "buildResult": null,
                    "marked": {
                        "SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c",
                        "branch": [{"SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c", "name": "quicker"}]
                    },
                    "revision": {
                        "SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c",
                        "branch": [{"SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c", "name": "quicker"}]
                    }
                }
            },
            "lastBuiltRevision": {
                "SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c",
                "branch": [{"SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c", "name": "quicker"}]
            },
            "remoteUrls": ["https://github.com/TomTestOrg/PR-demo.git"],
            "scmName": "",
            "urlName": "git"
        }, {
            "_class": "hudson.plugins.git.GitTagAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/tagBuild/"
                }
            },
            "tags": [],
            "urlName": "tagBuild"
        }, {
            "_class": "hudson.plugins.git.util.BuildData",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/git/"
                }
            },
            "buildsByBranchName": {
                "quicker": {
                    "_class": "hudson.plugins.git.util.Build",
                    "buildNumber": 1,
                    "buildResult": null,
                    "marked": {
                        "SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c",
                        "branch": [{"SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c", "name": "quicker"}]
                    },
                    "revision": {
                        "SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c",
                        "branch": [{"SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c", "name": "quicker"}]
                    }
                }
            },
            "lastBuiltRevision": {
                "SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c",
                "branch": [{"SHA1": "ffd80d68b6c3bc7552feff5a7b77525625500a7c", "name": "quicker"}]
            },
            "remoteUrls": ["https://github.com/TomTestOrg/PR-demo.git"],
            "scmName": "",
            "urlName": "git"
        }, {
            "_class": "hudson.plugins.git.GitTagAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/tagBuild/"
                }
            },
            "tags": [],
            "urlName": "tagBuild"
        }, {
            "_class": "org.jenkinsci.plugins.workflow.job.views.FlowGraphAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/flowGraph/"
                }
            },
            "nodes": [{"_class": "org.jenkinsci.plugins.workflow.graph.FlowStartNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode"}, {"_class": "org.jenkinsci.plugins.workflow.graph.FlowEndNode"}],
            "urlName": "flowGraph"
        }],
        "artifacts": [],
        "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
        "changeSet": [],
        "durationInMillis": 18710,
        "enQueueTime": "2016-07-01T17:20:23.071+0100",
        "endTime": "2016-07-01T17:20:41.781+0100",
        "estimatedDurationInMillis": 18710,
        "id": "1",
        "organization": "jenkins",
        "pipeline": "quicker",
        "result": "SUCCESS",
        "runSummary": "stable",
        "startTime": "2016-07-01T17:20:23.071+0100",
        "state": "FINISHED",
        "steps": [{
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/steps/3/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/steps/6/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/steps/7/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/steps/10/"
                }
            }
        }, {
            "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/quicker/runs/1/steps/11/"
                }
            }
        }],
        "type": "WorkflowRun",
        "commitId": "ffd80d68b6c3bc7552feff5a7b77525625500a7c"
    },
    "name": "quicker",
    "organization": "jenkins",
    "weatherScore": 100,
    "pullRequest": null
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.BranchImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/actions/"
        },
        "runs": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/"
        },
        "queue": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/queue/"
        }
    },
    "actions": [{
        "_class": "com.cloudbees.plugins.credentials.ViewCredentialsAction",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/credentials/"
            }
        },
        "stores": {},
        "urlName": "credentials"
    }],
    "displayName": "tfennelly-patch-1",
    "estimatedDurationInMillis": -1,
    "fullName": "tfprdemo/tfennelly-patch-1",
    "latestRun": {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl",
        "_links": {
            "nodes": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/1/nodes/"
            },
            "log": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/1/log/"
            },
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/1/"
            },
            "actions": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/1/actions/"
            },
            "steps": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/1/steps/"
            }
        },
        "actions": [{
            "_class": "jenkins.metrics.impl.TimeInQueueAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/1/timings/"
                }
            },
            "queuingDurationMillis": 2,
            "totalDurationMillis": 2,
            "urlName": "timings"
        }, {
            "_class": "org.jenkinsci.plugins.workflow.job.views.FlowGraphAction",
            "_links": {
                "self": {
                    "_class": "io.jenkins.blueocean.rest.hal.Link",
                    "href": "/blue/rest/organizations/jenkins/pipelines/tfprdemo/branches/tfennelly-patch-1/runs/1/flowGraph/"
                }
            },
            "nodes": [],
            "urlName": "flowGraph"
        }],
        "artifacts": [],
        "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
        "changeSet": [],
        "durationInMillis": 0,
        "enQueueTime": "2016-07-04T16:13:59.226+0100",
        "endTime": null,
        "estimatedDurationInMillis": -1,
        "id": "1",
        "organization": "jenkins",
        "pipeline": "tfennelly-patch-1",
        "result": "UNKNOWN",
        "runSummary": "?",
        "startTime": "2016-07-04T16:13:59.227+0100",
        "state": "QUEUED",
        "steps": [],
        "type": "WorkflowRun",
        "commitId": null
    },
    "name": "tfennelly-patch-1",
    "organization": "jenkins",
    "weatherScore": 100,
    "pullRequest": null
}]

export const pipelineQueued = [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/5/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/5/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/5/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/5/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "Stage 1",
    "durationInMillis": null,
    "id": "5",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "8"
    }]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/8/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/8/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/8/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/8/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "Stage 2",
    "durationInMillis": null,
    "id": "8",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "11"
    }, {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "12"
    }]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/11/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/11/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/11/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/11/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "firstBranch",
    "durationInMillis": null,
    "id": "11",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "18"
    }]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/12/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/12/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/12/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/12/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "secondBranch",
    "durationInMillis": null,
    "id": "12",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "18"
    }]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/18/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/18/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/18/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/18/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "fin",
    "durationInMillis": null,
    "id": "18",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "22"
    }]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/22/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/22/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/22/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/stages/runs/2/nodes/22/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "NoSteps",
    "durationInMillis": null,
    "id": "22",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": []
}];

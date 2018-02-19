export const queuedAborted = [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/5/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/5/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/5/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/5/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "Pre",
    "durationInMillis": null,
    "id": "5",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": [{"_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl", "id": "6"}]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/6/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/6/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/6/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/6/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "Stage 1",
    "durationInMillis": null,
    "id": "6",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": [{"_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl", "id": "8"}]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/8/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/8/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/8/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/8/wfapi/"
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
    "edges": [{"_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl", "id": "10"}]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/10/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/10/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/10/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/JENKINS-36700/runs/5/nodes/10/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "Cleanup",
    "durationInMillis": null,
    "id": "10",
    "result": null,
    "startTime": null,
    "state": null,
    "edges": []
}]

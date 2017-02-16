export const poststagefail = [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/11/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/11/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/11/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {"self": null},
        "_class": "org.jenkinsci.plugins.workflow.actions.BodyInvocationAction",
        "urlName": null
    }, {
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {"self": null},
        "_class": "org.jenkinsci.plugins.workflow.actions.LabelAction",
        "urlName": null
    }, {
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {"self": null},
        "_class": "org.jenkinsci.plugins.workflow.actions.TimingAction",
        "urlName": null
    }, {
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/11/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "build",
    "durationInMillis": 82230,
    "id": "11",
    "input": null,
    "result": "FAILURE",
    "startTime": "2017-01-16T21:36:49.032+0000",
    "state": "FINISHED",
    "causeOfBlockage": null,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "16"
    }]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/16/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/16/actions/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/16/steps/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {"self": null},
        "_class": "org.jenkinsci.plugins.workflow.actions.BodyInvocationAction",
        "urlName": null
    }, {
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {"self": null},
        "_class": "org.jenkinsci.plugins.workflow.actions.LabelAction",
        "urlName": null
    }, {
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {"self": null},
        "_class": "org.jenkinsci.plugins.workflow.actions.TimingAction",
        "urlName": null
    }, {
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/Pipeline Editor/branches/master/runs/17/nodes/16/wfapi/"
            }
        },
        "_class": "com.cloudbees.workflow.rest.endpoints.FlowNodeAPI",
        "urlName": "wfapi"
    }],
    "displayName": "Post Build Actions",
    "durationInMillis": 86,
    "id": "16",
    "input": null,
    "result": "FAILURE",
    "startTime": "2017-01-16T21:38:11.327+0000",
    "state": "FINISHED",
    "causeOfBlockage": null,
    "edges": []
}]

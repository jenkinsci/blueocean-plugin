/* eslint-disable */
export default [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/histos/pipelines/karaoke-samples/branches/step-description-long/runs/2/nodes/6/steps/7/"
        },
        "actions": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/histos/pipelines/karaoke-samples/branches/step-description-long/runs/2/nodes/6/steps/7/actions/"
        }
    },
    "actions": [{
        "_class": "io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl",
        "_links": {
            "self": {
                "_class": "io.jenkins.blueocean.rest.hal.Link",
                "href": "/blue/rest/organizations/jenkins/pipelines/histos/pipelines/karaoke-samples/branches/step-description-long/runs/2/nodes/6/steps/7/log/"
            }
        },
        "_class": "org.jenkinsci.plugins.workflow.support.actions.LogActionImpl",
        "urlName": "log"
    }],
    "displayDescription": "echo \"This is a very long step description. The intent of this step description is to be so long that it would cause the text to wrap. So we will just keep adding more and more text until that happens.\"",
    "displayName": "Shell Script",
    "durationInMillis": 258,
    "id": "7",
    "input": null,
    "result": "SUCCESS",
    "startTime": "2017-06-05T09:56:27.681-0400",
    "state": "FINISHED"
}];

export const finishedMultipleFailure = [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "displayName": "Stage 1",
    "durationInMillis": 10264,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "12"
    }],
    "id": "5",
    "result": "SUCCESS",
    "startTime": "2016-05-25T13:47:40.534+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "displayName": "Stage 2",
    "durationInMillis": 22143,
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "16"
    }, {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "17"
    }, {
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "18"
    }],
    "id": "12",
    "result": "FAILURE",
    "startTime": "2016-05-25T13:47:50.798+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "displayName": "firstBranch",
    "durationInMillis": 22004,
    "edges": [],
    "id": "16",
    "result": "SUCCESS",
    "startTime": "2016-05-25T13:47:50.800+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "displayName": "secondBranch",
    "durationInMillis": 22053,
    "edges": [],
    "id": "17",
    "result": "FAILURE",
    "startTime": "2016-05-25T13:47:50.801+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "displayName": "thirdBranch",
    "durationInMillis": 22069,
    "edges": [],
    "id": "18",
    "result": "FAILURE",
    "startTime": "2016-05-25T13:47:50.802+0200",
    "state": "FINISHED"
}]

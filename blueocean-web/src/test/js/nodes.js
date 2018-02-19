export const nodes = [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/"
        }
    },
    "displayName": "deploy",
    "durationInMillis": 30779,
    "id": "5",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:38.829+0200",
    "state": "FINISHED",
    "edges": [{"_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl", "id": "23"}]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/23/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/23/steps/"
        }
    },
    "displayName": "testing",
    "durationInMillis": 20235,
    "id": "23",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:09.608+0200",
    "state": "FINISHED",
    "edges": [{
        "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl",
        "id": "26"
    }, {"_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl", "id": "27"}]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/26/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/26/steps/"
        }
    },
    "displayName": "firstBranch",
    "durationInMillis": 20176,
    "id": "26",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:09.609+0200",
    "state": "FINISHED",
    "edges": [{"_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl", "id": "45"}]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/27/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/27/steps/"
        }
    },
    "displayName": "secondBranch",
    "durationInMillis": 15652,
    "id": "27",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:09.609+0200",
    "state": "FINISHED",
    "edges": [{"_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl", "id": "45"}]
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/"
        },
        "steps": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/"
        }
    },
    "displayName": "fin",
    "durationInMillis": 26951,
    "id": "45",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:29.843+0200",
    "state": "FINISHED",
    "edges": []
}];
export const stepsNode5 = [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/6/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 4389,
    "id": "6",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:38.830+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/7/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 2,
    "id": "7",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:43.219+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/8/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 4390,
    "id": "8",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:43.221+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/9/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 1,
    "id": "9",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:47.611+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/10/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 4391,
    "id": "10",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:47.612+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/11/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 2,
    "id": "11",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:52.003+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/12/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 4389,
    "id": "12",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:52.005+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/13/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 2,
    "id": "13",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:56.394+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/14/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 4386,
    "id": "14",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:02:56.396+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/15/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 2,
    "id": "15",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:00.782+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/16/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 4384,
    "id": "16",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:00.784+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/17/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 1,
    "id": "17",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:05.168+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/5/steps/18/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 4413,
    "id": "18",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:05.169+0200",
    "state": "FINISHED"
}];

export const stepsNode45 = [{
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/46/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 6727,
    "id": "46",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:29.844+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/47/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 1,
    "id": "47",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:36.571+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/48/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 6726,
    "id": "48",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:36.572+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/49/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 1,
    "id": "49",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:43.298+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/50/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 1,
    "id": "50",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:43.299+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/51/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 6737,
    "id": "51",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:43.300+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/52/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 0,
    "id": "52",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:50.037+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/53/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 1,
    "id": "53",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:50.037+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/54/"
        }
    },
    "displayName": "Print Message",
    "durationInMillis": 1,
    "id": "54",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:50.038+0200",
    "state": "FINISHED"
}, {
    "_class": "io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl",
    "_links": {
        "self": {
            "_class": "io.jenkins.blueocean.rest.hal.Link",
            "href": "/blue/rest/organizations/jenkins/pipelines/steps/runs/16/nodes/45/steps/55/"
        }
    },
    "displayName": "Shell Script",
    "durationInMillis": 6749,
    "id": "55",
    "result": "SUCCESS",
    "startTime": "2016-07-01T14:03:50.039+0200",
    "state": "FINISHED"
}];

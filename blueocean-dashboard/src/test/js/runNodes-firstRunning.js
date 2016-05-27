export const firstRunning = [
  {
    "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl",
    "displayName": "Stage 1",
    "durationInMillis": 2996,
    "edges": [
      {
        "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl$EdgeImpl",
        "id": "12"
      }
    ],
    "id": "5",
    "result": "UNKNOWN",
    "startTime": "2016-05-25T13:47:40.534+0200",
    "state": "RUNNING"
  },
  {
    "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl",
    "displayName": "Stage 2",
    "durationInMillis": null,
    "edges": [
      {
        "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl$EdgeImpl",
        "id": "15"
      },
      {
        "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl$EdgeImpl",
        "id": "16"
      }
    ],
    "id": "12",
    "result": null,
    "startTime": null,
    "state": null
  },
  {
    "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl",
    "displayName": "firstBranch",
    "durationInMillis": null,
    "edges": [
      {
        "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl$EdgeImpl",
        "id": "28"
      }
    ],
    "id": "15",
    "result": null,
    "startTime": null,
    "state": null
  },
  {
    "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl",
    "displayName": "secondBranch",
    "durationInMillis": null,
    "edges": [
      {
        "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl$EdgeImpl",
        "id": "28"
      }
    ],
    "id": "16",
    "result": null,
    "startTime": null,
    "state": null
  },
  {
    "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineNodeImpl",
    "displayName": "deploy",
    "durationInMillis": null,
    "edges": [],
    "id": "28",
    "result": null,
    "startTime": null,
    "state": null
  }
]

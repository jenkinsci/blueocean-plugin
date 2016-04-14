# Usage

## Crumbs

Jenkins usually requires a "crumb" with posted reuqests to prevent request forgery and other shenanigans. 
To avoid needing a crumb to POST data, the header `Content-Type: application/json` *must* be used.

## Run BlueOcean plugin

    cd bluecoean-plugin
    mvn hpi:run
    
This will launch jenkins with BlueOcean plugin. 

BlueOcean UI is available at:
    
    http://localhost:8080/jenkins/bo
    

BlueOcean rest API base URL is:
    
    http://localhost:8080/jenkins/blue/rest

## Get a user

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/users/alice 
    
    {
      "id" : "alice",
      "fullName" : "Alice"
      "email" : "alice@example.com"
    }

## Find users in an organization

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/search\?q\=type:user\;organization:jenkins
    
    [ 
      {
        "id" : "alice",
        "name" : "Alice"
      } 
    ]

## Get organization details

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins
    
    {
      "name" : "jenkins"
    }

## Get a Pipeline

    curl -v -X GET  "http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1"

    {
      "organization" : "jenkins",
      "name" : "pipeline1",
      "displayName": "pipeline1",
      "weatherScore": 100
    }

## Get Pipelines

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/
    
    [ 
      {
      "organization" : "jenkins",
      "name" : "pipeline1",
      "displayName": "pipeline1",
      "weatherScore": 100
      } 
    ]
    
## Get all runs in a pipeline
    
    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs
    
    [
        {
            "changeSet": [],
            "durationInMillis": 841,
            "enQueueTime": "2016-03-16T09:02:26.492-0700",
            "endTime": "2016-03-16T09:02:27.339-0700",
            "id": "1",
            "organization": "jenkins",
            "pipeline": "pipeline1",
            "result": "SUCCESS",
            "runSummary": "stable",
            "startTime": "2016-03-16T09:02:26.498-0700",
            "state": "FINISHED",
            "type": "WorkflowRun",
            "commitId": null
        }
    ]
    

## Get a run details

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1    
    
    {
        "changeSet": [],
        "durationInMillis": 841,
        "enQueueTime": "2016-03-16T09:02:26.492-0700",
        "endTime": "2016-03-16T09:02:27.339-0700",
        "id": "1",
        "organization": "jenkins",
        "pipeline": "pipeline1",
        "result": "SUCCESS",
        "runSummary": "stable",
        "startTime": "2016-03-16T09:02:26.498-0700",
        "state": "FINISHED",
        "type": "WorkflowRun",
        "commitId": null
    }

## Find latest run of a pipeline

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/?q=type:run;organization:jenkins;pipeline:pipeline1;latestOnly:true
    
    [ 
      {
          "changeSet": [],
          "durationInMillis": 841,
          "enQueueTime": "2016-03-16T09:02:26.492-0700",
          "endTime": "2016-03-16T09:02:27.339-0700",
          "id": "1",
          "organization": "jenkins",
          "pipeline": "pipeline1",
          "result": "SUCCESS",
          "runSummary": "stable",
          "startTime": "2016-03-16T09:02:26.498-0700",
          "state": "FINISHED",
          "type": "WorkflowRun",
          "commitId": null
      } 
    ]

## Find latest run on all pipelines

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/?q=type:run;organization:jenkins;latestOnly:true
    
    [ 
      {
          "changeSet": [],
          "durationInMillis": 841,
          "enQueueTime": "2016-03-16T09:02:26.492-0700",
          "endTime": "2016-03-16T09:02:27.339-0700",
          "id": "1",
          "organization": "jenkins",
          "pipeline": "pipeline1",
          "result": "SUCCESS",
          "runSummary": "stable",
          "startTime": "2016-03-16T09:02:26.498-0700",
          "state": "FINISHED",
          "type": "WorkflowRun",
          "commitId": null
      }       
    ]

# MultiBranch Pipeline

Create MultiBranch build and set it up with your git repo. Your git repo must have Jenkinsfile with build script. 
Each branch in the repo with Jenkins file will appear as a branch in this pipeline.

## Get MultiBranch pipeline 

    curl -v http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/p/
    
    {
        "displayName": "p",
        "name": "p",
        "organization": "jenkins",
        "weatherScore": 100,
        "branchNames": [
            "feature1",
            "master",
            "feature2"
        ],
        "numberOfFailingBranches": 0,
        "numberOfFailingPullRequests": 0,
        "numberOfSuccessfulBranches": 0,
        "numberOfSuccessfulPullRequests": 0,
        "totalNumberOfBranches": 3,
        "totalNumberOfPullRequests": 0
    }

    
## Get MultiBranch pipeline branches 

    curl -v http://localhost:56720/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches
    
    [
        {
            "name": "master",
            "weatherScore":100,
            "latestRun": {
                  "changeSet": [

                  ],
                  "durationInMillis": 0,
                  "enQueueTime": "2016-03-10T15:14:08.787+1300",
                  "endTime": null,
                  "id": "1",
                  "organization": "jenkins",
                  "pipeline": "feature1",
                  "result": "UNKNOWN",
                  "runSummary": "?",
                  "startTime": "2016-03-10T15:14:08.788+1300",
                  "state": "RUNNING",
                  "type": "WorkflowRun",
                  "commitId": "431a0359f3e6d0559853439c3a9ffd36c47ca5d3"
                },
            "pullRequest": null
        },
        {
            "name": "feature1",
            "weatherScore":100,
            "latestRun": {
                  "changeSet": [

                  ],
                  "durationInMillis": 0,
                  "enQueueTime": "2016-03-10T15:14:08.787+1300",
                  "endTime": null,
                  "id": "1",
                  "organization": "jenkins",
                  "pipeline": "feature1",
                  "result": "UNKNOWN",
                  "runSummary": "?",
                  "startTime": "2016-03-10T15:14:08.788+1300",
                  "state": "RUNNING",
                  "type": "WorkflowRun",
                  "commitId": "431a0359f3e6d0559853439c3a9ffd36c47ca5d3"
            },
            "pullRequest": null
        },
        {
            "name": "feature2",
            "weatherScore":100,
            "latestRun": {
                  "changeSet": [

                  ],
                  "durationInMillis": 0,
                  "enQueueTime": "2016-03-10T15:14:08.787+1300",
                  "endTime": null,
                  "id": "1",
                  "organization": "jenkins",
                  "pipeline": "feature1",
                  "result": "UNKNOWN",
                  "runSummary": "?",
                  "startTime": "2016-03-10T15:14:08.788+1300",
                  "state": "RUNNING",
                  "type": "WorkflowRun",
                  "commitId": "431a0359f3e6d0559853439c3a9ffd36c47ca5d3"
            },
            "pullRequest": null
        }
    ]
    
    
## Get MultiBranch job's branch run detail
    
    curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/feature1/runs/1
    
    {
        "durationInMillis": 1330,
        "enQueueTime": "2016-03-16T09:08:15.607-0700",
        "endTime": "2016-03-16T09:08:16.942-0700",
        "id": "1",
        "organization": "jenkins",
        "pipeline": "feature1",
        "result": "SUCCESS",
        "runSummary": "stable",
        "startTime": "2016-03-16T09:08:15.612-0700",
        "state": "FINISHED",
        "type": "WorkflowRun",
        "commitId": "aad1c51fb29e053d1ccb20dbcdb1fe28fddcfba5",
        "changeSet": []
    }

## Get all runs for all branches on a multibranch pipeline (ordered by date)

     curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs

    [
        {
            "changeSet": [

            ],
            "durationInMillis": 1875,
            "enQueueTime": "2016-03-10T15:27:13.687+1300",
            "endTime": "2016-03-10T15:27:15.567+1300",
            "id": "1",
            "organization": "jenkins",
            "pipeline": "feature1",
            "result": "SUCCESS",
            "runSummary": "stable",
            "startTime": "2016-03-10T15:27:13.692+1300",
            "state": "FINISHED",
            "type": "WorkflowRun",
            "commitId": "52615df5828f1dddf672b86d64196294e3fbee88"
        },
        {
            "changeSet": [

            ],
            "durationInMillis": 1716,
            "enQueueTime": "2016-03-10T15:27:13.692+1300",
            "endTime": "2016-03-10T15:27:15.409+1300",
            "id": "1",
            "organization": "jenkins",
            "pipeline": "master",
            "result": "SUCCESS",
            "runSummary": "stable",
            "startTime": "2016-03-10T15:27:13.693+1300",
            "state": "FINISHED",
            "type": "WorkflowRun",
            "commitId": "bfd1f72dc63ca63a8c1b152dc9263c7c81862afa"
        },
        {
            "changeSet": [

            ],
            "durationInMillis": 1714,
            "enQueueTime": "2016-03-10T15:27:13.700+1300",
            "endTime": "2016-03-10T15:27:15.415+1300",
            "id": "1",
            "organization": "jenkins",
            "pipeline": "feature2",
            "result": "SUCCESS",
            "runSummary": "stable",
            "startTime": "2016-03-10T15:27:13.701+1300",
            "state": "FINISHED",
            "type": "WorkflowRun",
            "commitId": "84cb56b50589e720385ef2491a1ebab9d227da6e"
        }
    ]

## Get change set for a run

    curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/master/runs/2/
    
    {
        "changeSet": [
            {
                "author": {
                    "email": "vivek.pandey@gmail.com",
                    "fullName": "vivek.pandey",
                    "id": "vivek.pandey"
                },
                "affectedPaths": [
                    "file"
                ],
                "commitId": "e2d1d695a2009ac44d97e6e7a542ba3786153c41",
                "comment": "tweaked11\n",
                "date": "2016-03-02 16:49:26 -0800",
                "id": "e2d1d695a2009ac44d97e6e7a542ba3786153c41",
                "msg": "tweaked11",
                "paths": [
                    {
                        "editType": "edit",
                        "file": "file"
                    }
                ],
                "timestamp": "2016-03-02T16:49:26.000-0800"
            }
        ],
        "durationInMillis": 348,
        "enQueueTime": "2016-03-02T16:49:26.548-0800",
        "endTime": "2016-03-02T16:49:26.898-0800",
        "id": "2",
        "organization": "jenkins",
        "pipeline": "master",
        "runSummary": "stable",
        "startTime": "2016-03-02T16:49:26.550-0800",
        "status": "SUCCESS",
        "type": "WorkflowRun"
    }
    
# Get Pipeline run nodes
    curl -v  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/
    
    [
        {
            "displayName": "build",
            "edges": [
                {
                    "durationInMillis": 234,
                    "id": "9"
                }
            ],
            "id": "3",
            "startTime": "2016-03-11T00:32:52.273-0800",
            "status": "SUCCESS",
            "state": "FINISHED"
        },
        {
            "displayName": "test",
            "edges": [
                {
                    "durationInMillis": 4,
                    "id": "13"
                },
                {
                    "durationInMillis": 6,
                    "id": "14"
                },
                {
                    "durationInMillis": 9,
                    "id": "15"
                }
            ],
            "id": "9",
            "startTime": "2016-03-11T00:32:52.507-0800",
            "status": "SUCCESS",
            "state": "FINISHED"
        },
        {
            "displayName": "unit",
            "edges": [
                {
                    "durationInMillis": 161,
                    "id": "35"
                }
            ],
            "id": "13",
            "startTime": "2016-03-11T00:32:52.511-0800",
            "status": "SUCCESS",
            "state": "FINISHED"
        },
        {
            "displayName": "deploy",
            "edges": [],
            "id": "35",
            "startTime": "2016-03-11T00:32:52.672-0800",
            "status": "SUCCESS",
            "state": "FINISHED"
        },
        {
            "displayName": "integration",
            "edges": [
                {
                    "durationInMillis": 159,
                    "id": "35"
                }
            ],
            "id": "14",
            "startTime": "2016-03-11T00:32:52.513-0800",
            "status": "SUCCESS",
            "state": "FINISHED"
        },
        {
            "displayName": "ui",
            "edges": [
                {
                    "durationInMillis": 156,
                    "id": "35"
                }
            ],
            "id": "15",
            "startTime": "2016-03-11T00:32:52.516-0800",
            "status": "SUCCESS",
            "state": "FINISHED"
        }
    ]

# Get a Pipeline run node's detail

    curl -v  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/3
    
    {
        "displayName": "build",
        "edges": [
            {
                "durationInMillis": 234,
                "id": "9"
            }
        ],
        "id": "3",
        "startTime": "2016-03-11T00:32:52.273-0800",
        "status": "SUCCESS",
        "state": "FINISHED"
    }
        
# Fetching logs

Clients should look for HTTP header *X-TEXT-SIZE* and *X-More-Data* in the response. 

* X-More-Data

If *X-More-Data* is true, then client should repeat the request after some delay. In the repeated request it should use 
*X-TEXT-SIZE* header value with *start* query parameter.       

* X-TEXT-SIZE

X-TEXT-SIZE is the byte offset of the raw log file client should use in the next request as value of start query parameter.

* start

start query parameter tells API to send log starting from this offset in the log file.

    
## Get Log for a Pipeline run

    curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/log?start=0
    
    Content-Type: text/plain; charset=utf-8
    X-Text-Size: 1835
    X-More-Data: false

    Started
    [Pipeline] Allocate node : Start
    Running on master in /var/folders/5q/51y3qf0x5t39d4c4c_c2l1s40000gn/T/hudson6188345779815397724test/workspace/pipeline1
    [Pipeline] node {
    [Pipeline] stage (Build1)
    Entering stage Build1
    Proceeding
    [Pipeline] echo
    Building
    [Pipeline] stage (Test1)
    Entering stage Test1
    Proceeding
    [Pipeline] echo
    Testing
    [Pipeline] } //node
    [Pipeline] Allocate node : End
    [Pipeline] End of Pipeline
    Finished: SUCCESS

> Note: Fetching log on a Multi-Branch project will give 404 as a Multi-Branch project doesn't have run of it's own, it's essetnailly a folder hence no logs.

## Get Log for a pipeline node

    curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/3/log
    
    Content-Type: text/plain; charset=utf-8
    X-Text-Size: 164
    Content-Length: 168
    Server: Jetty(6.1.26)
    
    Entering stage build
    Proceeding
    Running on master in /var/folders/5q/51y3qf0x5t39d4c4c_c2l1s40000gn/T/hudson8758973583122932916test/workspace/pipeline1
    Building...

# Usage

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

    curl -v -X GET  "http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/test1"

    {
      "organization" : "jenkins",
      "name" : "test1",
      "branches" : [ ]
    }

## Get Pipelines

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/
    
    [ 
      {
        "organization" : "jenkins",
        "name" : "test1",
        "branches" : [ ]
      } 
    ]
    
## Get all runs in a pipeline
    
    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/test1/runs
    
    [ 
      {
        "id" : "2",
        "pipeline" : "test1",
        "organization" : "jenkins",
        "result" : "SUCCESSFUL",
        "state" : "FINISHED",
        "startTime" : "2016-02-19T11:14:53.074Z",
        "enQueueTime" : "2016-02-19T11:14:53.072Z",
        "endTime" : "2016-02-19T11:14:53.109Z",
        "durationInMillis" : 35,
        "runSummary" : "stable",
        "type" : "FreeStyleBuild"
      }, {
        "id" : "1",
        "pipeline" : "test1",
        "organization" : "jenkins",
        "result" : "SUCCESSFUL",
        "state" : "FINISHED",
        "startTime" : "2016-02-18T19:39:36.679Z",
        "enQueueTime" : "2016-02-18T19:39:36.674Z",
        "endTime" : "2016-02-18T19:39:36.747Z",
        "durationInMillis" : 68,
        "runSummary" : "stable",
        "type" : "FreeStyleBuild"
      } 
    ]
    

## Get a run details

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/test2/runs/1    
    
    {
        "id" : "1",
        "pipeline" : "test2",
        "organization" : "jenkins",
        "result" : "SUCCESSFUL",
        "state" : "FINISHED",
        "startTime" : "2016-02-19T11:14:41.414Z",
        "enQueueTime" : "2016-02-19T11:14:41.411Z",
        "endTime" : "2016-02-19T11:14:41.482Z",
        "durationInMillis" : 68,
        "runSummary" : "stable",
        "type" : "FreeStyleBuild"
      }
    }      

## Find latest run of a pipeline

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/?q=type:run;organization:jenkins;pipeline:test1;latestOnly:true
    
    [ {
        "id" : "2",
        "pipeline" : "test1",
        "organization" : "jenkins",
        "result" : "SUCCESSFUL",
        "state" : "FINISHED",
        "startTime" : "2016-02-19T11:14:53.074Z",
        "enQueueTime" : "2016-02-19T11:14:53.072Z",
        "endTime" : "2016-02-19T11:14:53.109Z",
        "durationInMillis" : 35,
        "runSummary" : "stable",
        "type" : "FreeStyleBuild"
        }
      } 
    ]

## Find latest run on all pipelines

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/?q=type:run;organization:jenkins;latestOnly:true
    
    [ {
        "id" : "2",
        "pipeline" : "test1",
        "organization" : "jenkins",
        "result" : "SUCCESSFUL",
        "state" : "FINISHED",
        "startTime" : "2016-02-19T11:14:53.074Z",
        "enQueueTime" : "2016-02-19T11:14:53.072Z",
        "endTime" : "2016-02-19T11:14:53.109Z",
        "durationInMillis" : 35,
        "runSummary" : "stable",
        "type" : "FreeStyleBuild"
        }
      },
      {
        "id" : "2",
        "pipeline" : "test2",
        "organization" : "jenkins",
        "result" : "SUCCESSFUL",
        "state" : "FINISHED",
        "startTime" : "2016-02-19T11:14:53.074Z",
        "enQueueTime" : "2016-02-19T11:14:53.072Z",
        "endTime" : "2016-02-19T11:14:53.109Z",
        "durationInMillis" : 35,
        "runSummary" : "stable",
        "type" : "FreeStyleBuild"
      }       
    ]

# MultiBranch Pipeline

Create MultiBranch build and set it up with your git repo. Your git repo must have Jenkinsfile with build script. 
Each branch in the repo with Jenkins file will appear as a branch in this pipeline.

## Get MultiBranch pipeline 

    curl -v http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/
    
    {
        "displayName": "p",
        "name": "p",
        "organization": "jenkins",
        "branches": [
            "master",
            "feature1",
            "feature2"
        ],
        "numberOfFailingBranches": 0,
        "numberOfSuccessfulBranches": 3,
        "totalNumberOfBranches": 3,
        "weatherScore":100
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
    
    curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/feature2/runs/1
    
    {
        "durationInMillis": 1578,
        "enQueueTime": "2016-02-26T19:07:00.183Z",
        "endTime": "2016-02-26T19:07:01.765Z",
        "id": "1",
        "organization": "jenkins",
        "pipeline": "feature2",
        "runSummary": "stable",
        "startTime": "2016-02-26T19:07:00.187Z",
        "result": "SUCCESS",
        "state" : "FINISHED",
        "type": "WorkflowRun",
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
            "status": "SUCCESS"
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
            "status": "SUCCESS"
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
            "status": "SUCCESS"
        },
        {
            "displayName": "deploy",
            "edges": null,
            "id": "35",
            "startTime": "2016-03-11T00:32:52.672-0800",
            "status": "SUCCESS"
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
            "status": "SUCCESS"
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
            "status": "SUCCESS"
        }
    ]
    
# Get Log for a run

Clients should look for HTTP header *X-TEXT-SIZE* and *X-More-Data* in the response. 

* X-More-Data

If *X-More-Data* is true, then client should repeat the request after some delay. In the repeated request it should use 
*X-TEXT-SIZE* header value with *start* query parameter.       

* X-TEXT-SIZE

X-TEXT-SIZE is the byte offset of the raw log file client should use in the next request as value of start query parameter.

* start

start query parameter tells API to send log starting from this offset in the log file.
 

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

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
        "status" : "SUCCESSFUL",
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
        "status" : "SUCCESSFUL",
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
        "status" : "SUCCESSFUL",
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
        "status" : "SUCCESSFUL",
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
        "status" : "SUCCESSFUL",
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
        "status" : "SUCCESSFUL",
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
            "weatherScore":100
        },
        {
            "name": "feature1",
            "weatherScore":100
        },
        {
            "name": "feature2",
            "weatherScore":100
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
        "status": "SUCCESS",
        "type": "WorkflowRun",
        "changeSet": []
    }

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

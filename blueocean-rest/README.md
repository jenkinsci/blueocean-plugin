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
      "run" : {
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
        "totalNumberOfBranches": 3
    }
    
## Get MultiBranch pipeline branches 

    curl -v http://localhost:56720/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches
    
    [
        {
            "name": "master"
        },
        {
            "name": "feature1"
        },
        {
            "name": "feature2"
        }
    ]
    
## Get MultiBranch job's branch run detail
    
    curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/feature2/runs/1
    
    {
        "branch": null,
        "commitId": null,
        "durationInMillis": 1578,
        "enQueueTime": "2016-02-26T19:07:00.183Z",
        "endTime": "2016-02-26T19:07:01.765Z",
        "id": "1",
        "organization": "jenkins",
        "pipeline": "feature2",
        "runSummary": "stable",
        "startTime": "2016-02-26T19:07:00.187Z",
        "status": "SUCCESS",
        "type": "WorkflowRun"
    }

# Usage

## Run embedded-driver

    cd embedded-driver
    mvn hpi:run
    
This will launch jenkins with BO plugin. 

BlueOcean UI is available at:
    
    http://localhost:8080/jenkins/bo
    

BlueOcean rest API base URL is:
    
    http://localhost:8080/jenkins/bo/rest


## Get a user

    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/users/alice 
    
    {
      "user" : {
        "id" : "alice",
        "name" : "Alice"
      }
    }

## Get user details

    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/users/alice\?details\=true    
    
    {
      "userDetails" : {
        "id" : "alice",
        "name" : "Alice",
        "email" : "none"
      }
    }

> Backend needs to send email of user if present

## Find users in an organization

    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/search\?q\=type:user\;organization:jenkins
    
    {
      "users" : [ {
        "id" : "alice",
        "name" : "Alice"
      } ]
    }

## Get organization details

    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/organizations/jenkins
    
    {
      "organization" : {
        "name" : "jenkins"
      }
    }

## Get a Pipeline

    curl -v -X GET  "http://localhost:8080/jenkins/bo/rest/organizations/jenkins/pipelines/test1"

    {
      "pipeline" : {
        "organization" : "jenkins",
        "name" : "test1",
        "branches" : [ ]
      }
    }

## Get Pipelines

    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/organizations/jenkins/pipelines
    
    {
      "pipelines" : [ {
        "organization" : "jenkins",
        "name" : "test1",
        "branches" : [ ]
      } ]
    }
    
## Get all runs in a pipeline
    
    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/organizations/jenkins/pipelines/test1/runs
    
    {
      "runs" : [ {
        "id" : "2",
        "pipeline" : "test1",
        "organization" : "jenkins",
        "status" : "SUCCESSFUL",
        "startTime" : "2016-02-19T11:14:53.074Z",
        "enQueueTime" : "2016-02-19T11:14:53.072Z",
        "endTime" : "2016-02-19T11:14:53.109Z",
        "durationInMillis" : 35,
        "runSummary" : "stable",
        "result" : {
          "type" : "FreeStyleBuild",
          "data" : { }
        }
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
        "result" : {
          "type" : "FreeStyleBuild",
          "data" : { }
        }
      } ]
    }  
    

## Get latest run of a pipeline

    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/organizations/jenkins/pipelines/test1/runs\?latestOnly\=true
    
    {
      "runs" : [ {
        "id" : "2",
        "pipeline" : "test1",
        "organization" : "jenkins",
        "status" : "SUCCESSFUL",
        "startTime" : "2016-02-19T11:14:53.074Z",
        "enQueueTime" : "2016-02-19T11:14:53.072Z",
        "endTime" : "2016-02-19T11:14:53.109Z",
        "durationInMillis" : 35,
        "runSummary" : "stable",
        "result" : {
          "type" : "FreeStyleBuild",
          "data" : { }
        }
      } ]
    }

## Get a run details

    curl -v -X GET  http://localhost:8080/jenkins/bo/rest/organizations/jenkins/pipelines/test2/runs/1    
    
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
        "result" : {
          "type" : "FreeStyleBuild",
          "data" : { }
        }
      }
    }      

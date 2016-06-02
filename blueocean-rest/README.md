# REST api for Blue Ocean components

This defines the http/REST like interface that Blue Ocean components use. 

# Usage

## Crumbs

Jenkins usually requires a "crumb" with posted requests to prevent request forgery and other shenanigans. 
To avoid needing a crumb to POST data, the header `Content-Type: application/json` *must* be used.

## Run Blue Ocean plugin

    cd bluecoean-plugin
    mvn hpi:run
    
This will launch a development Jenkins instance with the Blue Ocean plugin and this plugin ready to go. 

BlueOcean UI is available at:
    
    http://localhost:8080/jenkins/blue
    

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

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/users/
    
    [ 
      {
        "id" : "alice",
        "name" : "Alice"
      } 
    ]

$$
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
      "weatherScore": 100,
      "estimatedDurationInMillis": 20264,
      "lastSuccessfulRun": "http://localhost:64106/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1",
      "latestRun": {
          "changeSet": [],
          "artifacts": [
              {
                  "name": "fizz",
                  "size": 8,
                  "url": "/jenkins/job/pipeline1/1/artifact/dir/fizz"
              }
          ],
          "durationInMillis": 20264,
          "estimatedDurationInMillis" : 567,
          "enQueueTime": "2016-04-11T17:44:28.342+1000",
          "endTime": "2016-04-11T17:44:48.608+1000",
          "id": "3",
          "organization": "jenkins",
          "pipeline": "mypipe1",
          "result": "SUCCESS",
          "runSummary": "stable",
          "startTime": "2016-04-11T17:44:28.344+1000",
          "state": "FINISHED",
          "type": "WorkflowRun",
          "commitId": null
        }
    }

## Get Pipelines

    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/
    
    [ 
      {
      "organization" : "jenkins",
      "name" : "pipeline1",
      "displayName": "pipeline1",
      "weatherScore": 100,
      "estimatedDurationInMillis": 280,
      } 
    ]
    
## Get all runs in a pipeline
    
    curl -v -X GET  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs
    
    [
        {
            "changeSet": [],
            "artifacts": [
              {
                  "name": "fizz",
                  "size": 8,
                  "url": "/jenkins/job/pipeline1/1/artifact/dir/fizz"
              }
            ],
            "durationInMillis": 841,
            "estimatedDurationInMillis" : 567,
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
        "artifacts": [
          {
              "name": "fizz",
              "size": 8,
              "url": "/jenkins/job/pipeline1/1/artifact/dir/fizz"
          }
        ],
        "durationInMillis": 841,
        "estimatedDurationInMillis" : 567,
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
          "artifacts": [
            {
                "name": "fizz",
                "size": 8,
                "url": "/jenkins/job/pipeline1/1/artifact/dir/fizz"
            }
          ],
          "durationInMillis": 841,
          "estimatedDurationInMillis" : 567,
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
          "artifacts": [
            {
                "name": "fizz",
                "size": 8,
                "url": "/jenkins/job/pipeline1/1/artifact/dir/fizz"
            }
          ],
          "durationInMillis": 841,
          "estimatedDurationInMillis" : 567,
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
        "estimatedDurationInMillis": 280,
        "latestRun": null,
        "name": "p",
        "organization": "jenkins",
        "weatherScore": 100,
        "branchNames": [
            "feature2",
            "master",
            "feature1"
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
            "displayName": "feature2",
            "estimatedDurationInMillis": 1391,
            "name": "master",
            "weatherScore":100,
             "lastSuccessfulRun": "http://localhost:63971/jenkins/blue/rest/organizations/jenkins/pipelines/p/branches/master/runs/1",
            "latestRun": {
                "changeSet": [
                    
                ],
                "durationInMillis": 1391,
                "estimatedDurationInMillis" : 567,
                "enQueueTime": "2016-04-15T19:59:28.717-0700",
                "endTime": "2016-04-15T19:59:30.114-0700",
                "id": "1",
                "organization": "jenkins",
                "pipeline": "feature2",
                "result": "SUCCESS",
                "runSummary": "stable",
                "startTime": "2016-04-15T19:59:28.723-0700",
                "state": "FINISHED",
                "type": "WorkflowRun",
                "commitId": "662766a80af35404c430240e6996598d5397471e"
            },
            "name": "feature2",
            "organization": "jenkins",
            "weatherScore": 100,
            "pullRequest": null
        },
        {
            "displayName": "master",
            "estimatedDurationInMillis": 1468,
            "name": "feature1",
            "weatherScore":100,
            "lastSuccessfulRun": "http://localhost:64077/jenkins/blue/rest/organizations/jenkins/pipelines/p/branches/feature1/runs/1",            
            "latestRun": {
                "changeSet": [
                    
                ],
                "artifacts": [
                  {
                      "name": "fizz",
                      "size": 8,
                      "url": "/jenkins/job/pipeline1/1/artifact/dir/fizz"
                  }
                ],
                "durationInMillis": 1468,
                "estimatedDurationInMillis" : 567,
                "enQueueTime": "2016-04-15T19:59:28.730-0700",
                "endTime": "2016-04-15T19:59:30.199-0700",
                "id": "1",
                "organization": "jenkins",
                "pipeline": "master",
                "result": "SUCCESS",
                "runSummary": "stable",
                "startTime": "2016-04-15T19:59:28.731-0700",
                "state": "FINISHED",
                "type": "WorkflowRun",
                "commitId": "96e0a0f29d9e5b1381ebb1b7503b0be04ed19a5b"
            },
            "name": "master",
            "organization": "jenkins",
            "weatherScore": 100,
            "pullRequest": null
        },
        {
            "displayName": "feature1",
            "estimatedDurationInMillis": 1443,
            "name": "feature2",
            "weatherScore":100,
            "lastSuccessfulRun": "http://localhost:64077/jenkins/blue/rest/organizations/jenkins/pipelines/p/branches/feature2/runs/1",            
            "latestRun": {
                "changeSet": [
                    
                ],
                "durationInMillis": 1443,
                "estimatedDurationInMillis" : 567,
                "enQueueTime": "2016-04-15T19:59:28.723-0700",
                "endTime": "2016-04-15T19:59:30.167-0700",
                "id": "1",
                "organization": "jenkins",
                "pipeline": "feature1",
                "result": "SUCCESS",
                "runSummary": "stable",
                "startTime": "2016-04-15T19:59:28.724-0700",
                "state": "FINISHED",
                "type": "WorkflowRun",
                "commitId": "f436952a7de493603f4937ecb9dac3f79fd13c79"
            },
            "name": "feature1",
            "organization": "jenkins",
            "weatherScore": 100,
            "pullRequest": null
        }
    ]
    
    
## Get MultiBranch job's branch run detail
    
    curl -v http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/feature1/runs/1
    
    {
        "durationInMillis": 1330,
        "estimatedDurationInMillis" : 567,
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
            "artifacts": [
              {
                  "name": "fizz",
                  "size": 8,
                  "url": "/jenkins/job/pipeline1/1/artifact/dir/fizz"
              }
            ],
            "durationInMillis": 1875,
            "estimatedDurationInMillis" : 567,
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
            "estimatedDurationInMillis" : 567,
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
            "estimatedDurationInMillis" : 567,
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
        "estimatedDurationInMillis" : 567,
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
    
    [ {
      "displayName" : "build",
      "durationInMillis" : 219,
      "edges" : [ {
        "id" : "9"
      } ],
      "id" : "3",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:15:08.719-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "test",
      "durationInMillis" : 158,
      "edges" : [ {
        "id" : "13"
      }, {
        "id" : "14"
      }, {
        "id" : "15"
      } ],
      "id" : "9",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:15:08.938-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "unit",
      "durationInMillis" : 127,
      "edges" : [ {
        "id" : "35"
      } ],
      "id" : "13",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:15:08.942-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "integration",
      "durationInMillis" : 126,
      "edges" : [ {
        "id" : "35"
      } ],
      "id" : "14",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:15:08.944-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "ui",
      "durationInMillis" : 137,
      "edges" : [ {
        "id" : "35"
      } ],
      "id" : "15",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:15:08.945-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "deploy",
      "durationInMillis" : 47,
      "edges" : [ ],
      "id" : "35",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:15:09.096-0700",
      "state" : "FINISHED"
    } ]

> In case pipeline run fails in one of the parallel branch, enclosing stage node will appear failed as well.

> In case if the pipeline is in progress or failed in the middle, the response may include future nodes if there was 
  last successful pipeline build. The returned future nodes will have startTime, result and state as null. 
  Also the last node's edges will be patched to point to the future node. 

From the above example, if build failed at parallel node *unit* then the response will be:

    [ {
      "displayName" : "build",
      "durationInMillis" : 51,
      "edges" : [ {
        "id" : "9"
      } ],
      "id" : "3",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:39:18.569-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "test",
      "durationInMillis" : 344,
      "edges" : [ {
        "id" : "13"
      }, {
        "id" : "14"
      }, {
        "id" : "15"
      } ],
      "id" : "9",
      "result" : "FAILURE",
      "startTime" : "2016-05-06T15:39:18.620-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "unit",
      "durationInMillis" : 329,
      "edges" : [ {
        "id" : "35"
      } ],
      "id" : "13",
      "result" : "FAILURE",
      "startTime" : "2016-05-06T15:39:18.622-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "integration",
      "durationInMillis" : 97,
      "edges" : [ {
        "id" : "35"
      } ],
      "id" : "14",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:39:18.623-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "ui",
      "durationInMillis" : 107,
      "edges" : [ {
        "id" : "35"
      } ],
      "id" : "15",
      "result" : "SUCCESS",
      "startTime" : "2016-05-06T15:39:18.623-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "deploy",
      "durationInMillis" : null,
      "edges" : [ {
        "id" : "41"
      } ],
      "id" : "35",
      "result" : null,
      "startTime" : null,
      "state" : null
    }, {
      "displayName" : "deployToProd",
      "durationInMillis" : null,
      "edges" : [ ],
      "id" : "41",
      "result" : null,
      "startTime" : null,
      "state" : null
    } ]

# Get a Pipeline run node's detail

    curl -v  http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/3
    
    {
        "displayName": "build",
        "edges": [
            {
                "id": "9"
            }
        ],
        "id": "3",
        "startTime": "2016-03-11T00:32:52.273-0800",
        "status": "SUCCESS",
        "state": "FINISHED"
    }

# Get Pipeline node's steps

This API gives steps inside a pipeline node. For Stage, the steps will include all the steps defined inside Parallels as well as Nodes.

        
### Get steps for a Pipeline node

Given this pipeline script:

    stage 'build'
    node{
      echo "Building..."
    }
    
    stage 'test'
    parallel 'unit':{
      node{
        echo "Unit testing..."
      }
    },'integration':{
      node{
        echo "Integration testing..."
      }
    }, 'ui':{
      node{
        echo "UI testing..."
      }
    }
    
    stage 'deploy'
    node{
      echo "Deploying"
    }
    
    stage 'deployToProd'
    node{
      echo "Deploying to production"
    }        


Get steps of 'test' stage node:

    GET http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/9/steps/

    [ {
      "displayName" : "Print Message",
      "durationInMillis" : 1,
      "id" : "21",
      "result" : "SUCCESS",
      "startTime" : "2016-05-13T09:37:01.230-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "Shell Script",
      "durationInMillis" : 2,
      "id" : "22",
      "result" : "SUCCESS",
      "startTime" : "2016-05-13T09:37:01.231-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "Print Message",
      "durationInMillis" : 1,
      "id" : "23",
      "result" : "SUCCESS",
      "startTime" : "2016-05-13T09:37:01.233-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "Print Message",
      "durationInMillis" : 1,
      "id" : "28",
      "result" : "SUCCESS",
      "startTime" : "2016-05-13T09:37:01.266-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "Shell Script",
      "durationInMillis" : 272,
      "id" : "32",
      "result" : "SUCCESS",
      "startTime" : "2016-05-13T09:37:01.474-0700",
      "state" : "FINISHED"
    }, {
      "displayName" : "Print Message",
      "durationInMillis" : 2,
      "id" : "39",
      "result" : "SUCCESS",
      "startTime" : "2016-05-13T09:37:01.784-0700",
      "state" : "FINISHED"
    } ]
                
### Get a Pipeline step details

    GET http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/13/steps/21/
    {
      "displayName" : "Print Message",
      "durationInMillis" : 1,
      "id" : "21",
      "result" : "SUCCESS",
      "startTime" : "2016-05-13T09:37:01.230-0700",
      "state" : "FINISHED"
    }
                       
# Fetching logs

Clients should look for HTTP header *X-TEXT-SIZE* and *X-More-Data* in the response.
 
 By default only last 150 KB log data is returned in the response. You can fetch full log by sending start=0 query 
 parameter. You can override default log size from 150KB to other values using thresholdInKB query parameter. 

* X-More-Data Header

If *X-More-Data* is true, then client should repeat the request after some delay. In the repeated request it should use 
*X-TEXT-SIZE* header value with *start* query parameter.       

* X-TEXT-SIZE Header

X-TEXT-SIZE is the byte offset of the raw log file client should use in the next request as value of start query parameter.

* start Query Parameter

start query parameter tells API to send log starting from this offset in the log file. 

* thresholdInKB Query Parameter

Size of log to return in the response. Default value is 150 KB of log data.


    
## Get log for a Pipeline run

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

### Get log for a Pipeline step

    GET http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/runs/1/nodes/13/steps/21/log/
    
    Unit testing...


## Favorite a pipeline
Returns 200 on success. Must be authenticated.

    curl -u bob:bob -H"Content-Type:application/json" -XPUT -d '{"favorite":true} ttp://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/favorite

## Favorite a multibranch pipeline
Must be authenticated.

This favorites the master branch. Returns 200 on success. 500 if master does not exist

    curl -u bob:bob  -H"Content-Type:application/json" -XPUT -d '{"favorite":true} http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/favorite

## Favorite a multibranch pipeline branch
Returns 200 on success. Must be authenticated.

    curl -H"Content-Type:application/json" -XPUT -d '{"favorite":true} http://localhost:56748/jenkins/blue/rest/organizations/jenkins/pipelines/pipeline1/branches/master/favorite


## Fetch user favorites
Must be authenticated.

    curl -u bob:bob  http://localhost:8080/jenkins/blue/rest/users/bob/favorites/

    [{
        "pipeline":"/organizations/jenkins/pipelines/pipeline1"
    }]

## Stop a build
Note it takes a while to stop, so you may get a state of RUNNING or QUEUED.

    curl -X PUT http://localhost:8080/jenkins/blue/rest/organiations/jenkins/pipelines/pipeline1/runs/1/stop
    {
      "result" : "ABORTED",
      "state" : "FINISHED"
    }

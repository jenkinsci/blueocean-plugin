export const latestRuns = [{
    '_class': 'some.class',
    '_links': {
        'self': {
            '_class': 'io.jenkins.blueocean.rest.hal.Link',
            'href': '/blue/rest/organizations/jenkins/pipelines/morebeers/'
        },
    },
    'latestRun': {
        '_links': {
            'self': {
                'href': 'test'
            }
        },
        'artifacts':[
            {
                'name':'hey',
                'path': 'hey',
                'size':4,
                'downloadable': true,
                'url':'/jenkins/job/jenkinsfile-experiments/branch/master/1/artifact/hey'
            }
        ],
        'changeSet':[{
            'author': {
                'email': 'tscherler@cloudbees.com',
                'fullName': 'tscherler',
                'id': 'tscherler'
            },
            'affectedPaths': [
                'Jenkinsfile'
            ],
            'checkoutCount': '0',
            'commitId': '21552ff3072684cc1d593376b7cc7023deb15e1c',
            'comment': 'Update Jenkinsfile\n',
            'date': '2016-03-15 00:35:58 +0100',
            'id': '21552ff3072684cc1d593376b7cc7023deb15e1c',
            'msg': 'Update Jenkinsfile',
            'paths': [{
                'editType': 'edit',
                'file': 'Jenkinsfile'
            }],
            'timestamp': '2016-03-15T00:35:58.000+0100'
        }, {
            'author': {
                'email': 'tscherler@cloudbees.com',
                'fullName': 'tscherler',
                'id': 'tscherler'
            },
            'affectedPaths': [
                'Jenkinsfile'
            ],
            'checkoutCount': '0',
            'commitId': 'eb92e0df899ff742b002a3ea652f4eb406ea83b8',
            'comment': 'Update Jenkinsfile\n',
            'date': '2016-03-15 00:33:40 +0100',
            'id': 'eb92e0df899ff742b002a3ea652f4eb406ea83b8',
            'msg': 'Update Jenkinsfile',
            'paths': [{
                'editType': 'edit',
                'file': 'Jenkinsfile'
            }],
            'timestamp': '2016-03-15T00:33:40.000+0100'
        }],
        'durationInMillis': 18678,
        'enQueueTime': '2016-03-15T00:38:33.890+0100',
        'endTime': '2016-03-15T00:38:52.606+0100',
        'id': '2',
        'organization': 'jenkins',
        'pipeline': 'master',
        'result': 'SUCCESS',
        'runSummary': 'stable',
        'startTime': '2016-03-15T00:38:33.928+0100',
        'state': 'FINISHED',
        'type': 'WorkflowRun',
        'commitId': '09794ca7e2e98cdc5e2f0f02117d79f5b112c7c1',
        "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
    },
    'name': 'master',
    'weatherScore': 100,
    'pullRequest': null
}, {
    '_class': 'some.class',
    '_links': {
        'self': {
            '_class': 'io.jenkins.blueocean.rest.hal.Link',
            'href': '/blue/rest/organizations/jenkins/pipelines/morebeers/'
        },
    },
    'latestRun': {
        'changeSet': [],
        'durationInMillis': 25636,
        'enQueueTime': '2016-03-15T00:38:35.964+0100',
        'endTime': '2016-03-15T00:39:01.616+0100',
        'id': '1',
        'organization': 'jenkins',
        'pipeline': 'PR-6',
        'result': 'FAILURE',
        'runSummary': 'broken since this build',
        'startTime': '2016-03-15T00:38:35.980+0100',
        'state': 'FINISHED',
        'type': 'WorkflowRun',
        'commitId': 'c38ab8e9275c2ee03e14e1bbfc9f44f2eddcff19',
        "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
    },
    'name': 'PR-6',
    'weatherScore': 0,
    'pullRequest': {
        'author': 'target-x',
        'id': '6',
        'title': 'Feature/yyy',
        'url': 'https://github.com/cloudbees/blueocean-pr-testing/pull/6'
    }
}];

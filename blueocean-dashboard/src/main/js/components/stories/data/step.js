/* eslint-disable */
export const step = {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineStepImpl',
    '_links': {
        'self': {
            '_class': 'io.jenkins.blueocean.rest.hal.Link',
            'href': '/blue/rest/organizations/jenkins/pipelines/pause/runs/3/nodes/12/steps/14/',
        },
        'actions': {
            '_class': 'io.jenkins.blueocean.rest.hal.Link',
            'href': '/blue/rest/organizations/jenkins/pipelines/pause/runs/3/nodes/12/steps/14/actions/',
        },
    },
    'actions': [{
        '_class': 'io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl',
        '_links': {
            'self': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/pause/runs/3/nodes/12/steps/14/log/',
            },
        },
        '_class': 'org.jenkinsci.plugins.workflow.support.actions.LogActionImpl',
        'urlName': 'log',
    }, {
        '_class': 'io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl',
        '_links': { 'self': null },
        '_class': 'org.jenkinsci.plugins.workflow.support.actions.PauseAction',
        'urlName': null,
    }, {
        '_class': 'io.jenkins.blueocean.service.embedded.rest.ActionProxiesImpl',
        '_links': { 'self': null },
        '_class': 'org.jenkinsci.plugins.workflow.actions.TimingAction',
        'urlName': null,
    }],
    'displayName': 'Wait for interactive input',
    'durationInMillis': 263614020,
    'id': '14',
    'input': {
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.InputStepImpl',
        '_links': {
            'self': {
                '_class': 'io.jenkins.blueocean.rest.hal.Link',
                'href': '/blue/rest/organizations/jenkins/pipelines/pause/runs/3/nodes/12/steps/14/input/',
            },
        },
        'id': 'CustomIdHere',
        'message': 'this is a message to user',
        'ok': 'Go ahead',
        'parameters': [{
            '_class': 'hudson.model.BooleanParameterDefinition',
            'defaultParameterValue': {
                '_class': 'hudson.model.BooleanParameterValue',
                'name': 'thisIsBool',
                'value': true,
            },
            'description': 'yes or no',
            'name': 'thisIsBool',
            'type': 'BooleanParameterDefinition',
        }, {
            '_class': 'hudson.model.ChoiceParameterDefinition',
            'defaultParameterValue': {
                '_class': 'hudson.model.StringParameterValue',
                'name': 'This is choice',
                'value': 'c1',
            },
            'description': 'this is choice description',
            'name': 'This is choice',
            'type': 'ChoiceParameterDefinition',
            'choices': ['c1', 'c2'],
        }, {
            '_class': 'hudson.model.TextParameterDefinition',
            'defaultParameterValue': {
                '_class': 'hudson.model.StringParameterValue',
                'name': 'This is a multi line string',
                'value': 'default',
            },
            'description': 'Long text goes here',
            'name': 'This is a multi line string',
            'type': 'TextParameterDefinition',
        }, {
            '_class': 'hudson.model.StringParameterDefinition',
            'defaultParameterValue': {
                '_class': 'hudson.model.StringParameterValue',
                'name': 'this is a string parameter',
                'value': 'yeah',
            },
            'description': 'string parameter desc',
            'name': 'this is a string parameter',
            'type': 'StringParameterDefinition',
        }, {
            '_class': 'hudson.model.PasswordParameterDefinition',
            'defaultParameterValue': {
                '_class': 'hudson.model.PasswordParameterValue',
                'name': 'password param',
            },
            'description': 'password param desc',
            'name': 'password param',
            'type': 'PasswordParameterDefinition',
        }],
        'submitter': null,
    },
    'result': 'UNKNOWN',
    'startTime': '2016-12-16T14:07:46.411+0100',
    'state': 'PAUSED',
};

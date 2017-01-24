export const runNodesFail = [{
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'Stage 1',
    durationInMillis: 10263,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '12'
    }],
    id: '5',
    result: 'SUCCESS',
    startTime: '2016-05-24T13:42:07.833+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'Stage 2',
    durationInMillis: 22141,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '15'
    }, {
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '16'
    }],
    id: '12',
    result: 'FAILURE',
    startTime: '2016-05-24T13:42:18.096+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'firstBranch',
    durationInMillis: 22074,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '28'
    }],
    id: '15',
    result: 'SUCCESS',
    startTime: '2016-05-24T13:42:18.098+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'secondBranch',
    durationInMillis: 22076,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '28'
    }],
    id: '16',
    result: 'FAILURE',
    startTime: '2016-05-24T13:42:18.099+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'deploy',
    durationInMillis: null,
    edges: [],
    id: '28',
    result: null,
    startTime: null,
    state: null
}];

export const runNodesSuccess = [{
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'Stage 1',
    durationInMillis: 10260,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '12'
    }],
    id: '5',
    result: 'SUCCESS',
    startTime: '2016-05-24T13:34:49.234+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'Stage 2',
    durationInMillis: 22161,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '15'
    }, {
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '16'
    }],
    id: '12',
    result: 'SUCCESS',
    startTime: '2016-05-24T13:34:59.494+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'firstBranch',
    durationInMillis: 22081,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '28'
    }],
    id: '15',
    result: 'SUCCESS',
    startTime: '2016-05-24T13:34:59.497+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'secondBranch',
    durationInMillis: 22079,
    edges: [{
        '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl$EdgeImpl',
        id: '28'
    }],
    id: '16',
    result: 'SUCCESS',
    startTime: '2016-05-24T13:34:59.499+0200',
    state: 'FINISHED'
}, {
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeImpl',
    displayName: 'deploy',
    durationInMillis: 10211,
    edges: [],
    id: '28',
    result: 'SUCCESS',
    startTime: '2016-05-24T13:35:21.655+0200',
    state: 'FINISHED'
}];

export const runNodesRunning = [{
    '_class': 'io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl',
    artifacts: [],
    changeSet: [],
    durationInMillis: 0,
    enQueueTime: '2016-05-25T11:37:02.175+0200',
    endTime: null,
    estimatedDurationInMillis: 483908,
    id: '2',
    organization: 'jenkins',
    pipeline: 'feature%2FUX-234',
    result: 'UNKNOWN',
    runSummary: '?',
    startTime: '2016-05-25T11:37:02.249+0200',
    state: 'RUNNING',
    type: 'WorkflowRun',
    commitId: 'e14d77d0d441da163c2ca5c617ed586aa928c8f0'
}];

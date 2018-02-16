import {assert} from 'chai';
import sinon from 'sinon';

import {DefaultSSEHandler} from '../../../src/js/services/DefaultSSEHandler';


class ActivityServiceMock {
    fetchActivity(event) {
        return Promise.resolve({});
    }
}

class PipelineServiceMock {
    constructor() {
        this.data = {};
    }
    hasItem(key) {
        return !!this.data[key];
    }
    _setItem(key, item) {
        this.data[key] = item;
    }
}

class PagerServiceMock {}


const eventRunStartedPipeline = {
    blueocean_job_pipeline_name: "folder1/folder2/folder3/nested-pipeline",
    blueocean_job_rest_url: "/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/nested-pipeline/",
    blueocean_queue_item_expected_build_number: "16",
    jenkins_channel: "job",
    jenkins_event: "job_run_started",
    jenkins_event_timestamp: "1512670842446",
    jenkins_event_uuid: "9b4e135d-981c-4a8f-8199-351d197a8f83",
    jenkins_instance_url: "http://localhost:8080/jenkins/",
    jenkins_object_id: "16",
    jenkins_object_name: "#16",
    jenkins_object_type: "org.jenkinsci.plugins.workflow.job.WorkflowRun",
    jenkins_object_url: "job/folder1/job/folder2/job/folder3/job/nested-pipeline/16/",
    jenkins_org: "jenkins",
    job_name: "folder1/folder2/folder3/nested-pipeline",
    job_run_queueId: "9",
    job_run_status: "RUNNING",
    sse_subs_dispatcher: "jenkins-blueocean-core-js-1512665768044-xeni2",
    sse_subs_dispatcher_inst: "148615817",
};

const eventRunStartedMultibranch = {
    blueocean_job_branch_name: 'duration-5m',
    blueocean_job_pipeline_name: 'pipeline-samples',
    blueocean_job_rest_url: '/blue/rest/organizations/jenkins/pipelines/pipeline-samples/branches/duration-5m/',
    blueocean_queue_item_expected_build_number: '13',
    jenkins_channel: 'job',
    jenkins_event: 'job_run_started',
    jenkins_event_timestamp: '1512670672779',
    jenkins_event_uuid: 'a310d02e-a8d4-4dd4-af61-ce8c068360bb',
    jenkins_instance_url: 'http://localhost:8080/jenkins/',
    jenkins_object_id: '13',
    jenkins_object_name: '#13',
    jenkins_object_type: 'org.jenkinsci.plugins.workflow.job.WorkflowRun',
    jenkins_object_url: 'job/pipeline-samples/job/duration-5m/13/',
    jenkins_org: 'jenkins',
    job_name: 'pipeline-samples/duration-5m',
    job_run_queueId: '7',
    job_run_status: 'RUNNING',
    sse_subs_dispatcher: 'jenkins-blueocean-core-js-1512665768044-xeni2',
    sse_subs_dispatcher_inst: '148615817'
};

const pipelineSamplesJobUrl = '/blue/rest/organizations/jenkins/pipelines/pipeline-samples/';


describe('DefaultSSEHandler', () => {
    let sseHandler;
    let pipelineService;
    let activityService;
    let fetchActivitySpy;

    beforeEach(() => {
        pipelineService = new PipelineServiceMock();
        activityService = new ActivityServiceMock();
        fetchActivitySpy = sinon.spy(activityService, 'fetchActivity');
        sseHandler = new DefaultSSEHandler(pipelineService, activityService, new PagerServiceMock());
    });

    describe('handleEvents', () => {
        it('fetches activity for a loaded pipeline', () => {
            pipelineService._setItem(eventRunStartedPipeline.blueocean_job_rest_url, {});
            sseHandler.handleEvents(eventRunStartedPipeline);
            assert.isTrue(fetchActivitySpy.calledOnce);
        });

        it('does not fetch activity for an unloaded pipeline', () => {
            sseHandler.handleEvents(eventRunStartedPipeline);
            assert.isTrue(fetchActivitySpy.notCalled);
        });

        it('fetches activity for a loaded multibranch pipeline', () => {
            pipelineService._setItem(pipelineSamplesJobUrl, {});
            sseHandler.handleEvents(eventRunStartedMultibranch);
            assert.isTrue(fetchActivitySpy.calledOnce);
        });

        it('does not fetch activity for an unloaded multibranch pipeline', () => {
            sseHandler.handleEvents(eventRunStartedMultibranch);
            assert.isTrue(fetchActivitySpy.notCalled);
        });
    });

    describe('_computePipelineHref', () => {
        it('works for pipeline event', () => {
            const href = sseHandler._computePipelineHref(eventRunStartedPipeline);
            assert.equal(href, eventRunStartedPipeline.blueocean_job_rest_url);
        });

        it('works for multibranch event', () => {
            const href = sseHandler._computePipelineHref(eventRunStartedMultibranch);
            assert.equal(href, pipelineSamplesJobUrl);
        });
    });
});

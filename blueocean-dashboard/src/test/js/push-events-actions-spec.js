import React from 'react';
import { assert} from 'chai';
import { shallow } from 'enzyme';

import * as actions from '../../main/js/redux/actions';

function newEvent(type) {
    return {
        blueocean_branch_name: "quicker",
        blueocean_is_for_current_job: true,
        blueocean_is_multi_branch: true,
        blueocean_job_name: "PR-demo",
        jenkins_channel: "job",
        jenkins_event: type,
        jenkins_object_name: "CloudBeers/PR-demo/quicker",
        jenkins_object_url: "job/CloudBeers/job/PR-demo/branch/quicker/",
        job_name: "CloudBeers/PR-demo/quicker",
        jenkins_object_id: "12",
        job_run_queueId: "12",
        job_run_status: "QUEUED"
    };
}
const CONFIG = {
    getAppURLBase: function() { return '/jenkins'; }
};
const originalFetchJson = actions.fetchJson;

try {
    describe("push events - queued run tests", () => {

        // Test queued event for when the event is for the pipeline that
        // the user is actually "currently" looking at.
        it("currently displayed pipeline", () => {
            const event = newEvent('job_run_queue_enter');
            const dispatcher = actions.actions.processJobQueuedEvent(event);

            // mimic invocation of this action dispatcher and inspect the
            // actualDispatchObj passed to the dispatch function
            const dispatchedEvents = [];
            dispatcher(function(actualDispatchObj) {
                //console.log(actualDispatchObj);
                dispatchedEvents.push(actualDispatchObj);
            }, function() {
                return {
                    adminStore: {
                        runs: {
                            'PR-demo': []
                        }
                    }
                }
            });

            // The queued event should get dispatched twice:
            //  1. To update the redux store's view of the set of 'currentRuns' because
            //     the event is associated with the currently active pipeline.
            //  2. To update the redux store's view of the set of runs associated with the
            //     pipeline itself. Every time the user navs to a pipeline, the runs for
            //     that pipeline are cached in the redux store. This dispatch updates
            //     that state.
            assert.equal(dispatchedEvents.length, 2);
            assert.equal(dispatchedEvents[0].type, actions.ACTION_TYPES.SET_CURRENT_RUN_DATA);
            assert.equal(dispatchedEvents[0].payload.length, 1);
            assert.equal(dispatchedEvents[0].payload[0].pipeline, 'quicker');
            assert.equal(dispatchedEvents[0].payload[0].state, 'QUEUED');
            assert.equal(dispatchedEvents[1].type, actions.ACTION_TYPES.SET_RUNS_DATA);
            assert.equal(dispatchedEvents[1].id, 'PR-demo');
            assert.equal(dispatchedEvents[1].payload.length, 1);
            assert.equal(dispatchedEvents[1].payload[0].pipeline, 'quicker');
            assert.equal(dispatchedEvents[1].payload[0].state, 'QUEUED');
        });

        // Test queued event for when the event is for a different pipeline to
        // the one that the user is actually "currently" looking at.
        it("not currently displayed pipeline", () => {
            const event = newEvent('job_run_queue_enter');

            // modify the event to turn off the blueocean_is_for_current_job flag.
            // This should result in just one dispatch.
            event.blueocean_is_for_current_job = false;

            const dispatcher = actions.actions.processJobQueuedEvent(event);

            // mimic invocation of this action dispatcher and inspect the
            // actualDispatchObj passed to the dispatch function
            const dispatchedEvents = [];
            dispatcher(function(actualDispatchObj) {
                // console.log(actualDispatchObj);
                dispatchedEvents.push(actualDispatchObj);
            }, function() {
                return {
                    adminStore: {
                        runs: {
                            'PR-demo': []
                        }
                    }
                }
            });

            // The queued event should get dispatched once only i.e. to update the
            // "global" run state. See the previous test comments for more details.
            assert.equal(dispatchedEvents.length, 1);
            assert.equal(dispatchedEvents[0].type, actions.ACTION_TYPES.SET_RUNS_DATA);
            assert.equal(dispatchedEvents[0].id, 'PR-demo');
            assert.equal(dispatchedEvents[0].payload.length, 1);
            assert.equal(dispatchedEvents[0].payload[0].pipeline, 'quicker');
            assert.equal(dispatchedEvents[0].payload[0].state, 'QUEUED');
        });

        // Test queued event is ignored if already received. This can happen because
        // there are multiple events relating to the run queue lifecycle.
        it("ignore multiple events with same queueId", () => {

            // mimic invocation of this action dispatcher and inspect the
            // actualDispatchObj passed to the dispatch function
            const adminStore =  {runs: { 'PR-demo': [] } };

            function fireEvent() {
                const event = newEvent('job_run_queue_enter');
                // modify the event to turn off the blueocean_is_for_current_job flag.
                // This should result in max of one dispatch per call to dispatcher.
                event.blueocean_is_for_current_job = false;
                const dispatcher = actions.actions.processJobQueuedEvent(event);

                dispatcher(function (actualDispatchObj) {
                    adminStore.runs['PR-demo'] = actualDispatchObj.payload;
                }, function () {
                    return {
                        adminStore: adminStore
                    }
                });
            }

            // fire the dispatcher for the first time...
            fireEvent();
            // Should have been dispatched i.e. count === 1 ...
            assert.equal(adminStore.runs['PR-demo'].length, 1);
            // fire the dispatcher a second time (same event)...
            fireEvent();
            // Should not have been dispatched i.e. count
            // should still be 1 ...
            assert.equal(adminStore.runs['PR-demo'].length, 1);
        });
    });

    describe("push events - started run tests", () => {

        // Test run started event for when the event is for the pipeline that
        // the user is actually "currently" looking at.
        it("run fetch ok", () => {
            // Mimic the run being in the queued state before the start
            const adminStore = {
                runs: {
                    'PR-demo': [{
                        job_run_queueId: '12',
                        pipeline: 'quicker',
                        state: 'QUEUED',
                        result: 'UNKNOWN'
                    }]
                }
            };

            function fireEvent() {
                const event = newEvent('job_run_started');
                event.blueocean_is_for_current_job = false;

                // Mock the fetchJson
                actions.fetchJson = function(url, onSuccess, onError) {
                    assert.equal(url, '/jenkins/rest/organizations/jenkins/pipelines/PR-demo/branches/quicker/runs/12');
                    onSuccess({
                        "_class": "io.jenkins.blueocean.service.embedded.rest.PipelineRunImpl",
                        "artifacts": [],
                        "changeSet": [],
                        "durationInMillis": 0,
                        "enQueueTime": "2016-05-19T22:05:39.301+0100",
                        "endTime": null,
                        "estimatedDurationInMillis": 17882,
                        "id": "12",
                        "organization": "jenkins",
                        "pipeline": "quicker",
                        "result": "UNKNOWN",
                        "runSummary": "?",
                        "startTime": "2016-05-19T22:05:39.303+0100",
                        "state": "RUNNING",
                        "type": "WorkflowRun",
                        "commitId": null
                    });
                };

                const dispatcher = actions.actions.updateRunState(event, CONFIG, true);

                dispatcher(function (actualDispatchObj) {
                    adminStore.runs['PR-demo'] = actualDispatchObj.payload;
                }, function () {
                    return {
                        adminStore: adminStore
                    }
                });
            }

            // Fire the start event and then check that the run state
            // has changed as expected.
            fireEvent();

            var runs = adminStore.runs['PR-demo'];
            assert.equal(runs.length, 1);
            assert.equal(runs[0].enQueueTime, '2016-05-19T22:05:39.301+0100');
            assert.equal(runs[0].state, 'RUNNING');
        });

        it("run fetch failed", () => {
            // Mimic the run being in the queued state before the start
            const adminStore = {
                runs: {
                    'PR-demo': [{
                        job_run_queueId: '12',
                        pipeline: 'quicker',
                        state: 'QUEUED',
                        result: 'UNKNOWN'
                    }]
                }
            };

            function fireEvent() {
                const event = newEvent('job_run_started');
                event.blueocean_is_for_current_job = false;

                // Mock the fetchJson
                actions.fetchJson = function(url, onSuccess, onError) {
                    assert.equal(url, '/jenkins/rest/organizations/jenkins/pipelines/PR-demo/branches/quicker/runs/12');
                    onError({});
                };

                const dispatcher = actions.actions.updateRunState(event, CONFIG, true);

                dispatcher(function (actualDispatchObj) {
                    adminStore.runs['PR-demo'] = actualDispatchObj.payload;
                }, function () {
                    return {
                        adminStore: adminStore
                    }
                });
            }

            // Fire the start event and then check that the run state
            // has changed as expected .
            fireEvent();

            var runs = adminStore.runs['PR-demo'];
            assert.equal(runs.length, 1);
            // This time, the run state should have changed as expected
            // because we do it manually when the fetch fails, but we don't
            // see the time changes etc.
            assert.equal(runs[0].enQueueTime, undefined);
            assert.equal(runs[0].state, 'RUNNING');
        });
    });

} finally {
    actions.fetchJson = originalFetchJson;
}

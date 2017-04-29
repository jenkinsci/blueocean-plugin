import keymirror from 'keymirror';
import { applyFetchMarkers, fetch as smartFetch, paginate } from '../util/smart-fetch';
import { State } from '../components/records';
import UrlConfig from '../config';
import { getNodesInformation } from '../util/logDisplayHelper';
import {
    calculateLogUrl,
    calculateNodeBaseUrl,
    calculateStepsBaseUrl,
    getRestUrl,
    paginateUrl,
} from '../util/UrlUtils';
import findAndUpdate from '../util/find-and-update';
import { Fetch, FetchFunctions, AppConfig } from '@jenkins-cd/blueocean-core-js';
const debugLog = require('debug')('blueocean-actions-js:debug');

/**
 * This function maps a queue item into a run instancce.
 *
 * We do this because the api returns us queued items as well
 * as runs and its easier to deal with them if they are modeled
 * as the same thing. If the raw data is needed if can be fetched
 * from _item.
 */
function _mapQueueToPsuedoRun(run) {
    if (run._class === 'io.jenkins.blueocean.service.embedded.rest.QueueItemImpl') {
        return {
            id: String(run.expectedBuildNumber),
            state: 'QUEUED',
            pipeline: run.pipeline,
            type: 'QueuedItem',
            result: 'UNKNOWN',
            job_run_queueId: run.id,
            enQueueTime: run.queuedTime,
            organization: run.organization,
            changeSet: [],
            causeOfBlockage: run.causeOfBlockage,
            _item: run,
        };
    }
    return run;
}

function tryToFixRunState(run, pipelineRuns) {
    if (run.state === 'QUEUED') {
        // look up the run locally first, as this is the only way
        // for us to get an appropriate value if the run was recently queued
        // this is a back-end issue where the status takes a while to update to 'RUNNING'
        const found = pipelineRuns && pipelineRuns.filter(r => r.id === run.id || (r.job_run_queueId && r.job_run_queueId === run.job_run_queueId))[0];
        if (found) {
            return Object.assign({}, run, {
                id: run.id || found.id,
                state: found.state,
                result: found.result,
                job_run_queueId: found.job_run_queueId,
            });
        }
        return Object.assign({}, run, {
            state: 'RUNNING',
            result: 'UNKNOWN',
        });
    }
    return run;
}

// main actin logic
export const ACTION_TYPES = keymirror({
    UPDATE_MESSAGES: null,
    CLEAR_PIPELINES_DATA: null,
    SET_ALL_PIPELINES_DATA: null,
    SET_ORG_PIPELINES_DATA: null,
    SET_PIPELINE: null,
    CLEAR_PIPELINE_DATA: null,
    SET_RUNS_DATA: null,
    SET_CURRENT_RUN_DATA: null,
    SET_CURRENT_RUN: null,
    SET_CURRENT_PULL_REQUEST_DATA: null,
    SET_CURRENT_BRANCHES_DATA: null,
    CLEAR_CURRENT_BRANCHES_DATA: null,
    CLEAR_CURRENT_PULL_REQUEST_DATA: null,
    SET_STEPS: null,
    SET_NODE: null,
    SET_NODES: null,
    SET_LOGS: null,
    REMOVE_LOG: null,
    FIND_AND_UPDATE: null,
    REMOVE_STEP: null,
});

export const actionHandlers = {
    [ACTION_TYPES.UPDATE_MESSAGES](state, { payload }): State {
        const messages = state.get('messages') || [];
        if (payload) {
            messages.push(payload);
        }
        return state.set('messages', messages);
    },
    [ACTION_TYPES.CLEAR_PIPELINES_DATA](state) {
        return state.set('allPipelines', null)
            .set('organizationPipelines', null);
    },
    [ACTION_TYPES.SET_ALL_PIPELINES_DATA](state, { payload }): State {
        return state.set('allPipelines', payload);
    },
    [ACTION_TYPES.SET_ORG_PIPELINES_DATA](state, { payload }): State {
        return state.set('organizationPipelines', payload);
    },
    [ACTION_TYPES.CLEAR_PIPELINE_DATA](state) {
        return state.set('pipeline', null);
    },
    [ACTION_TYPES.SET_PIPELINE](state, { payload }): State {
        return state.set('pipeline', payload);
    },
    [ACTION_TYPES.SET_CURRENT_RUN_DATA](state, { payload }): State {
        return state.set('currentRuns', payload.map((run) => _mapQueueToPsuedoRun(run)));
    },
    [ACTION_TYPES.SET_CURRENT_RUN](state, { payload }): State {
        return state.set('currentRun', payload);
    },
    [ACTION_TYPES.SET_NODE](state, { payload }): State {
        return state.set('node', { ...payload });
    },
    [ACTION_TYPES.SET_NODES](state, { payload }): State {
        const nodes = { ...state.nodes } || {};
        nodes[payload.nodesBaseUrl] = payload;
        return state.set('nodes', nodes);
    },
    [ACTION_TYPES.SET_RUNS_DATA](state, { payload, id }): State {
        const runs = { ...state.runs } || {};
        runs[id] = payload.map(run => _mapQueueToPsuedoRun(run));
        applyFetchMarkers(runs[id], payload);
        return state.set('runs', runs)
            .set('currentRuns', runs[id]);
    },
    [ACTION_TYPES.CLEAR_CURRENT_BRANCHES_DATA](state) {
        return state.delete('currentBranches');
    },
    [ACTION_TYPES.CLEAR_CURRENT_PULL_REQUEST_DATA](state) {
        return state.delete('pullRequests');
    },
    [ACTION_TYPES.SET_CURRENT_BRANCHES_DATA](state, { payload }): State {
        return state.set('currentBranches', payload);
    },
    [ACTION_TYPES.SET_CURRENT_PULL_REQUEST_DATA](state, { payload }): State {
        return state.set('pullRequests', payload);
    },
    [ACTION_TYPES.SET_STEPS](state, { payload }): State {
        const steps = { ...state.steps } || {};
        steps[payload.nodesBaseUrl] = payload;
        return state.set('steps', steps);
    },
    [ACTION_TYPES.REMOVE_STEP](state, { stepId }): State {
        const steps = { ...state.steps } || {};
        delete steps[stepId];
        return state.set('steps', steps);
    },
    [ACTION_TYPES.SET_LOGS](state, { payload }): State {
        const logs = { ...state.logs } || {};
        logs[payload.logUrl] = payload;

        return state.set('logs', logs);
    },
    [ACTION_TYPES.REMOVE_LOG](state, { key }): State {
        const logs = { ...state.logs } || {};
        Object.keys(logs)
            .filter((item) => item.indexOf(key) !== -1)
            .map((item) => delete logs[item]);
        return state.set('logs', logs);
    },
    [ACTION_TYPES.FIND_AND_UPDATE](state, { payload }): State {
        const updated = findAndUpdate(state, payload);
        if (updated) {
            return updated;
        }
        return state;
    },
};

function parseMoreDataHeader(response) {
    let newStart = null;
    /*
     * If X-More-Data is true, then client should repeat the request after some delay.
     * In the repeated request it should use X-TEXT-SIZE header value with start query parameter.
     */
    if (response.headers.get('X-More-Data')) {
        /*
         * X-TEXT-SIZE is the byte offset of the raw log file client should use in the next request
         * as value of start query parameter.
         */
        newStart = response.headers.get('X-TEXT-SIZE');
    }
    const payload = { response, newStart };
    return payload;
}

/**
 * Fetch TXT/log data and inject a start parameter to indicate that a refetch is needed
 * <p>
 * Utility function that can be mocked for testing.
 *
 * @param url The URL to fetch from.
 * @param start query parameter tells API to send log starting from this offset in the log file.
 * @param onSuccess Main callback to run specific callback code
 * @param onError Error callback
 */

exports.fetchLogsInjectStart = function fetchLogsInjectStart(url, start, onSuccess, onError) {
    let refetchUrl;
    if (start === null) {
        refetchUrl = url;
    } else {
        refetchUrl = `${url}?start=${start}`;
    }
    return Fetch.fetch(refetchUrl)
        .then(parseMoreDataHeader)
        .then(onSuccess)
        .catch(FetchFunctions.onError(onError));
};

/**
 * Locates instances in the state tree and replaces them, just provide a 'replacer' method,
 * which returns undefined/false for objects which should not be replaced.
 */
function dispatchFindAndUpdate(dispatch, fn) {
    dispatch({
        type: ACTION_TYPES.FIND_AND_UPDATE,
        payload: fn,
    });
}

/**
 * Clone a JSON object/array instance.
 * <p>
 * This needs to be done for redux. See
 * http://redux.js.org//docs/recipes/UsingObjectSpreadOperator.html.
 * <p>
 * TODO: Maybe use object/array spread operator.
 * I didn't try because I was not sure if they could be used for this as it seems
 * like they only perform a shallow clone.
 *
 * @param json The JSON object/array to be cloned.
 */
function clone(json) {
    return JSON.parse(JSON.stringify(json));
}

export const actions = {
    clearPipelinesData: () => ({ type: ACTION_TYPES.CLEAR_PIPELINES_DATA }),
    clearPipelineData() {
        return (dispatch) => dispatch({ type: ACTION_TYPES.CLEAR_PIPELINE_DATA });
    },

    /**
     * Returns cached global pipeline list or causes a fetch
     */
    getAllPipelines() {
        return (dispatch, getState) => {
            if (!getState().adminStore.allPipelines) {
                actions.fetchAllPipelines()(dispatch);
            }
        };
    },

    /**
     * Returns cached organization pipelines or causes a fetch
     * @param { organizationName } specific organization to fetch
     */
    getOrganizationPipelines({ organizationName }) {
        return (dispatch, getState) => {
            const orgPipelines = getState().adminStore.organizationPipelines;
            if (!orgPipelines || !orgPipelines.length || !orgPipelines[0].organizationName === organizationName) {
                // Doesn't match, clear existing set
                dispatch({
                    type: ACTION_TYPES.SET_ORG_PIPELINES_DATA,
                    payload: null,
                });
                actions.fetchOrganizationPipelines({ organizationName })(dispatch);
            }
        };
    },

    /**
     * Unconditionally fetch and update the pipelines list.
     * @param config Application configuration.
     * @param organizationName (optional)
     */
    // eslint-disable-next-line no-unused-vars
    fetchAllPipelines() {
        return (dispatch) => {
            // Note: this is including folders, which we can't deal with, so exclude them with the ?filter=no-folders
            const organization = AppConfig.getOrganizationName();
            const url =
                `${UrlConfig.getRestRoot()}/search/?q=type:pipeline;organization:${organization};excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
            return paginate({ urlProvider: paginateUrl(url) })
            .then(data => {
                dispatch({
                    type: ACTION_TYPES.SET_ALL_PIPELINES_DATA,
                    payload: data,
                });
            });
        };
    },

    clearBranchData() {
        return (dispatch) => dispatch({
            type: ACTION_TYPES.CLEAR_CURRENT_BRANCHES_DATA,
        });
    },

    clearPRData() {
        return (dispatch) => dispatch({
            type: ACTION_TYPES.CLEAR_CURRENT_PULL_REQUEST_DATA,
        });
    },

    fetchOrganizationPipelines({ organizationName }) {
        return (dispatch) => {
            // Note: this is including folders, which we can't deal with, so exclude them with the ?filter=no-folders
            const url =
                `${UrlConfig.getRestRoot()}/search/?q=type:pipeline;organization:${encodeURIComponent(organizationName)};excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
            return paginate({ urlProvider: paginateUrl(url) })
            .then(data => {
                dispatch({
                    type: ACTION_TYPES.SET_ORG_PIPELINES_DATA,
                    payload: data,
                });
            });
        };
    },

    fetchBranches({ organizationName, pipelineName }) {
        return (dispatch) => {
            const url =
                `${UrlConfig.getRestRoot()}/organizations/${encodeURIComponent(organizationName)}/pipelines/${pipelineName}/branches/?filter=origin`;
            return paginate({ urlProvider: paginateUrl(url) })
            .then(data => {
                dispatch({
                    id: pipelineName,
                    type: ACTION_TYPES.SET_CURRENT_BRANCHES_DATA,
                    payload: data,
                });
            });
        };
    },

    fetchPullRequests({ organizationName, pipelineName }) {
        return (dispatch) => {
            const url =
                `${UrlConfig.getRestRoot()}/organizations/${encodeURIComponent(organizationName)}/pipelines/${pipelineName}/branches/?filter=pull-requests`;
            return paginate({ urlProvider: paginateUrl(url) })
            .then(data => {
                dispatch({
                    type: ACTION_TYPES.SET_CURRENT_PULL_REQUEST_DATA,
                    payload: data,
                });
            });
        };
    },

    /**
     * Fetch a specific pipeline, sets as current
     */
    fetchPipeline(organizationName, pipelineName) {
        return (dispatch) =>
            smartFetch(
                getRestUrl({ organization: organizationName, pipeline: pipelineName }),
                data => dispatch({
                    id: pipelineName,
                    type: ACTION_TYPES.SET_PIPELINE,
                    payload: data,
                })
            );
    },

    processJobQueuedEvent(event) {
        return (dispatch, getState) => {
            const id = event.blueocean_job_pipeline_name;
            const runsByJobName = getState().adminStore && getState().adminStore.runs || {};
            const eventJobRuns = runsByJobName[id];

            // Only interested in the event if we have already loaded the runs for that job.
            if (eventJobRuns && event.job_run_queueId) {
                // here, we're emulating the expectedBuildNumber in order to get a runId
                // we need a runId so clicking to view the queued item shows the right page
                // now that we are fetching a run 'directly', however it's important to note
                // that this could still fail with a reload if the runs have not loaded and
                // we don't have a matching queued item with the same runId
                let nextId = 0;
                for (let i = 0; i < eventJobRuns.length; i++) {
                    const run = eventJobRuns[i];
                    if (event.job_ismultibranch
                        && event.blueocean_job_branch_name !== run.pipeline) {
                        // Not the same branch. Yes, run.pipeline actually contains
                        // the branch name i.e. naming seems a bit confusing.
                        continue;
                    }
                    if (run.job_run_queueId === event.job_run_queueId) {
                        // We already have a "dummy" record for this queued job
                        // run. No need to create another i.e. ignore this event.
                        return;
                    }
                    if (parseInt(run.id, 10) > nextId) { // figure out the next id, expectedBuildNumber
                        nextId = parseInt(run.id, 10);
                    }
                }

                // Create a new "dummy" entry in the runs list for the
                // run that's been queued.
                const newRun = { id: `${nextId + 1}` };

                // We keep the queueId so we can cross reference it with the actual
                // run once it has been started.
                newRun.job_run_queueId = event.job_run_queueId;
                let queueUrl = event.blueocean_job_rest_url;
                if (event.job_ismultibranch) {
                    newRun.pipeline = event.blueocean_job_branch_name;
                    queueUrl += `/branches/${event.blueocean_job_branch_name}/queue/${event.job_run_queueId}`;
                } else {
                    newRun.pipeline = event.blueocean_job_pipeline_name;
                    queueUrl += `/queue/${event.job_run_queueId}`;
                }

                // attach the queue href via the _item prop; see _mapQueueToPsuedoRun
                newRun._item = {
                    _links: {
                        self: {
                            href: queueUrl,
                        },
                    },
                };

                newRun.state = 'QUEUED';
                newRun.result = 'UNKNOWN';

                const newRuns = clone([newRun, ...eventJobRuns]);
                applyFetchMarkers(newRuns, eventJobRuns);

                if (event.blueocean_is_for_current_job) {
                    // set current runs since we are ATM looking at it
                    dispatch({ payload: newRuns, type: ACTION_TYPES.SET_CURRENT_RUN_DATA });
                }
                dispatch({
                    payload: newRuns,
                    id,
                    type: ACTION_TYPES.SET_RUNS_DATA,
                });
            }
        };
    },

    processJobLeftQueueEvent(event) {
        return (dispatch, getState) => {
            // only proceed with removal if the job was cancelled
            if (event.job_run_status === 'CANCELLED') {
                const id = event.blueocean_job_pipeline_name;
                const runsByJobName = getState().adminStore && getState().adminStore.runs || {};
                const eventJobRuns = runsByJobName[id];

                // Only interested in the event if we have already loaded the runs for that job.
                if (eventJobRuns && event.job_run_queueId) {
                    const newRuns = clone(eventJobRuns);
                    applyFetchMarkers(newRuns, eventJobRuns);

                    const queueItemToRemove = newRuns.find((run) => (
                        run.job_run_queueId === event.job_run_queueId
                    ));

                    newRuns.splice(newRuns.indexOf(queueItemToRemove), 1);

                    if (event.blueocean_is_for_current_job) {
                        // set current runs since we are ATM looking at it
                        dispatch({ payload: newRuns, type: ACTION_TYPES.SET_CURRENT_RUN_DATA });
                    }
                    dispatch({
                        payload: newRuns,
                        id,
                        type: ACTION_TYPES.SET_RUNS_DATA,
                    });
                }
            }
        };
    },

    updateRunState(event) {
        function matchesEvent(evt, o) {
            return o.job_run_queueId === evt.job_run_queueId
                || (o._links && o._links.self && o._links.self.href.indexOf(evt.blueocean_job_rest_url) === 0 && o.id === evt.jenkins_object_id);
        }
        return (dispatch, getState) => {
            debugLog('updateRunState:', event);
            let found = false;
            findAndUpdate(getState().adminStore, o => {
                if (!found && matchesEvent(event, o)) {
                    debugLog('found:', o);
                    found = true;
                }
            });
            if (found) {
                debugLog('Calling dispatch for event ', event);
                const runUrl = `${UrlConfig.getJenkinsRootURL()}${event.blueocean_job_rest_url}runs/${event.jenkins_object_id}`;
                smartFetch(runUrl)
                .then(data => {
                    if (data.$pending) return;
                    debugLog('Updating run: ', data);
                    dispatchFindAndUpdate(dispatch, run => {
                        if (matchesEvent(event, run)) {
                            if (event.jenkins_event !== 'job_run_ended') {
                                return { ...data,
                                    id: event.jenkins_object_id, // make sure the runId is set so we can find it later
                                    // here, we explicitly set to running. this is because
                                    // pipeline runs get removed from the queue, a running event is sent
                                    // but they are still queued in Jenkins, as they may not actually
                                    // be executing anything. However, pipeline doesn't send any further
                                    // events so we're never notified when it actually does start, so
                                    // we just treat this subsequent runStateEvent as a start or a finish
                                    state: 'RUNNING',
                                    result: 'UNKNOWN',
                                };
                            }
                            return data;
                        }
                        return undefined;
                    });
                })
                .catch(err => {
                    // Just update the run state for fetch failures (this was the existing behavior, not sure why, really)
                    dispatchFindAndUpdate(dispatch, o => {
                        if (o.job_run_queueId === event.job_run_queueId) {
                            return { ...o, state: event.jenkins_event === 'job_run_ended' ? 'FINISHED' : 'RUNNING' };
                        }
                        return undefined;
                    });
                    debugLog('Fetch error: ', err);
                });
            }
        };
    },

    updateBranchState(event) {
        return (dispatch, getState) => {
            debugLog('updateBranchState:', event);
            let found = false;
            findAndUpdate(getState().adminStore, o => {
                debugLog('updateBranchState:', o);
                if (!found && o && o.latestRun && (o.fullName === event.job_name)) {
                    debugLog('found:', o);
                    found = true;
                }
            });
            if (found) {
                const url = `${UrlConfig.getJenkinsRootURL()}${event.blueocean_job_rest_url}`;
                smartFetch(url, (branchData) => {
                    if (branchData.$pending) { return; }
                    if (branchData.$failure) {
                        debugLog(branchData.$failure);
                        return;
                    }
                    // apply the new data to the store
                    dispatchFindAndUpdate(dispatch, branch => {
                        if (branch && branch.latestRun && (branch.fullName === event.job_name)) {
                            if (branchData.latestRun.state !== 'RUNNING') {
                                return { ...branchData,
                                    latestRun: { ...branchData.latestRun,
                                        // Jenkins doesn't update this value
                                        // at least not fast enough...
                                        state: event.jenkins_event === 'job_run_ended' ? 'FINISHED' : 'RUNNING',
                                    },
                                };
                            }
                            return branchData;
                        }
                        return undefined;
                    });
                });
            }
        };
    },

    updateBranchList(event) {
        return (dispatch, getState) => {
            // this should probably be responsible for determining when to update, rather than
            // the event having any knowledge of which job is visible...
            // e.g.: if (event.blueocean_job_pipeline_name === getState().adminStore.pipeline.name)
            if (event.blueocean_is_for_current_job) {
                this.fetchBranches({
                    organizationName: event.jenkins_org,
                    pipelineName: event.blueocean_job_pipeline_name,
                })(dispatch, getState);
            }
        };
    },

    fetchRuns({ organization, pipeline }) {
        return (dispatch, getState) => paginate({
            urlProvider: paginateUrl(
                `${UrlConfig.getRestRoot()}/organizations/${organization}/pipelines/${pipeline}/activities/`),
            onData: data => {
                const runs = getState().adminStore && getState().adminStore.runs ? getState().adminStore.runs[pipeline] : [];
                dispatch({
                    id: pipeline,
                    payload: data.map(run => tryToFixRunState(run, runs)),
                    type: ACTION_TYPES.SET_RUNS_DATA,
                });
            },
        });
    },

    fetchRun(config) {
        return (dispatch, getState) => {
            const runs = getState().adminStore && getState().adminStore.runs ? getState().adminStore.runs[config.pipeline] : [];
            smartFetch(
                getRestUrl(config),
                data => {
                    if (data.$failed && runs) { // might be a queued item...
                        const found = runs.filter(r => r.id === config.runId)[0];
                        if (found) {
                            try {
                                found.$success = true;
                            } catch (e) {
                                // Ignore, might be a real item
                                console.log('amoc', e);
                            }
                            dispatch({
                                id: config.pipeline,
                                type: ACTION_TYPES.SET_CURRENT_RUN,
                                payload: found,
                            });
                            return; // skip the next dispatch
                        }
                    }
                    dispatch({
                        id: config.pipeline,
                        type: ACTION_TYPES.SET_CURRENT_RUN,
                        payload: tryToFixRunState(data, runs),
                    });
                }
            )
            .catch(err => {
                debugLog('Fetch error: ', err);
            });
        };
    },

    /*
     For the detail view we need to fetch the different nodes of
     a run in case we do not have specific node, to
     determine which one we have to show in the detail view.
     We later store them with the key: nodesBaseUrl
     so we only fetch them once.
     */
    fetchNodes(config) {
        return (dispatch, getState) => {
            const data = getState().adminStore.nodes;
            const nodesBaseUrl = calculateNodeBaseUrl(config);

            function getNodeAndSteps(information) {
                let nodeModel;
                let node;
                if (!config.node) {
                    const focused = information.model.filter((item) => item.isFocused)[0];
                    if (focused) {
                        nodeModel = focused;
                    } else {
                        if (config.isPipelineQueued) {
                            nodeModel = (information.model[0]);
                        } else {
                            nodeModel = (information.model[information.model.length - 1]);
                        }
                    }
                    node = nodeModel ? nodeModel.id : null;
                } else {
                    nodeModel = information.model.filter((item) => item.id === config.node)[0];
                    node = config.node;
                }
                // console.log('ACTION_TYPES.SET_NODE', nodeModel);

                dispatch({
                    type: ACTION_TYPES.SET_NODE,
                    payload: nodeModel,
                });
                const mergedConfig = { ...config, node };
                return dispatch(actions.fetchSteps(mergedConfig));
            }

            if (!data || !data[nodesBaseUrl] || config.refetch) {
                return Fetch.fetchJSON(nodesBaseUrl)
                    .then((json) => {
                        const information = getNodesInformation(json);
                        information.nodesBaseUrl = nodesBaseUrl;
                        // console.log('nodes fetch log', information, json);
                        dispatch({
                            type: ACTION_TYPES.SET_NODES,
                            payload: information,
                        });

                        return getNodeAndSteps(information);
                    }).catch(FetchFunctions.consoleError);
            }

            return getNodeAndSteps(data[nodesBaseUrl]);
        };
    },

    setNode(config) {
        return (dispatch, getState) => {
            const data = getState().adminStore.nodes;
            const nodesBaseUrl = calculateNodeBaseUrl(config);
            if (!data || !data[nodesBaseUrl] || config.refetch) {
                return actions.fetchNodes(config);
            }
            const node = data[nodesBaseUrl].model.filter((item) => item.id === config.node)[0];
            return dispatch({
                type: ACTION_TYPES.SET_NODE,
                payload: node,
            });
        };
    },

    cleanNodePointer() {
        return (dispatch) => dispatch({
            type: ACTION_TYPES.SET_NODE,
            payload: null,
        });
    },
    /*
     For the detail view we need to fetch the different steps of a nodes.
     We later store them with the key: nodesBaseUrl
     so we only fetch them once.
     */
    fetchSteps(config) {
        return (dispatch, getState) => {
            const data = getState().adminStore.steps;
            const stepBaseUrl = calculateStepsBaseUrl(config);
            if (!data || !data[stepBaseUrl] || config.refetch) {
                return Fetch.fetchJSON(stepBaseUrl)
                    .then((json) => {
                        const information = getNodesInformation(json);
                        information.nodesBaseUrl = stepBaseUrl;
                        // console.log('action fetch log', information, json);

                        return dispatch({
                            type: ACTION_TYPES.SET_STEPS,
                            payload: information,
                        });
                    }).catch(FetchFunctions.consoleError);
            }
            return null;
        };
    },
    /**
     * Remove steps from cache
     *
     * @param id {String} the step we want to remove
     * @returns {function(*)}
     */
    removeStep(id) {
        return (dispatch) => dispatch({
            type: ACTION_TYPES.REMOVE_STEP,
            stepId: id,
        });
    },
    /**
     * Remove logs from cache
     *
     * @param id {String} the log we want to remove - we doing indexOf to remove all logs startingWith
     * @returns {function(*)}
     */
    removeLogs(id) {
        return (dispatch) => dispatch({
            type: ACTION_TYPES.REMOVE_LOG,
            key: id,
        });
    },
    /*
     Get a specific log for a node, fetch it only if needed.
     key for cache: logUrl = calculateLogUrl
     */
    fetchLog(cfg) {
        return (dispatch, getState) => {
            const data = getState().adminStore.logs;
            let config = cfg;
            if (!config.nodesBaseUrl) {
                config = { ...config, nodeBaseUrl: calculateNodeBaseUrl(config) };
            }
            const logUrl = calculateLogUrl(config);
            if (
                config.fetchAll ||
                !data || !data[logUrl] ||
                config.newStart > 0 ||
                (data && data[logUrl] && data[logUrl].newStart > 0 || !data[logUrl].logArray)
            ) {
                return exports.fetchLogsInjectStart(
                    logUrl,
                    config.newStart || null,
                    response => response.response.text()
                        .then(text => {
                            // By default only last 150 KB log data is returned in the response.
                            const maxLength = 150000;
                            const contentLength = Number(response.response.headers.get('X-Text-Size'));
                            // set flag that there are more logs then we deliver
                            let hasMore = contentLength > maxLength;
                            // when we came from ?start=0, hasMore has to be false since there is no more
                            // debugLog(config.fetchAll, 'inner')
                            if (config.fetchAll) {
                                hasMore = false;
                            }
                            const { newStart } = response;
                            const payload = {
                                logUrl,
                                newStart,
                                hasMore,
                            };
                            if (text && !!text.trim()) {
                                payload.logArray = text.trim().split('\n');
                            }
                            return dispatch({
                                payload,
                                type: ACTION_TYPES.SET_LOGS,
                            });
                        }),
                    (error) => console.error('error', error) // eslint-disable-line no-console
                );
            }
            return null;
        };
    },
};

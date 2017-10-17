console.log('pipeline');

import React, { Component, PropTypes } from 'react';
import { calculateLogView } from '@jenkins-cd/blueocean-core-js';
import { Logger } from '../../../util/Logger';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import { NoSteps, QueuedState } from './QueuedState';
import LogToolbar from './LogToolbar';
import Steps from './Steps';
import FreeStyle from './FreeStyle';
import RunDescription from './RunDescription';
import { LiveUpdate } from '../services/LiveUpdate';
import { NodeStore } from '../services/NodeStore';
import { StepStore } from '../services/StepStore';

import { KaraokeConfig } from '../';
const log = new Logger('pipeline.run.render');

@observer
export default class Pipeline extends Component {
    constructor(props) {
        super(props);
        // query parameter before preference
        this.classicLog = calculateLogView(props) || KaraokeConfig.getPreference('runDetails.logView').value === 'classic';
        this.stopOnClick = KaraokeConfig.getPreference('runDetails.pipeline.stopKaraokeOnAnyNodeClick').value === 'always';
    }
    componentWillMount() {
        this.componentWillReceiveProps(this.props);
    }

    /**
     * Core logic to update and re-fetch data
     * @param nextProps
     */
    componentWillReceiveProps(nextProps) {
        log.debug('componentWillReceiveProps', nextProps);
        const { pipelineView, run, params: { node } } = nextProps;
        if (!pipelineView) {
            return;
        }
        if (!this.liveUpdate || this.liveUpdate.run.id !== run.id) {
            this.stopKaraoke();
            this.nodeStore = new NodeStore(pipelineView.nodesUrl);
            this.stepStore = new StepStore(pipelineView.stepsUrl);
            if (this.liveUpdate) {
                this.liveUpdate.cleanup();
            }
            this.liveUpdate = new LiveUpdate(nextProps.run, pipelineView, this.nodeStore, this.stepStore);
            this.liveUpdate.addUpdater(() => this.nodeStore.fetch());
            this.liveUpdate.addUpdater(() => new Promise(resolve => {
                const selectedNode = this.nodeStore.findNode(node);
                if (selectedNode) {
                    pipelineView.setCurrentNode(selectedNode);
                } else {
                    pipelineView.setCurrentNode(this.nodeStore.findAutoFocusNode());
                }
                this.forceUpdate();
                resolve();
            }));
            this.liveUpdate.addUpdater(() => new Promise(resolve => {
                this.stepStore.url = pipelineView.stepsUrl;
                this.stepStore.fetch();
                this.forceUpdate();
                resolve();
            }));
            this.liveUpdate.addUpdater(() => new Promise(resolve => {
                if (!nextProps.pipelineView.currentNode.isCompleted) {
                    nextProps.pipelineView.setLogActive(false);
                    nextProps.pipelineView.setFollowing(true);
                }
                this.forceUpdate();
                resolve();
            }));
        }
        // starting pipeline service when we have an pipelineView
        if (this.liveUpdate) {
            this.liveUpdate._processUpdates();
        }
    }
    /**
     * Need to remove the listener to prevent memory leaks
     */
    componentWillUnmount() {
        this.stopKaraoke();
    }

    // here we decide what to do next if somebody clicks on a flowNode
    // Underlying tasks are fetching nodes information for the selected node
    onNodeClick(id) {
        const { router, location, pipelineView } = this.props;
        log.debug('clicked on node with id:', id);
        const nextNode = this.nodeStore.nodes.data.model.filter((item) => item.id === id)[0];
        // remove trailing /
        const pathname = location.pathname.replace(/\/$/, '');
        let nextPath;
        if (pathname.endsWith('pipeline')) {
            nextPath = pathname;
        } else { // means we are in a node url
            // remove last bits
            const pathArray = pathname.split('/');
            pathArray.pop();
            pathArray.shift();
            nextPath = `/${pathArray.join('/')}`;
        }
        // check whether we have a parallel node
        const isParallel = nextNode.isParallel;
        // see whether we need to update the state
        if (nextNode.state === 'FINISHED' || isParallel) {
            nextPath = `${nextPath}/${id}`; // only allow node param in finished nodes
        }
        if (pipelineView) {
            if (nextNode.state === 'FINISHED') {
                log.debug('turning off following since we clicked on a completed node');
                // make sure sse events are coming in, but don't turn on following yet
                this.liveUpdate.setActive(false);
            }
        }
        location.pathname = nextPath;
        log.debug('redirecting now to:', location.pathname);
        router.push(location);
    }

    stopKaraoke() {
        const { pipelineView } = this.props;
        log.debug('stopping following along');
        if (pipelineView) {
            pipelineView.setFollowing(false);
        }
        if (this.liveUpdate) {
            this.liveUpdate.cleanup();
        }
    }

    /**
     * Listen for pipeline flow node events. We need to re-fetch in case of some events.
     * @param event sse event coming from the backende
     */
    sseEventHandler(event) {
         // we are using try/catch to throw an early out error
        try {
            log.debug('incoming event', event);
            const followingOut = KaraokeConfig.getPreference('runDetails.pipeline.following').value === 'never' || !this.following;
            const jenkinsEvent = event.jenkins_event;
            const { run } = this.props;
            const runId = run.id;
             // we get events from the pipeline and the job channel, they have different naming for the id
            //  && event.jenkins_object_id !== runId -> job
            if (event.pipeline_run_id !== runId) {
                log.debug('early out');
                throw new Error('exit');
            }
            switch (jenkinsEvent) {
            case 'pipeline_step': {
                if (followingOut) {
                    log.debug('early out because we do not want to follow along sse events');
                    throw new Error('exit');
                }
                log.debug('sse event step fetchCurrentSteps', jenkinsEvent);

                log.debug('sse fetch it', this.following);
                this.pager.fetchCurrentStepUrl();

                // prevent flashing of stages and nodes
                this.showPending = false;
                break;
            }
            case 'pipeline_end':
            case 'pipeline_start':
            case 'job_run_ended':
            case 'pipeline_block_end':
            case 'pipeline_stage': {
                log.debug('sse event block starts refetchNodes', jenkinsEvent);

                log.debug('sse fetch it', this.following);
                if (followingOut) {
                    this.pager.fetchNodesOnly({});
                } else {
                    this.pager.fetchNodes({});
                }

                // prevent flashing of stages and nodes
                this.showPending = false;
                break;
            }
            default: {
                log.debug('ignoring event', jenkinsEvent);
            }
            }
        } catch (e) {
            // we only ignore the exit error
            if (e.message !== 'exit') {
                log.error('sse Event has produced an error, will not work as expected.', e);
            }
        }
    }

    stopKaraoke() {
        const { pipelineView } = this.props;
        log.debug('stopping following along');
        if (pipelineView) {
            pipelineView.setFollowing(false);
        }
        if (this.liveUpdate) {
            this.liveUpdate.cleanup();
        }
    }

    render() {
        log.trace('re-rendering Pipeline.jsx');

        const { t, run, pipelineView, branch, pipeline, router, location } = this.props;
        const steps = this.stepStore && this.stepStore.steps;
        const stepsPending = this.stepStore && this.stepStore.pending;
        // do we have something to display?
        const noSteps = steps === undefined || !steps || !steps.data || !steps.data.hasResultsForSteps;
        // Queue magic since a pipeline is only showing queued state a short time even if still waiting for executors
        const isPipelineQueued = run.isQueued() || (run.isRunning() && noSteps);
        log.debug('isQueued', run.isQueued(), 'noSteps', noSteps, 'isPipelineQueued', isPipelineQueued);
        if (isPipelineQueued) { // if queued we are saying that we are waiting to start
            log.trace('run is queued');
            return (<QueuedState
                translation={t}
                titleKey="rundetail.pipeline.waiting.message.title"
                messageKey="rundetail.pipeline.waiting.message.description"
                message={run.causeOfBlockage}
            />);
        }
        const supportsNodes = this.nodeStore.nodes === undefined;
        if (!this.nodeStore.pending && (this.classicLog || (noSteps && supportsNodes))) { // no information? fallback to freeStyle
            log.trace('falling back to freeStyle rendering');
            return (<FreeStyle {...this.props } />);
        }
        if (this.nodeStore.pending && this.nodeStore.nodes.length === 0) { // we are waiting for the backend information
            log.trace('no nodes fetched, pending');
            return (<QueuedState
                translation={t}
                titleKey="rundetail.pipeline.pending.message.title"
                messageKey="rundetail.pipeline.pending.message.description"
            />);
        }
        const title = pipelineView.currentNode && t('rundetail.pipeline.steps', {
            defaultValue: 'Steps {0}',
            0: pipelineView.currentNode.displayName,
        }) || '';
        // JENKINS-40526 node can provide logs only related to that node
        const logUrl = pipelineView.currentNode !== undefined ? pipelineView.getNodesLogUrl(pipelineView.currentNode) : pipelineView.generalLogUrl;
        const logFileName = pipelineView.currentNode !== undefined ? pipelineView.getNodesLogFileName(pipelineView.currentNode) : pipelineView.generalLogFileName;
        log.debug('displayName', pipelineView.currentNode && pipelineView.currentNode.displayName, 'logging info', logUrl, logFileName);
        return (<div key={run.id}>
            { <RunDescription run={this.props.run} t={t} /> }
            { pipelineView.currentNode &&
                <Extensions.Renderer
                    extensionPoint="jenkins.pipeline.run.result"
                    selectedStage={pipelineView.currentNode}
                    callback={id => this.onNodeClick(id)}
                    nodes={this.nodeStore.nodes.data.model}
                    pipelineName={pipeline.displayName}
                    branchName={pipelineView.isMultiBranchPipeline ? branch : undefined}
                    runId={run.id}
                    run={run}
                    t={t}
                />
            }
            { !isPipelineQueued &&
                <LogToolbar
                    fileName={logFileName}
                    url={logUrl}
                    title={title}
                />
            }
            { steps && !noSteps &&
                <Steps
                    {...{
                        key: pipelineView.stepsUrl,
                        nodeInformation: steps.data,
                        followAlong: pipelineView.following,
                        pipelineView,
                        t,
                        router,
                        location,
                    }}
                />
            }

            { !stepsPending && !isPipelineQueued && noSteps &&
                <NoSteps
                    translation={t}
                    titleKey="rundetail.pipeline.nosteps.message.title"
                    messageKey="rundetail.pipeline.nosteps.message.description"
                />
            }

            { isPipelineQueued &&
                <QueuedState translation={t}
                    titleKey="rundetail.pipeline.waiting.message.title"
                    messageKey="rundetail.pipeline.waiting.message.description"
                    message={run.causeOfBlockage}
                />
            }
        </div>);
    }
}

Pipeline.propTypes = {
    pipelineView: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    run: PropTypes.object,
    t: PropTypes.func,
    router: PropTypes.shape,
    location: PropTypes.shape,
    scrollToBottom: PropTypes.bol,
    params: PropTypes.object,
};

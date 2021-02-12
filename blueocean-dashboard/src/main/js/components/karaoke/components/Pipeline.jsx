import React, { Component, PropTypes } from 'react';
import { logging, sseConnection } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import { NoSteps, QueuedState } from './QueuedState';
import { KaraokeService } from '../index';
import LogConsole from './LogConsole';
import LogToolbar from './LogToolbar';
import Steps from './Steps';
import FreeStyle from './FreeStyle';
import RunDescription from './RunDescription';
import { UrlBuilder } from '@jenkins-cd/blueocean-core-js';
import StageRestartLink from '../../StageRestartLink';

import { KaraokeConfig } from '../';
import { DownstreamRuns } from '../../downstream-runs/DownstreamRuns';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pipeline');

// using the hook 'location.search'.includes('view=0') to trigger the logConsole view instead of steps
function isClassicLogView(props) {
    const { search } = props.location;

    if (search) {
        const viewReg = /view=(\d+)/;
        const match = viewReg.exec(search);
        if (match && match[1] && Number(match[1]) === 0) {
            return true;
        }
    }
    return false;
}

// NB: This is loaded into RunDetailsPipeline.jsx as a handler for the jenkins.pipeline.karaoke.pipeline.provider extension point
@observer
export default class Pipeline extends Component {
    constructor(props) {
        super(props);
        this.listener = {};
        this.sseEventHandler = this.sseEventHandler.bind(this);
        // query parameter before preference
        this.classicLog = isClassicLogView(props) || KaraokeConfig.getPreference('runDetails.logView').value === 'classic';
        this.showPending = KaraokeConfig.getPreference('runDetails.pipeline.showPending').value !== 'never'; // Configure flag to show pending or not
        this.karaoke = KaraokeConfig.getPreference('runDetails.pipeline.karaoke').value === 'never' ? false : props.augmenter.karaoke; // initial karaoke state
        this.updateOnFinish = KaraokeConfig.getPreference('runDetails.pipeline.updateOnFinish').value;
        this.stopOnClick = KaraokeConfig.getPreference('runDetails.pipeline.stopKaraokeOnAnyNodeClick').value === 'always';
        this.state = { tailLogs: false };

        this.debounceFetchNodes = debounce((karaokeOut) => {
            logger.debug('sse fetch it', this.karaoke);
            if (karaokeOut) {
                this.pager.fetchNodesOnly({});
            } else {
                this.pager.fetchNodes({});
            }
        }, 200);
    }

    // These are normally on the context, but have to be sent into this component as props because context
    // doesn't cross the extension point barrier. So we put them back into the new context here.
    getChildContext() {
        const { params, location, activityService } = this.props;
        return { params, location, activityService };
    }

    componentWillMount() {
        // starting pipeline service when we have an augmenter
        if (this.props.augmenter) {
            const {
                augmenter,
                params: { node },
            } = this.props;
            this.pager = KaraokeService.pipelinePager(augmenter, { node });
        }
    }

    componentDidMount() {
        // get sse listener to react on the different in sse events
        this.listener.ssePipeline = sseConnection.subscribe('pipeline', this.sseEventHandler);
        this.listener.sseJob = sseConnection.subscribe('job', this.sseEventHandler);
    }

    /**
     * Core logic to update and re-fetch data
     * @param nextProps
     */
    componentWillReceiveProps(nextProps) {
        if (
            this.props.pipeline !== nextProps.pipeline ||
            this.props.run !== nextProps.run ||
            this.props.branch !== nextProps.branch ||
            this.props.params !== nextProps.params ||
            this.props.params.node !== nextProps.params.node ||
            this.props.augmenter !== nextProps.augmenter
        ) {
            // Any time these change, we'll just throw out the cached local state object rather than try to invalidate parts of it in-place
            const { node } = this.props.params;
            this.pager = KaraokeService.pipelinePager(nextProps.augmenter, { node });
        }

        // karaoke has changed state?
        if (!nextProps.augmenter.karaoke) {
            logger.debug('stopping karaoke mode.');
            this.stopKaraoke();
        }
        logger.debug(
            'karaoke mode.',
            'nextProps.augmenter.karaoke ',
            nextProps.augmenter.karaoke,
            'this.props.augmenter.karaoke',
            this.props.augmenter.karaoke,
            'this.karaoke',
            this.karaoke
        );
        // update on finish and start, you can de-activate it by setting updateOnFinish to false
        //
        if (nextProps.run.id !== this.props.run.id || (this.updateOnFinish !== 'never' && nextProps.run.isCompleted() && !this.props.run.isCompleted())) {
            logger.debug('re-fetching since result changed and we want to display the full log and correct result states');
            // remove all timeouts in the backend
            this.stopKaraoke();
            if (nextProps.run !== this.props.run) {
                logger.debug('Need to set new Run. Happens when e.g. re-run.');
                nextProps.augmenter.setRun(nextProps.run);
            }
            debounce(() => {
                if (KaraokeConfig.getPreference('runDetails.pipeline.karaoke').value !== 'never') {
                    logger.debug('re-setting karaoke mode.');
                    this.karaoke = true;
                }
                this.pager.fetchNodes({ node: nextProps.params.node });
            }, 200)();
        }
        // switches from the url which node to focus
        if (nextProps.params.node !== this.props.params.node) {
            logger.debug('Need to fetch new nodes.');
            this.pager.fetchNodes({ node: nextProps.params.node });
        }
    }

    /**
     * Need to remove the listener to prevent memory leaks
     */
    componentWillUnmount() {
        this.stopKaraoke();
        if (this.listener.ssePipeline) {
            sseConnection.unsubscribe(this.listener.ssePipeline);
            delete this.listener.ssePipeline;
        }
        if (this.listener.sseJob) {
            sseConnection.unsubscribe(this.listener.sseJob);
            delete this.listener.sseJob;
        }
    }

    stopKaraoke() {
        logger.debug('stopping karaoke mode, by removing the timeouts on the pager.');
        this.pager.clear();
        this.karaoke = false;
    }

    /**
     * User has explicitly opened a step log
     */
    userExpandedStep = step => {
        if (step.isFocused !== undefined && step.isFocused) {
            this.setState({ tailLogs: true });
        }
    };

    /**
     * User has explicitly opened a step log
     */
    userCollapsedStep = step => {
        if (step.isFocused !== undefined && step.isFocused) {
            this.setState({ tailLogs: false });
        }
    };

    /**
     * Listen for pipeline flow node events. We need to re-fetch in case of some events.
     * @param event sse event coming from the backende
     */
    sseEventHandler(event) {
        // we are using try/catch to throw an early out error
        try {
            logger.debug('incoming event', event);
            const karaokeOut = KaraokeConfig.getPreference('runDetails.pipeline.karaoke').value === 'never' || !this.karaoke;
            const jenkinsEvent = event.jenkins_event;
            const { run } = this.props;
            const runId = run.id;
            // we get events from the pipeline and the job channel, they have different naming for the id
            //  && event.jenkins_object_id !== runId -> job
            if (event.pipeline_run_id !== runId) {
                logger.debug('early out');
                throw new Error('exit');
            }
            switch (jenkinsEvent) {
                case 'pipeline_step': {
                    if (karaokeOut) {
                        logger.debug('early out because we do not want to follow along sse events');
                        throw new Error('exit');
                    }
                    logger.debug('sse event step fetchCurrentSteps', jenkinsEvent);
                    debounce(() => {
                        logger.debug('sse fetch it', this.karaoke);
                        this.pager.fetchCurrentStepUrl();
                    }, 200)();
                    // prevent flashing of stages and nodes
                    this.showPending = false;
                    break;
                }
                case 'pipeline_end':
                case 'pipeline_start':
                case 'job_run_ended':
                case 'pipeline_block_end':
                case 'pipeline_stage': {
                    logger.debug('sse event block starts refetchNodes', jenkinsEvent);
                    this.debounceFetchNodes(karaokeOut);
                    // prevent flashing of stages and nodes
                    this.showPending = false;
                    break;
                }
                default: {
                    logger.debug('ignoring event', jenkinsEvent);
                }
            }
        } catch (e) {
            // we only ignore the exit error
            if (e.message !== 'exit') {
                logger.error('sse Event has produced an error, will not work as expected.', e);
            }
        }
    }

    render() {
        const { t, locale, run, augmenter, branch, pipeline, router, scrollToBottom, location } = this.props;
        // do we have something to display?
        const noResultsToDisplay = this.pager.steps === undefined || (this.pager.steps && !this.pager.steps.data.hasResultsForSteps);
        // Queue magic since a pipeline is only showing queued state a short time even if still waiting for executors
        const isPipelineQueued = (run.isQueued() || run.isRunning()) && noResultsToDisplay;
        logger.debug('isQueued', run.isQueued(), 'noResultsToDisplay', noResultsToDisplay, 'isPipelineQueued', isPipelineQueued);
        const supportsNodes = this.pager.nodes === undefined;
        if (!this.pager.pending && (this.classicLog || (noResultsToDisplay && supportsNodes))) {
            // no information? fallback to freeStyle
            logger.debug('EarlyOut - We do not have any information we can display or we opt-out by preference, falling back to freeStyle rendering');
            return <FreeStyle {...this.props} />;
        }
        if (this.pager.pending && this.showPending) {
            // we are waiting for the backend information
            logger.debug('EarlyOut - abort due to pager pending');
            return (
                <QueuedState translation={t} titleKey="rundetail.pipeline.pending.message.title" messageKey="rundetail.pipeline.pending.message.description" />
            );
        }
        // here we decide what to do next if somebody clicks on a flowNode
        // Underlying tasks are fetching nodes information for the selected node
        const afterClick = id => {
            logger.debug('clicked on node with id:', id);
            this.showPending = false; // Configure flag to not show pending anymore -> reduce flicker
            const nextNode = this.pager.nodes.data.model.filter(item => item.id === id)[0];
            // remove trailing /
            const pathname = location.pathname.replace(/\/$/, '');
            let nextPath;
            if (pathname.endsWith('pipeline')) {
                nextPath = pathname;
            } else {
                // means we are in a node url
                // remove last bits
                const pathArray = pathname.split('/');
                pathArray.pop();
                pathArray.shift();
                nextPath = `/${pathArray.join('/')}`;
            }

            nextPath = `${nextPath}/${id}`;

            // see whether we need to update the karaoke mode
            if (nextNode.state === 'FINISHED' && this.karaoke) {
                logger.debug('turning off karaoke since we do not need it anymore because focus is on a finished node.');
                this.stopKaraoke();
            }
            if (!this.stopOnClick && nextNode.state !== 'FINISHED' && !this.karaoke) {
                logger.debug('turning on karaoke since we need it because we are focusing on a new node.');
                this.karaoke = true;
            }
            location.pathname = nextPath;
            logger.debug('redirecting now to:', location.pathname);
            router.push(location);
        };

        const switchRunDetails = newUrl => {
            location.pathname = newUrl;
            router.push(location);
        };

        let stepName = this.pager.nodes !== undefined ? this.pager.nodes.data.model.filter(item => item.id === this.pager.currentNode.parent)[0] : '';
        stepName = stepName && this.pager.currentNode.isParallel ? stepName.displayName : '';

        let title = this.pager.nodes !== undefined ? this.pager.currentNode.displayName : '';

        title = stepName ? `${stepName} / ${title}` : title;

        // JENKINS-40526 node can provide logs only related to that node
        const logUrl = this.pager.nodes !== undefined ? augmenter.getNodesLogUrl(this.pager.currentNode) : augmenter.generalLogUrl;
        const logFileName = this.pager.nodes !== undefined ? augmenter.getNodesLogFileName(this.pager.currentNode) : augmenter.generalLogFileName;
        logger.debug('displayName', this.pager.currentNode.displayName, 'logging info', logUrl, logFileName);

        let downstreamRuns = [];

        if (this.pager.currentNode && this.pager.currentNode.actions && this.pager.currentNode.actions.length) {
            downstreamRuns = this.pager.currentNode.actions
                .filter(action => action._class === 'io.jenkins.blueocean.listeners.NodeDownstreamBuildAction')
                .map(action => ({ runDescription: action.description, runLink: action.link.href }));
        }

        const generalLogPager = !this.pager.pending && !isPipelineQueued && noResultsToDisplay ? KaraokeService.generalLogPager(augmenter, location) : '';
        const { data: logArray, hasMore } = !this.pager.pending && !isPipelineQueued && noResultsToDisplay && generalLogPager.log ? generalLogPager.log : '';

        const stageRestartLink = () => {
            if (this.pager.currentNode) {
                let nodeRestartId = this.pager.currentNode.restartable ? this.pager.currentNode.id : '';
                let nodeRestartTitle = this.pager.currentNode.restartable ? title : '';

                if (this.pager.currentNode.restartable == false) {
                    let currentNodeParent = this.pager.nodes.data.model.filter(node => node.id == this.pager.currentNode.firstParent)[0];

                    while (currentNodeParent) {
                        if (currentNodeParent && currentNodeParent.restartable) {
                            nodeRestartId = currentNodeParent.id;
                            nodeRestartTitle = currentNodeParent.title;
                            break;
                        }
                        currentNodeParent = this.pager.nodes.data.model.filter(node => node.id == currentNodeParent.firstParent)[0];
                    }
                }

                return nodeRestartId ? (
                    <StageRestartLink
                        title={nodeRestartTitle}
                        t={t}
                        run={run}
                        nodeRestartId={nodeRestartId}
                        pipeline={pipeline}
                        onNavigation={switchRunDetails}
                    />
                ) : (
                    false
                );
            }

            return false;
        };

        return (
            <div>
                {<RunDescription run={this.props.run} t={t} />}

                {this.pager.nodes !== undefined && (
                    <Extensions.Renderer
                        extensionPoint="jenkins.pipeline.run.result"
                        selectedStage={this.pager.currentNode}
                        callback={afterClick}
                        nodes={this.pager.nodes.data.model}
                        pipelineName={pipeline.displayName}
                        branchName={augmenter.isMultiBranchPipeline ? branch : undefined}
                        runId={run.id}
                        run={run}
                        t={t}
                    />
                )}

                {!isPipelineQueued && (
                    <LogToolbar
                        fileName={generalLogPager ? augmenter.generalLogFileName : logFileName}
                        url={generalLogPager ? augmenter.generalLogUrl : logUrl}
                        title={title}
                        duration={!generalLogPager ? this.pager.currentNode.durationInMillis : ''}
                        running={!generalLogPager ? this.pager.currentNode.isRunning : false}
                        t={t}
                        stageRestartLink={stageRestartLink()}
                    />
                )}

                {this.pager.steps &&
                    !noResultsToDisplay && (
                        <Steps
                            onUserExpand={this.userExpandedStep}
                            onUserCollapse={this.userCollapsedStep}
                            tailLogs={this.state.tailLogs}
                            key={this.pager.currentStepsUrl}
                            nodeInformation={this.pager.steps.data}
                            followAlong={augmenter.karaoke}
                            augmenter={augmenter}
                            t={t}
                            scrollToBottom={scrollToBottom}
                            router={router}
                            location={location}
                            classicInputUrl={UrlBuilder.buildClassicInputUrl(pipeline, branch, run.id)}
                        />
                    )}

                {!this.pager.pending &&
                    !isPipelineQueued &&
                    noResultsToDisplay && (
                        <div className="nosteps-container">
                            <NoSteps
                                translation={t}
                                titleKey="rundetail.pipeline.nosteps.message.title"
                                messageKey="rundetail.pipeline.nosteps.message.description"
                            />
                            <LogConsole
                                {...{
                                    t,
                                    router,
                                    location,
                                    hasMore,
                                    logArray,
                                    currentLogUrl: augmenter.generalLogUrl,
                                    key: augmenter.generalLogUrl,
                                }}
                            />
                        </div>
                    )}

                {downstreamRuns.length > 0 && (
                    <div>
                        <div className="DownstreamRuns-header">{t('rundetail.pipeline.downstream')}</div>
                        <DownstreamRuns runs={downstreamRuns} t={t} locale={locale} />
                    </div>
                )}

                {isPipelineQueued && (
                    <QueuedState
                        translation={t}
                        titleKey="rundetail.pipeline.waiting.message.title"
                        messageKey="rundetail.pipeline.waiting.message.description"
                        message={run.causeOfBlockage}
                    />
                )}
            </div>
        );
    }
}

Pipeline.childContextTypes = {
    params: PropTypes.object,
    location: PropTypes.object,
    activityService: PropTypes.object,
};

Pipeline.propTypes = {
    augmenter: PropTypes.object,
    pipeline: PropTypes.object,
    branch: PropTypes.string,
    run: PropTypes.object,
    t: PropTypes.func,
    locale: PropTypes.string,
    router: PropTypes.object,
    location: PropTypes.object,
    scrollToBottom: PropTypes.bool,
    params: PropTypes.object,
    activityService: PropTypes.object,
};

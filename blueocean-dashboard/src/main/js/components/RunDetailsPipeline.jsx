import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import Extensions from '@jenkins-cd/js-extensions';

import {
    calculateLogView,
    calculateStepsBaseUrl,
    calculateRunLogURLObject,
    calculateNodeBaseUrl,
    calculateFetchAll,
    buildClassicInputUrl,
    sseConnection,
    logging,
} from '@jenkins-cd/blueocean-core-js';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { Icon } from '@jenkins-cd/react-material-icons';

import LogConsoleView from './LogConsoleView';
import LogToolbar from './LogToolbar';
import Steps from './Steps';
import {
    steps as stepsSelector,
    logs as logSelector,
    node as nodeSelector,
    nodes as nodesSelector,
    actions,
    connect,
    createSelector,
} from '../redux';

import { calculateNode } from '../util/KaraokeHelper';

const logger = logging.logger('io.jenkins.blueocean.dashboard.RunDetailsPipeline');

const { string, object, any, func } = PropTypes;

// FIXME: needs to use i18n for translations
const QueuedState = () => (
    <EmptyStateView tightSpacing>
        <p>
            <Icon {...{
                size: 20,
                icon: 'timer',
                style: { fill: '#fff' },
            }}
            />
            <span className="waiting">Waiting for run to start.</span>
        </p>
    </EmptyStateView>
);

// It should really be using capability using /rest/classes API
const supportsNodes = (result) => result && result._class === 'io.jenkins.blueocean.rest.impl.pipeline.PipelineRunImpl';

export class RunDetailsPipeline extends Component {
    constructor(props) {
        super(props);
        // we do not want to follow any builds that are finished
        this.state = { followAlong: props && props.result && props.result.state !== 'FINISHED' };
        this.listener = {};
        this._handleKeys = this._handleKeys.bind(this);
        this._onScrollHandler = this._onScrollHandler.bind(this);
        this._onSseEvent = this._onSseEvent.bind(this);
    }

    componentWillMount() {
        const { fetchNodes, result } = this.props;
        this.mergedConfig = this.generateConfig(this.props);
        if (!result.isQueued()) {
            if (this.mergedConfig.supportsNode) {
                fetchNodes(this.mergedConfig);
            }
        }

        this.listener.ssePipeline = sseConnection.subscribe('pipeline', this._onSseEvent);
        this.listener.sseJob = sseConnection.subscribe('job', this._onSseEvent);
    }

    componentDidMount() {
        const { result } = this.props;

        if (!result.isQueued()) {// FIXME: when https://issues.jenkins-ci.org/browse/JENKINS-37708 is fixed, test whether it breaks karaoke on freestyle
            // determine scroll area
            const domNode = ReactDOM.findDOMNode(this.refs.scrollArea);
            // add both listener, one to the scroll area and another to the whole document
            if (domNode) {
                domNode.addEventListener('wheel', this._onScrollHandler, false);
            }
            document.addEventListener('keydown', this._handleKeys, false);
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.result.isQueued()) {
            return;
        }
        const followAlong = this.state.followAlong;
        this.mergedConfig = this.generateConfig({ ...nextProps, followAlong });

        // we do not want any timeouts if we are not doing karaoke
        if (!this.state.followAlong && this.timeout) {
            clearTimeout(this.timeout);
        }
        // calculate if we need to trigger any actions to get into the right state (is plain js for testing reasons)
        const nodeAction = calculateNode(this.props, nextProps, this.mergedConfig);
        if (nodeAction && nodeAction.action) {
            // use updated config
            this.mergedConfig = nodeAction.config;
            // we may need to stop following
            if (this.state.followAlong !== nodeAction.state.followAlong) {
                this.setState({ followAlong: nodeAction.state.followAlong });
            }
            // if we have actions we fire them
            this.props[nodeAction.action](this.mergedConfig);
        }
    }

    componentWillUnmount() {
        if (this.listener.ssePipeline) {
            sseConnection.unsubscribe(this.listener.ssePipeline);
            delete this.listener.ssePipeline;
        }
        if (this.listener.sseJob) {
            sseConnection.unsubscribe(this.listener.sseJob);
            delete this.listener.sseJob;
        }

        if (this.props.result.isQueued()) {
            return;
        }
        const domNode = ReactDOM.findDOMNode(this.refs.scrollArea);
        this.props.cleanNodePointer();
        clearTimeout(this.timeout);
        if (domNode) {
            domNode.removeEventListener('wheel', this._onScrollHandler);
        }
        document.removeEventListener('keydown', this._handleKeys);
    }

    // need to register handler to step out of karaoke mode
    // we bail out on scroll up
    _onScrollHandler(elem) {
        if (elem.deltaY < 0 && this.state.followAlong) {
            this.setState({ followAlong: false });
        }
    }

    // we bail out on arrow_up key
    _handleKeys(event) {
        if (event.keyCode === 38 && this.state.followAlong) {
            this.setState({ followAlong: false });
        }
    }

    // Listen for pipeline flow node events.
    // We filter them only for steps and the end event all other we let pass
    _onSseEvent(event) {
        const { fetchNodes, fetchSteps, removeStep, removeLogs } = this.props;
        // we are using try/catch to throw an early out error
        try {
            const jenkinsEvent = event.jenkins_event;
            const runId = this.props.result.id;
            // we get events from the pipeline and the job channel, they have different naming for the id
            if (event.pipeline_run_id !== runId && event.jenkins_object_id !== runId) {
                // console.log('early out');
                throw new Error('exit');
            }
            // we turn on refetch so we always fetch a new Node result
            const refetch = true;
            const refetchNodes = () => {
                delete this.mergedConfig.node;
                fetchNodes({ ...this.mergedConfig, refetch });
            };
            switch (jenkinsEvent) {

            // In all in the following cases we need to update the display of the run
            case 'job_run_started':
            case 'job_run_unpaused':
            case 'pipeline_stage':
            case 'pipeline_start':
            case 'pipeline_block_start':
            case 'pipeline_block_end':
                {
                    refetchNodes();
                    break;
                }
            case 'pipeline_step':
                {
                    // we are not using an early out for the events since we want to refresh the node if we finished
                    if (this.state.followAlong) { // if we do it means we want karaoke
                        let parallel = true;// JENKINS-37962 FIXME the problem is with new syntax that is not reporting satge_id
                        if (event.pipeline_step_stage_id && this.mergedConfig.nodeReducer.parent) {
                            parallel = event.pipeline_step_stage_id !== this.mergedConfig.nodeReducer.parent;
                        }
                        /*
                         * if the step_stage_id has changed we need to change the focus, however if we in a parallel
                         * node we only want to fetch the steps, since it seems that the "parent" is the reporter of
                         * some steps.
                         */
                        if (event.pipeline_step_stage_id !== this.mergedConfig.node && parallel) {
                            // console.log('nodes fetching via ssePipeline triggered');
                            refetchNodes();
                        } else {
                            // console.log('only steps fetching via ssePipeline triggered');
                            fetchSteps({ ...this.mergedConfig, refetch });
                        }
                    }
                    break;
                }
            case 'pipeline_end': // FIXME: the following code will be go away when refactoring to mobx
                {
                    // get all steps from the current run, we use the nodeKey and remove the last bit
                    const keyArray = this.mergedConfig.nodeKey.split('/');
                    // remove last part either / or nodes
                    keyArray.pop();
                    // check whether we started with ending /, if so we need to pop again
                    if (keyArray[keyArray.length - 1] === 'nodes') {
                        keyArray.pop();
                    }
                    // create the base id
                    const searchString = keyArray.join('/');
                    // fire to remove all logs from cache
                    removeLogs(searchString);
                    /** we have to check now if all steps have been declared as finished,
                     * if not we will remove them from the cache so we get the final information*/
                    const notFinishedSteps = Object.keys(this.props.steps)
                        .filter((item) => item.indexOf(searchString) !== -1)
                        .map((step) => this.props.steps[step])
                        .filter((nodeSteps) => !nodeSteps.isFinished);
                    /**
                     * we always should have one item in our array which is the last step of the pipeline
                      */
                    if (notFinishedSteps.length >= 1) {
                        notFinishedSteps.map((step) => removeStep(step.nodesBaseUrl));
                    }
                    // we always want to refresh if the run has finished
                    refetchNodes();
                    break;
                }
            default:
                {
                    // console.log(event);
                }
            }
        } catch (e) {
            // we only ignore the exit error
            if (e.message !== 'exit') {
                throw e;
            }
        }
    }

    /**
     * We are testing whether we are in parallel mode by comparing the current selected node (nodeReducer)
     * with an other (in our case the one we clicked)
     * @param nodeInfo {Object} node that we have clicked on the pipelineGraph
     * @returns {boolean} true when parallel, false otherwise
     */
    isParallel(nodeInfo) {
        // in case we have edges arrays we compare the first edge, if not we know we are not in parallel mode
        return this.mergedConfig.nodeReducer.edges && this.mergedConfig.nodeReducer.edges[0] && nodeInfo.edges[0] ?
            this.mergedConfig.nodeReducer.edges[0].id === nodeInfo.edges[0].id : false;
    }

    generateConfig(props) {
        const { config = {} } = this.context;
        const followAlong = this.state.followAlong;
        const { isMultiBranch, params, result, steps, nodes } = props;
        const fetchAll = calculateFetchAll(props);
        const forceLogView = calculateLogView(props);
        // we would use default properties however the node can be null so no default properties will be triggered
        let { nodeReducer } = props;
        if (!nodeReducer) {
            nodeReducer = { id: null, displayName: 'Steps' };
        }
        // if we have a node param we do not want the calculation of the focused node
        const node = params.node || nodeReducer.id;
        // It should really be using capability using /rest/classes API
        const supportsNode = supportsNodes(result);
        // do we have a running job?
        const isRunning = result && result.state && result.state !== 'FINISHED';
        // Merge config
        const calculatedResponse = {
            ...config,
            supportsNode,
            isMultiBranch,
            node,
            nodeReducer,
            followAlong,
            fetchAll,
            forceLogView,
            isRunning,
            name: params.pipeline,
            branch: params.branch,
            runId: params.runId,
        };
        // get the key for the steps we want to display
        const stepKey = calculateStepsBaseUrl(calculatedResponse);
        // get the key for the node we want to display
        const nodeKey = calculateNodeBaseUrl(calculatedResponse);
        // get the currentSteps (identified by the prior key)
        const currentSteps = steps ? steps[stepKey] : null;
        // do we have steps
        const noSteps = currentSteps && currentSteps.model ? currentSteps.model.length === 0 : true;
        // does the node has any results/steps
        let hasResultsForSteps = nodes && nodes[nodeKey] ? nodes[nodeKey].hasResultsForSteps : false;
        if ((noSteps !== null && !noSteps) || (isRunning && supportsNode && !noSteps)) {
            hasResultsForSteps = true;
        }
        // are we treating a queued node
        const isPipelineQueued = !hasResultsForSteps && (noSteps === null || noSteps) && isRunning;
        return {
            ...calculatedResponse,
            stepKey,
            nodeKey,
            currentSteps,
            noSteps,
            hasResultsForSteps,
            isPipelineQueued,
        };
    }

    render() {
        const { location, router } = this.context;

        const { isMultiBranch, nodes, result: run, params, t } = this.props;

        if (run.isQueued()) {
            return <QueuedState />;
        }
        const { nodeKey, supportsNode, noSteps, currentSteps, hasResultsForSteps, isPipelineQueued } = this.mergedConfig;// supportsNodes(run);
        const resultRun = !run.isCompleted() ? run.state : run.result;
        const followAlong = this.state.followAlong;
        // in certain cases we want that the log component will scroll to the end of a log
        const scrollToBottom =
                resultRun.toLowerCase() === 'failure'
                || (resultRun.toLowerCase() === 'running' && followAlong)
            ;

        const logGeneral = calculateRunLogURLObject(this.mergedConfig);

        let title = this.mergedConfig.nodeReducer.displayName;
        if (this.mergedConfig.nodeReducer.id !== null && title) {
            title = `${t('rundetail.pipeline.steps', { defaultValue: 'Steps' })} - ${title}`;
        }
        // here we decide what to do next if somebody clicks on a flowNode
        const afterClick = (id) => {
            // get some information about the node the user clicked
            const nodeInfo = nodes[nodeKey].model.filter((item) => item.id === id)[0];
            const pathname = location.pathname;
            let newPath;
            // if path ends with pipeline we simply use it
            if (pathname.endsWith('pipeline/')) {
                newPath = pathname;
            } else if (pathname.endsWith('pipeline')) {
                newPath = `${pathname}/`;
            } else {
                // remove last bits
                const pathArray = pathname.split('/');
                pathArray.pop();
                if (pathname.endsWith('/')) {
                    pathArray.pop();
                }
                pathArray.shift();
                newPath = `${pathArray.join('/')}/`;
            }
            // check whether we have a parallel node
            const isParallel = this.isParallel(nodeInfo);

            // we only want to redirect to the node if the node is finished
            if (nodeInfo.state === 'FINISHED' || isParallel) {
                newPath = `${newPath}${id}`;
            }
            // see whether we need to update the state
            if ((nodeInfo.state === 'FINISHED' || isParallel) && followAlong) {
                this.setState({ followAlong: false });
            }
            if (nodeInfo.state !== 'FINISHED' && !followAlong) {
                this.setState({ followAlong: true });
            }
            router.push(newPath);
        };

        const shouldShowLogHeader = noSteps !== null && !noSteps;
        const stepScrollAreaClass = `step-scroll-area ${followAlong ? 'follow-along-on' : 'follow-along-off'}`;

        const shouldShowCV = (!hasResultsForSteps && !isPipelineQueued) || !supportsNode || this.mergedConfig.forceLogView;
        const shouldShowEmptyState = !isPipelineQueued && hasResultsForSteps && noSteps;

        logger.debug('display helper', { shouldShowCV, shouldShowLogHeader, shouldShowEmptyState });
        const pipe = { fullName: this.props.pipeline.fullName };
        if (isMultiBranch) {
            pipe.fullName += `/${params.branch}`;
        }
        const classicInputUrl = buildClassicInputUrl(pipe, run.id);
        logger.debug('classic Input url', classicInputUrl, pipe);
        return (
            <div ref="scrollArea" className={stepScrollAreaClass}>
                { (hasResultsForSteps || isPipelineQueued) && nodes && nodes[nodeKey] && !this.mergedConfig.forceLogView && <Extensions.Renderer
                  extensionPoint="jenkins.pipeline.run.result"
                  selectedStage={this.mergedConfig.nodeReducer}
                  callback={afterClick}
                  nodes={nodes[nodeKey].model}
                  pipelineName={name}
                  branchName={isMultiBranch ? params.branch : undefined}
                  runId={run.id}
                  run={run}
                  t={t}
                />
                }
                { hasResultsForSteps && shouldShowLogHeader && !this.mergedConfig.forceLogView &&
                    <LogToolbar
                      fileName={logGeneral.fileName}
                      url={logGeneral.url}
                      title={title}
                    />
                }
                { hasResultsForSteps && currentSteps && !this.mergedConfig.forceLogView && <Steps
                  nodeInformation={currentSteps}
                  {...{
                      followAlong,
                      router,
                      classicInputUrl,
                      scrollToBottom,
                      ...this.props,
                      ...this.state,
                      url: logGeneral.url,
                      mergedConfig: this.mergedConfig,
                  }}
                />
                }
                { isPipelineQueued && supportsNode && <QueuedState /> }
                { shouldShowEmptyState && !this.mergedConfig.forceLogView && <EmptyStateView tightSpacing>
                    <p>{t('rundetail.pipeline.nosteps', { defaultValue: 'There are no logs' })}</p>
                </EmptyStateView>
                }
                { shouldShowCV && <LogConsoleView
                  {
                    ...{
                        router,
                        title: t('rundetail.pipeline.logs', { defaultValue: 'Logs' }),
                        scrollToBottom,
                        ...this.props,
                        ...this.state,
                        url: logGeneral.url,
                        mergedConfig: this.mergedConfig,
                    }
                  }
                /> }
            </div>
        );
    }

}

RunDetailsPipeline.propTypes = {
    pipeline: object,
    isMultiBranch: any,
    params: object,
    result: object,
    fileName: string,
    url: string,
    fetchNodes: func,
    setNode: func,
    fetchSteps: func,
    removeStep: func,
    removeLogs: func,
    cleanNodePointer: func,
    steps: object,
    nodes: object,
    nodeReducer: object,
    t: func,
};

RunDetailsPipeline.contextTypes = {
    config: object.isRequired,
    params: object,
    pipeline: object,
    router: object.isRequired, // From react-router
    location: object.isRequired, // From react-router
};

const selectors = createSelector(
    [stepsSelector, logSelector, nodeSelector, nodesSelector],
    (steps, logs, nodeReducer, nodes) => ({ steps, logs, nodeReducer, nodes }));

export default connect(selectors, actions)(RunDetailsPipeline);

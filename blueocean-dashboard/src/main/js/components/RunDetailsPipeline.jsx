import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import Extensions from '@jenkins-cd/js-extensions';
import LogConsoleView from './LogConsoleView';
import * as sse from '@jenkins-cd/sse-gateway';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { Icon } from 'react-material-icons-blue';

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

import { calculateLogView, calculateStepsBaseUrl, calculateRunLogURLObject, calculateNodeBaseUrl, calculateFetchAll } from '../util/UrlUtils';
import { calculateNode } from '../util/KaraokeHelper';


const { string, object, any, func } = PropTypes;

const QueuedState = () => (
    <EmptyStateView tightSpacing>
        <p>
            <Icon {...{
                size: 20,
                icon: 'timer',
                style: { fill: '#fff' },
            }}
            />
            <span>Waiting for run to start.</span>
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

        this.listener.sse = sse.subscribe('pipeline', this._onSseEvent);
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
        if (this.props.result.isQueued()) {
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
        if (this.listener.sse) {
            sse.unsubscribe(this.listener.sse);
            delete this.listener.sse;
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
        const { fetchNodes, fetchSteps } = this.props;
        const jenkinsEvent = event.jenkins_event;
        // we are using try/catch to throw an early out error
        try {
            if (event.pipeline_run_id !== this.props.result.id) {
                // console.log('early out');
                throw new Error('exit');
            }
            // we turn on refetch so we always fetch a new Node result
            const refetch = true;
            switch (jenkinsEvent) {
            case 'pipeline_step':
                {
                    // we are not using an early out for the events since we want to refresh the node if we finished
                    if (this.state.followAlong) { // if we do it means we want karaoke
                        // if the step_stage_id has changed we need to change the focus
                        if (event.pipeline_step_stage_id !== this.mergedConfig.node) {
                            // console.log('nodes fetching via sse triggered');
                            delete this.mergedConfig.node;
                            fetchNodes({ ...this.mergedConfig, refetch });
                        } else {
                            // console.log('only steps fetching via sse triggered');
                            fetchSteps({ ...this.mergedConfig, refetch });
                        }
                    }
                    break;
                }
            case 'pipeline_end':
                {
                    // we always want to refresh if the run has finished
                    fetchNodes({ ...this.mergedConfig, refetch });
                    break;
                }
            default:
                {
                    // //console.log(event);
                }
            }
        } catch (e) {
            // we only ignore the exit error
            if (e.message !== 'exit') {
                throw e;
            }
        }
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

        const { isMultiBranch, nodes, result: run, params } = this.props;

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
            title = `Steps - ${title}`;
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
            // we only want to redirect to the node if the node is finished
            if (nodeInfo.state === 'FINISHED') {
                newPath = `${newPath}${id}`;
            }
            // see whether we need to update the state
            if (nodeInfo.state === 'FINISHED' && followAlong) {
                this.setState({ followAlong: false });
            }
            if (nodeInfo.state !== 'FINISHED' && !followAlong) {
                this.setState({ followAlong: true });
            }
            router.push(newPath);
        };

        const shouldShowLogHeader = noSteps !== null && !noSteps;
        const stepScrollAreaClass = `step-scroll-area ${followAlong ? 'follow-along-on' : 'follow-along-off'}`;

        const logProps = {
            scrollToBottom,
            ...this.props,
            ...this.state,
            mergedConfig: this.mergedConfig,
        };

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
                  followAlong={followAlong}
                  router={router}
                  {...this.props}
                />
                }
                { isPipelineQueued && supportsNode && <QueuedState /> }
                { !isPipelineQueued && hasResultsForSteps && noSteps && !this.mergedConfig.forceLogView && <EmptyStateView tightSpacing>
                    <p>There are no steps.</p>
                </EmptyStateView>
                }
                { ((!hasResultsForSteps && !isPipelineQueued) || !supportsNode || this.mergedConfig.forceLogView) && <LogConsoleView {...logProps} /> }
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
    cleanNodePointer: func,
    steps: object,
    nodes: object,
    nodeReducer: object,
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

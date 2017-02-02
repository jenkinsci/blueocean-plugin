import React, { Component, PropTypes } from 'react';
import { PipelineGraph } from '@jenkins-cd/design-language';
import { i18nTranslator, logging } from '@jenkins-cd/blueocean-core-js';
import { TimeManager } from '../util/serverBrowserTimeHarmonize';

const timeManager = new TimeManager();
const { array, any, func, object, string } = PropTypes;
const logger = logging.logger('io.jenkins.blueocean.dashboard.PipelineRunGraph');
const translate = i18nTranslator('blueocean-dashboard');

function badNode(jenkinsNode) {
    // eslint-disable-next-line
    console.error('Malformed / missing Jenkins run node:', jenkinsNode);
    return new Error('convertJenkinsNodeDetails: malformed / missing Jenkins run node.');
}

function convertJenkinsNodeDetails(jenkinsNode, isCompleted) {
    if (!jenkinsNode
        || !jenkinsNode.displayName
        || !jenkinsNode.id) {
        throw badNode(jenkinsNode);
    }
    logger.debug('jenkinsNode', jenkinsNode);
    const isRunning = () => {
        switch (jenkinsNode.state) {
        case 'RUNNING':
        case 'PAUSED':
        case 'QUEUED':
            return true;
        default:
            return false;
        }
    };
    const { durationInMillis, startTime } = jenkinsNode;
    const { durationMillis } = timeManager.harmonizeTimes({
        isRunning: isRunning(),
        durationInMillis,
        startTime,
    });
    let completePercent = 0;
    let state = 'unknown';

    if (jenkinsNode.result === 'SUCCESS') {
        state = 'success';
        completePercent = 100;
    } else if (jenkinsNode.result === 'FAILURE') {
        state = 'failure';
        completePercent = 100;
    } else if (jenkinsNode.state === 'PAUSED') {
        state = 'paused';
        completePercent = 100;
    } else if (jenkinsNode.result === 'UNSTABLE') {
        state = 'unstable';
        completePercent = 100;
    } else if (jenkinsNode.state === 'RUNNING') {
        state = 'running';
        completePercent = 50;
    } else if (jenkinsNode.state === 'QUEUED'
        || (jenkinsNode.state === null && !isCompleted)) {
        state = 'queued';
        completePercent = 0;
    } else if (jenkinsNode.state === 'NOT_BUILT'
        || jenkinsNode.state === 'ABORTED'
        || jenkinsNode.state == null) {
        state = 'not_built';
        completePercent = 0;
    }
    const i18nDuration = timeManager.format(durationMillis, translate('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' }));

    const title = translate(`common.state.${state}`, { 0: i18nDuration });

    const converted = {
        name: jenkinsNode.displayName,
        children: [],
        state,
        completePercent,
        id: jenkinsNode.id,
        title,
    };
    logger.debug('converted node', converted);
    return converted;
}

/**
 * Convert the graph results of a run as reported by Jenkins into the
 * model required by the PipelineGraph component
 *
 * We need isCompleted to determine wether nodes that haven't been run are
 * still pending or simply weren't executed due to logic or early-abort
 * (either failure or intervention)
 */
export function convertJenkinsNodeGraph(jenkinsGraph, isCompleted) {
    if (!jenkinsGraph || !jenkinsGraph.length) {
        return [];
    }

    const results = [];
    const originalNodeForId = {};
    const convertedNodeForId = {};
    let firstNode = undefined;

    // Convert the basic details of nodes, and index them by id
    jenkinsGraph.forEach(jenkinsNode => {
        const convertedNode = convertJenkinsNodeDetails(jenkinsNode, isCompleted);
        const { id } = convertedNode;

        firstNode = firstNode || convertedNode;
        convertedNodeForId[id] = convertedNode;
        originalNodeForId[id] = jenkinsNode;
    });

    // Follow the graph and build our results
    let currentNode = firstNode;
    while (currentNode) {
        results.push(currentNode);

        let nextNode = null;
        const originalNode = originalNodeForId[currentNode.id];
        const edges = originalNode.edges || [];

        if (edges.length === 1) {
            // Single following (sibling) node
            nextNode = convertedNodeForId[edges[0].id];
        } else if (edges.length > 1) {
            // Multiple following nodes are child nodes not siblings
            currentNode.children = edges.map(edge => convertedNodeForId[edge.id]);

            // We need to look at the child node's edges to figure out what the next sibling node is
            const childEdges = originalNodeForId[edges[0].id].edges || [];
            if (childEdges.length) {
                nextNode = convertedNodeForId[childEdges[0].id];
            }
        }

        currentNode = nextNode;
    }

    return results;
}

export default class PipelineRunGraph extends Component {

    constructor(props) {
        super(props);
        this.lastData = null;
        this.state = { graphNodes: null };
    }

    componentWillMount() {
        const { nodes, run } = this.props;
        this.processData(nodes, run);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.nodes !== this.lastData || nextProps.run !== this.props.run) {
            this.processData(nextProps.nodes, nextProps.run);
        }
    }

    processData(newData, run) {
        this.lastData = newData;
        const isCompleted = run.state.toUpperCase() === 'FINISHED';

        const convertedGraph = convertJenkinsNodeGraph(newData, isCompleted);

        this.setState({
            graphNodes: convertedGraph,
        });
    }

    render() {
        const { graphNodes, t } = this.state;

        if (!graphNodes) {
            // FIXME: Make a placeholder empty state when nodes is null (loading)
            return (<div>{t('common.pager.loading', {
                defaultValue: 'Loading...',
            })}</div>);
        } else if (graphNodes.length === 0) {
            // Do nothing when there's no nodes
            return null;
        }

        const outerDivStyle = {
            display: 'flex',
            justifyContent: 'center',
        };
        const id = this.props.selectedStage.id;
        let selectedStage = graphNodes.filter((item) => {
            let matches = item.id === id;
            if (!matches && item.children.length > 0) {
                const childMatches = item.children.filter(child => child.id === id);
                matches = childMatches.length === 1;
            }
            return matches;
        });
        if (selectedStage[0] && selectedStage[0].id !== id && selectedStage[0].children.length > 0) {
            selectedStage = selectedStage[0].children.filter(item => item.id === id);
        }
        return (
            <div style={outerDivStyle}>
                <PipelineGraph
                  stages={graphNodes}
                  selectedStage={selectedStage[0]}
                  onNodeClick={
                    (name, stageId) => {
                        this.props.callback(stageId);
                    }
                  }
                />
            </div>
        );
    }
}

PipelineRunGraph.propTypes = {
    pipelineName: string,
    branchName: string,
    run: object,
    nodes: array,
    node: any,
    selectedStage: object,
    callback: func,
};


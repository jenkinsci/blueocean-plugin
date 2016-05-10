import React, { Component, PropTypes } from 'react';
import { fetch, PipelineGraph } from '@jenkins-cd/design-language';

function badNode(jenkinsNode) {
    // eslint-disable-next-line
    console.error('Malformed / missing Jenkins run node:', jenkinsNode);
    return new Error('convertJenkinsNodeDetails: malformed / missing Jenkins run node.');
}

function convertJenkinsNodeDetails(jenkinsNode) {
    if (!jenkinsNode
        || !jenkinsNode.displayName
        || !jenkinsNode.id) {
        throw badNode(jenkinsNode);
    }

    let completePercent = 0;
    let state = 'unknown';

    if (jenkinsNode.result === 'SUCCESS') {
        state = 'success';
        completePercent = 100;
    } else if (jenkinsNode.result === 'FAILURE') {
        state = 'failure';
        completePercent = 100;
    } else if (jenkinsNode.state === 'RUNNING') {
        state = 'running';
        completePercent = 50;
    } else if (jenkinsNode.state === 'QUEUED'
        || jenkinsNode.state === null) {
        state = 'queued';
        completePercent = 0;
    } else if (jenkinsNode.state === 'NOT_BUILT'
        || jenkinsNode.state === 'ABORTED') {
        state = 'not_built';
        completePercent = 0;
    }

    return {
        name: jenkinsNode.displayName,
        children: [],
        state,
        completePercent,
        id: jenkinsNode.id,
    };
}

/**
 * Convert the graph results of a run as reported by Jenkins into the
 * model required by the PipelineGraph component
 */
export function convertJenkinsNodeGraph(jenkinsGraph) {
    if (!jenkinsGraph || !jenkinsGraph.length) {
        return [];
    }

    const results = [];
    const originalNodeForId = {};
    const convertedNodeForId = {};
    let firstNode = undefined;

    // Convert the basic details of nodes, and index them by id
    jenkinsGraph.forEach(jenkinsNode => {
        const convertedNode = convertJenkinsNodeDetails(jenkinsNode);
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

export class PipelineRunGraph extends Component {

    constructor(props) {
        super(props);
        this.lastData = null;
        this.state = { graphNodes: null };
    }

    componentWillMount() {
        this.processData(this.props.data);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.data !== this.lastData) {
            this.processData(nextProps.data);
        }
    }

    processData(newData) {
        this.lastData = newData;

        this.setState({
            graphNodes: convertJenkinsNodeGraph(newData),
        });
    }

    render() {
        const { graphNodes } = this.state;

        if (!graphNodes) {
            // FIXME: Make a placeholder empty state when data is null (loading)
            return <div>Loading...</div>;
        } else if (graphNodes.length === 0) {
            // Do nothing when there's no node data
            return null;
        }

        const outerDivStyle = {
            display: 'flex',
            justifyContent: 'center',
        };

        return (
            <div style={outerDivStyle}>
                <PipelineGraph stages={graphNodes} />
            </div>
        );
    }
}

PipelineRunGraph.propTypes = {
    pipelineName: PropTypes.string,
    branchName: PropTypes.string,
    runId: PropTypes.string,
    data: PropTypes.array,
};

export default fetch(PipelineRunGraph, (props, config) => {
    const { pipelineName, branchName, runId } = props;

    if (!pipelineName || !runId) {
        return null; // Nothing to load yet
    }

    let id;

    if (branchName) {
        // Multibranch
        // eslint-disable-next-line
        id = encodeURIComponent(pipelineName) + '/branches/' +
            encodeURIComponent(branchName);
    } else {
        // No multibranch
        id = encodeURIComponent(pipelineName);
    }

    // eslint-disable-next-line
    return config.getAppURLBase() +
        '/rest/organizations/jenkins' +
        `/pipelines/${id}/runs/${runId}/nodes/`;
}) ;


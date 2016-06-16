import React, { Component, PropTypes } from 'react';
import { PipelineGraph } from '@jenkins-cd/design-language';

const { string, array, object, any } = PropTypes;


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

export default class PipelineRunGraph extends Component {

    constructor(props) {
        super(props);
        this.lastData = null;
        this.state = { graphNodes: null };
    }

    componentWillMount() {
        this.processData(this.props.nodes);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.nodes !== this.lastData) {
            this.processData(nextProps.nodes);
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
            // FIXME: Make a placeholder empty state when nodes is null (loading)
            return <div>Loading...</div>;
        } else if (graphNodes.length === 0) {
            // Do nothing when there's no nodes
            return null;
        }

        const outerDivStyle = {
            display: 'flex',
            justifyContent: 'center',
        };

        return (
            <div style={outerDivStyle}>
                <PipelineGraph
                  stages={graphNodes}
                  onNodeClick={
                    (name, id) => {
                        const pathname = this.props.location.pathname;
                        // if path ends with pipeline we simply add the node id
                        if (pathname.endsWith('pipeline/')) {
                            this.props.router.push(`${pathname}${id}`);
                        } else if (pathname.endsWith('pipeline')) {
                            this.props.router.push(`${pathname}/${id}`);
                        } else {
                            // remove last bit and replace it with node
                            const pathArray = pathname.split('/');
                            pathArray.pop();
                            if (pathname.endsWith('/')) {
                                pathArray.pop();
                            }
                            pathArray.shift();
                            this.props.router.push(`${pathArray.join('/')}/${id}`);
                        }
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
    runId: string,
    nodes: array,
    node: any,
    router: object.isRequired, // From react-router
    location: object.isRequired, // From react-router
};

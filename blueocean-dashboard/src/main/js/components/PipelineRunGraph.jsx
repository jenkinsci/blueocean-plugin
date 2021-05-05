import React, { Component, PropTypes } from 'react';
import { PipelineGraph } from '@jenkins-cd/pipeline-graph-widget';

import { convertJenkinsNodeGraph } from './GraphNodeConverter';

const { array, any, func, object, string } = PropTypes;

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
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        const convertedGraph = convertJenkinsNodeGraph(newData, isCompleted, skewMillis);

        this.setState({
            graphNodes: convertedGraph,
        });
    }

    graphNodeClicked = (name, stageId) => {
        const { callback } = this.props;
        if (callback) {
            callback(stageId);
        }
    };

    render() {
        const { graphNodes, t } = this.state;

        if (!graphNodes) {
            // FIXME: Make a placeholder empty state when nodes is null (loading)
            return (
                <div>
                    {t('common.pager.loading', {
                        defaultValue: 'Loading...',
                    })}
                </div>
            );
        } else if (graphNodes.length === 0) {
            // Do nothing when there's no nodes
            return null;
        }

        const id = this.props.selectedStage.id;
        let selectedStage = null;

        // Find selected stage by id
        for (const topStage of graphNodes) {
            if (topStage.id === id) {
                selectedStage = topStage;
            } else {
                for (const child of topStage.children) {
                    let currentStage = child;

                    while (currentStage) {
                        if (currentStage.id === id) {
                            selectedStage = currentStage;
                            break;
                        }

                        currentStage = currentStage.nextSibling;
                    }
                }
            }
            if (selectedStage) {
                break;
            }
        }

        return <PipelineGraph stages={graphNodes} selectedStage={selectedStage} onNodeClick={this.graphNodeClicked} />;
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

PipelineRunGraph.contextTypes = {
    config: object.isRequired,
};

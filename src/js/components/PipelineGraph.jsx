// @flow

import React, { Component, PropTypes } from 'react';
import {getGroupForResult, decodeResultValue} from './status/StatusIndicator';
import {strokeWidth as nodeStrokeWidth} from './status/SvgSpinner';

import type {Result} from './status/StatusIndicator';

// Dimensions used for layout, px
export const defaultLayout = {
    nodeSpacingH: 120,
    nodeSpacingV: 70,
    nodeRadius: 12,
    curveRadius: 12,
    connectorStrokeWidth: 3.5,
    labelOffsetV: 25,
    smallLabelOffsetV: 20
};

// Typedefs

type StageInfo = {
    name: string,
    title: string,
    state: Result,
    completePercent: number,
    id: number,
    children: Array<StageInfo>
};

type NodeInfo = {
    x: number,
    y: number,
    name: string,
    state: Result,
    completePercent: number,
    id: number,
    stage: StageInfo
};

type ConnectionInfo = [NodeInfo, NodeInfo];

type LabelInfo = {
    x: number,
    y: number,
    text: string,
    stage:StageInfo
};

type LayoutInfo = typeof defaultLayout;

// FIXME-FLOW: Currently need to duplicate react's propTypes obj in Flow.
// See: https://github.com/facebook/flow/issues/1770
type Props = {
    stages: Array<StageInfo>,
    layout: LayoutInfo,
    onNodeClick: (nodeName: string, id: string) => void,
    selectedStage: StageInfo
};

export class PipelineGraph extends Component {

    // Flow typedefs
    state: {
        nodes: Array<NodeInfo>,
        connections: Array<ConnectionInfo>,
        bigLabels: Array<LabelInfo>,
        smallLabels: Array<LabelInfo>,
        measuredWidth: number,
        measuredHeight: number,
        layout: LayoutInfo,
        selectedStage: StageInfo
    };

    constructor(props: Props) {
        super(props);
        this.state = {
            nodes: [],
            connections: [],
            bigLabels: [],
            smallLabels: [],
            measuredWidth: 0,
            measuredHeight: 0,
            layout: Object.assign({}, defaultLayout, props.layout),
            selectedStage: props.selectedStage
        };
    }

    componentWillMount() {
        this.stagesUpdated(this.props.stages);
    }

    componentWillReceiveProps(nextProps: Props) {

        let newState = null; // null == no new state
        let needsLayout = false;

        if (nextProps.layout != this.props.layout) {
            // TODO: Does layout obj really need to be in state?
            newState = {...newState, layout: Object.assign({}, defaultLayout, this.props.layout)};
            needsLayout = true;
        }

        if (nextProps.selectedStage !== this.props.selectedStage) {
            // If we're just changing selectedStage, we don't need to re-generate the children
            newState = {...newState, selectedStage: nextProps.selectedStage};
        }

        if (nextProps.stages !== this.props.stages) {
            needsLayout = true;
        }

        const doLayoutIfNeeded = () => {
            if (needsLayout) {
                this.stagesUpdated(nextProps.stages);
            }
        };

        if (newState) {
            // If we need to update the state, then we'll delay any layout changes
            this.setState(newState, doLayoutIfNeeded);
        } else {
            doLayoutIfNeeded();
        }
    }

    addConnectionDetails(connections: Array<ConnectionInfo>, previousNodes: Array<NodeInfo>, columnNodes: Array<NodeInfo>) {
        // Connect to top of previous/next column. Curves added when creating SVG

        // Collapse from previous node(s) to top column node
        for (const previousNode of previousNodes) {
            connections.push([previousNode, columnNodes[0]]);
        }

        // Expand from top previous node to column node(s) - first one done already above
        for (const columnNode of columnNodes.slice(1)) {
            connections.push([previousNodes[0], columnNode]);
        }
    }

    stagesUpdated(newStages: Array<StageInfo> = []) {
        // FIXME: Should we calculate based on expected text size guesstimate?
        const ypStart = 50;

        const { nodeSpacingH, nodeSpacingV } = this.state.layout;

        var nodes: Array<NodeInfo> = [];
        var connections: Array<ConnectionInfo> = [];
        var bigLabels: Array<LabelInfo> = [];
        var smallLabels: Array<LabelInfo> = [];

        // next node position
        var xp = nodeSpacingH / 2;
        var yp = 0;

        var previousNodes: Array<NodeInfo> = [];
        var mostColumnNodes = 0;

        // For reach top-level stage we have a column of node(s)
        for (const topStage of newStages) {

            yp = ypStart;

            // Always have a single bigLabel per top-level stage
            bigLabels.push({
                x: xp,
                y: yp,
                text: topStage.name,
                stage: topStage
            });

            // If stage has children, we don't draw a node for it, just its children
            const nodeStages = topStage.children && topStage.children.length ?
                topStage.children : [topStage];

            const columnNodes: Array<NodeInfo> = [];

            for (const nodeStage of nodeStages) {
                const node = {
                    x: xp,
                    y: yp,
                    name: nodeStage.name,
                    state: nodeStage.state,
                    completePercent: nodeStage.completePercent,
                    id: nodeStage.id,
                    stage: nodeStage
                };

                columnNodes.push(node);

                // Only separate child nodes need a smallLabel, as topStage already has a bigLabel
                if (nodeStage != topStage) {
                    smallLabels.push({
                        x: xp,
                        y: yp,
                        text: nodeStage.name,
                        stage: nodeStage
                    });
                }

                yp += nodeSpacingV;
            }

            if (previousNodes.length) {
                this.addConnectionDetails(connections, previousNodes, columnNodes);
            }

            xp += nodeSpacingH;
            mostColumnNodes = Math.max(mostColumnNodes, nodeStages.length);
            nodes.push(...columnNodes);
            previousNodes = columnNodes;
        }

        // Calc dimensions
        var measuredWidth = xp - Math.floor(nodeSpacingH / 2);
        const measuredHeight = ypStart + (mostColumnNodes * nodeSpacingV);

        this.setState({
            nodes,
            connections,
            bigLabels,
            smallLabels,
            measuredWidth,
            measuredHeight
        });
    }

    renderBigLabel(details: LabelInfo) {

        const { nodeSpacingH, labelOffsetV } = this.state.layout;

        const labelWidth = nodeSpacingH;
        const labelOffsetH = Math.floor(labelWidth * -0.5);

        // These are about layout more than appearance, so they should probably remain inline
        const bigLabelStyle = {
            position: "absolute",
            width: labelWidth,
            textAlign: "center",
            marginLeft: labelOffsetH,
            marginBottom: labelOffsetV
        };

        const x = details.x;
        const bottom = this.state.measuredHeight - details.y;

        const style = Object.assign({}, bigLabelStyle, {
            bottom: bottom + "px",
            left: x + "px"
        });

        const key = details.stage.id + "-big";

        const classNames = ["pipeline-big-label"];
        if (this.stageIsSelected(details.stage)
            || this.stageChildIsSelected(details.stage)) {
            classNames.push("selected");
        }

        return <div className={classNames.join(" ")} style={style} key={key}>{details.text}</div>;
    }

    renderSmallLabel(details: LabelInfo) {

        const {
            nodeSpacingH,
            curveRadius,
            smallLabelOffsetV } = this.state.layout;

        const smallLabelWidth = nodeSpacingH - (2 * curveRadius); // Fit between lines
        const smallLabelOffsetH = Math.floor(smallLabelWidth * -0.5);

        // These are about layout more than appearance, so they should probably remain inline
        const smallLabelStyle = {
            position: "absolute",
            width: smallLabelWidth,
            textAlign: "center",
            marginLeft: smallLabelOffsetH,
            marginTop: smallLabelOffsetV
        };

        const x = details.x;
        const top = details.y;

        const style = Object.assign({}, smallLabelStyle, {
            top: top,
            left: x
        });

        const key = details.stage.id + '-big';

        const classNames = ["pipeline-small-label"];
        if (this.stageIsSelected(details.stage)) {
            classNames.push("selected");
        }

        return <div className={classNames.join(" ")} style={style} key={key}>{details.text}</div>;
    }

    renderConnection(connection: ConnectionInfo) {

        const { nodeRadius, curveRadius, connectorStrokeWidth } = this.state.layout;

        const [leftNode, rightNode] = connection;
        const key = leftNode.name + leftNode.id + "_con_" + rightNode.name + rightNode.id;

        const leftPos = {
            x: leftNode.x + nodeRadius - (nodeStrokeWidth / 2),
            y: leftNode.y
        };

        const rightPos = {
            x: rightNode.x - nodeRadius + (nodeStrokeWidth / 2),
            y: rightNode.y
        };

        // Stroke props common to straight / curved connections
        const connectorStroke = {
            className: "pipeline-connector",
            strokeWidth: connectorStrokeWidth
        };

        if (leftPos.y == rightPos.y) {
            // Nice horizontal line
            return (<line {...connectorStroke}
                         key={key}
                         x1={leftPos.x}
                         y1={leftPos.y}
                         x2={rightPos.x}
                         y2={rightPos.y}/>);
        }

        // Otherwise, we'd like a curve

        const verticalDirection = Math.sign(rightPos.y - leftPos.y); // 1 == curve down, -1 == curve up
        const midPointX = Math.round((leftPos.x + rightPos.x) / 2 + (curveRadius * verticalDirection));
        const w1 = midPointX - curveRadius - leftPos.x;
        const w2 = rightPos.x - curveRadius - midPointX;
        const v = rightPos.y - leftPos.y - (2 * curveRadius * verticalDirection); // Will be -ive if curve up
        const cv = verticalDirection * curveRadius;

        const pathData = `M ${leftPos.x} ${leftPos.y}` // start position
                + ` l ${w1} 0` // first horizontal line
                + ` c ${curveRadius} 0 ${curveRadius} ${cv} ${curveRadius} ${cv}`  // turn
                + ` l 0 ${v}` // vertical line
                + ` c 0 ${cv} ${curveRadius} ${cv} ${curveRadius} ${cv}` // turn again
                + ` l ${w2} 0` // second horizontal line
            ;

        return <path {...connectorStroke} key={key} d={pathData} fill="none"/>;
    }

    renderNode(node: NodeInfo) {

        const nodeIsSelected = this.stageIsSelected(node.stage);
        const { nodeRadius, connectorStrokeWidth } = this.state.layout;
        // Use a bigger radius for invisible click/touch target
        const mouseTargetRadius = nodeRadius + (2 * connectorStrokeWidth);

        const resultClean = decodeResultValue(node.state);
        const key = "n_" + node.name + node.id;

        const completePercent = node.completePercent || 0;
        const groupChildren = [getGroupForResult(resultClean, completePercent, nodeRadius)];
        const { title } = node.stage;
        if (title) {
          groupChildren.push(<title>{ title }</title>);
        }
        // Add an invisible click/touch target, coz the nodes are small and (more importantly)
        // many are hollow.
        groupChildren.push(
            <circle r={mouseTargetRadius}
                    cursor="pointer"
                    className="pipeline-node-hittarget"
                    fillOpacity="0"
                    stroke="none"
                    onClick={() => this.nodeClicked(node)}/>
        );

        // All the nodes are in shared code, so they're rendered at 0,0 so we transform within a <g>
        const groupProps = {
            key,
            transform: `translate(${node.x},${node.y})`,
            className: nodeIsSelected ? "pipeline-node-selected" : "pipeline-node"
        };

        return React.createElement("g", groupProps, ...groupChildren);
    }

    renderSelectionHighlight() {

        const { nodeRadius, connectorStrokeWidth } = this.state.layout;
        const highlightRadius = nodeRadius + (0.49 * connectorStrokeWidth);
        let selectedNode = null;

        for (const node of this.state.nodes) {
            if (this.stageIsSelected(node.stage)) {
                selectedNode = node;
                break;
            }
        }

        if (!selectedNode) {
            return null;
        }

        const transform = `translate(${selectedNode.x} ${selectedNode.y})`;

        return (
            <g className="pipeline-selection-highlight" transform={transform}>
                <circle r={highlightRadius} strokeWidth={connectorStrokeWidth * 1.1}/>
            </g>
        );
    }

    // Put in a function so we can make improvements / multi-select
    stageIsSelected(stage: StageInfo) {
        const {selectedStage} = this.state;

        return selectedStage && selectedStage === stage;
    }

    stageChildIsSelected(stage: StageInfo) {
        const {children} = stage;
        const {selectedStage} = this.state;

        if (children && selectedStage) {
            for (const child of children) {
                if (child === selectedStage) {
                    return true;
                }
            }
        }
        return false;
    }

    nodeClicked(node:NodeInfo) {
        const stage = node.stage;
        const listener = this.props.onNodeClick;

        if (listener) {
            listener(stage.name, stage.id);
        }

        // Update selection
        this.setState({selectedStage: stage});
    }

    render() {
        const {
            nodes = [],
            connections = [],
            bigLabels = [],
            smallLabels = [],
            measuredWidth,
            measuredHeight } = this.state;

        // These are about layout more than appearance, so they should probably remain inline
        const outerDivStyle = {
            position: "relative", // So we can put the labels where we need them
            overflow: "visible" // So long labels can escape this component in layout
        };

        return (
            <div style={outerDivStyle}>
                <svg width={measuredWidth} height={measuredHeight}>
                    {this.renderSelectionHighlight()}
                    {connections.map(conn => this.renderConnection(conn))}
                    {nodes.map(node => this.renderNode(node))}
                </svg>
                {bigLabels.map(label => this.renderBigLabel(label))}
                {smallLabels.map(label => this.renderSmallLabel(label))}
            </div>
        );
    }
}

PipelineGraph.propTypes = {
    stages: PropTypes.array,
    layout: PropTypes.object,
    onNodeClick: PropTypes.func
};

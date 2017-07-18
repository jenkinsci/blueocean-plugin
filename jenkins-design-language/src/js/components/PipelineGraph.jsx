// @flow

import React, { Component, PropTypes } from 'react';
import { getGroupForResult, decodeResultValue } from './status/StatusIndicator';
import { strokeWidth as nodeStrokeWidth } from './status/SvgSpinner';
import { TruncatingLabel } from './TruncatingLabel';

import type { Result } from './status/StatusIndicator';

const ypStart = 55;

// Dimensions used for layout, px
export const defaultLayout = {
    nodeSpacingH: 120,
    nodeSpacingV: 70,
    nodeRadius: 12,
    curveRadius: 12,
    connectorStrokeWidth: 3.5,
    labelOffsetV: 20,
    smallLabelOffsetV: 15,
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

type CompositeConnection = {
    sourceNodes: Array<NodeInfo>,
    destinationNodes: Array<NodeInfo>,
    skippedNodes: Array<NodeInfo>
};

type LabelInfo = {
    x: number,
    y: number,
    text: string,
    stage: StageInfo
};

type LayoutInfo = typeof defaultLayout;

type SVGChildren = Array<any>; // TODO: Maybe refine this

// FIXME-FLOW: Currently need to duplicate react's propTypes obj in Flow.
// See: https://github.com/facebook/flow/issues/1770
type Props = {
    stages: Array<StageInfo>,
    layout: LayoutInfo,
    onNodeClick: (nodeName: string, id: string) => void,
    selectedStage: StageInfo
};

// Generate a react key for a connection
function connectorKey(leftNode, rightNode) {
    return leftNode.name + leftNode.id + '_con_' + rightNode.name + rightNode.id;
}

// For Debugging
let _debugKey = 111;
function debugPoint(cx, cy, message = 'debug') {
    const key = 'debug_' + (++_debugKey);

    return (
        <g key={key} title={message} stroke="pink" strokeWidth={3}>
            <line x1={cx - 5} y1={cy - 5} x2={cx + 5} y2={cy + 5} />
            <line x1={cx - 5} y1={cy + 5} x2={cx + 5} y2={cy - 5} />
        </g>
    );

    // return (
    //     <circle cx={cx} cy={cy} r="5" fill="pink" key={key} title={message}/>
    // );
}

export class PipelineGraph extends Component {

    // Flow typedefs
    state: {
        nodes: Array<NodeInfo>,
        connections: Array<CompositeConnection>, // TODO: Rename this once the new process is sorted
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
            selectedStage: props.selectedStage,
        };
    }

    componentWillMount() {
        this.stagesUpdated(this.props.stages);
    }

    componentWillReceiveProps(nextProps: Props) {

        let newState = null; // null == no new state
        let needsLayout = false;

        if (nextProps.layout != this.props.layout) {
            newState = { ...newState, layout: Object.assign({}, defaultLayout, this.props.layout) };
            needsLayout = true;
        }

        if (nextProps.selectedStage !== this.props.selectedStage) {
            // If we're just changing selectedStage, we don't need to re-generate the children
            newState = { ...newState, selectedStage: nextProps.selectedStage };
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

    stagesUpdated(newStages: Array<StageInfo> = []) {

        const { nodeSpacingH, nodeSpacingV } = this.state.layout;

        // The structures we're populating in this method
        const nodes: Array<NodeInfo> = [];
        const connections: Array<CompositeConnection> = [];
        const bigLabels: Array<LabelInfo> = [];
        const smallLabels: Array<LabelInfo> = [];

        // Location of next node to create
        let xp = nodeSpacingH / 2;
        let yp = 0;

        // Other state we need to maintain across columns
        let connectionSourceNodes: Array<NodeInfo> = []; // Column for last non-skipped stage, for connection generation
        let mostColumnNodes = 0; // "Tallest" column, for size calculation
        let skippedNodes = []; // Rendered nodes that need to be skipped by the next set of connections

        // For reach top-level stage we have a column of node(s)
        for (const topStage of newStages) {

            yp = ypStart;

            const isSkippedStage = topStage.state === 'skipped';

            // Always have a single bigLabel per top-level stage
            bigLabels.push({
                x: xp,
                y: yp,
                text: topStage.name,
                stage: topStage,
            });

            // If stage has children, we don't draw a node for it, just its children
            const nodeStages = topStage.children && topStage.children.length ?
                topStage.children : [topStage];

            const columnNodes: Array<NodeInfo> = [];

            for (let nodeStage of nodeStages) {

                if (!nodeStage) {
                    // Dummmy data to prevent render failing when not all stages' info are available(loading, etc)
                    nodeStage = {
                        children: [],
                        completePercent: 100,
                        id: -1,
                        name: 'Unable to display more',
                        state: 'unknown',
                        title: 'dummyTitle',
                    };
                }

                const node = {
                    x: xp,
                    y: yp,
                    name: nodeStage.name,
                    state: nodeStage.state,
                    completePercent: nodeStage.completePercent,
                    id: nodeStage.id,
                    stage: nodeStage,
                };

                columnNodes.push(node);

                // Only separate child nodes need a smallLabel, as topStage already has a bigLabel
                if (nodeStage != topStage) {
                    smallLabels.push({
                        x: xp,
                        y: yp,
                        text: nodeStage.name,
                        stage: nodeStage,
                    });
                }

                yp += nodeSpacingV;
            }

            if (isSkippedStage) {
                // Keep track of the nodes that have skipped status so we can route connections around them.
                skippedNodes.push(...columnNodes);
            } else {
                if (connectionSourceNodes.length) {
                    // Create a composite connection from source column to current, possibly skipping some
                    connections.push({
                        sourceNodes: connectionSourceNodes,
                        destinationNodes: columnNodes,
                        skippedNodes,
                    });
                }

                skippedNodes = [];

                // Any future connections should come from this column
                connectionSourceNodes = columnNodes;
            }

            xp += nodeSpacingH;
            mostColumnNodes = Math.max(mostColumnNodes, nodeStages.length);
            nodes.push(...columnNodes);
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
            measuredHeight,
        });
    }

    renderBigLabel(details: LabelInfo) {

        const {
            nodeSpacingH,
            labelOffsetV,
            connectorStrokeWidth,
        } = this.state.layout;

        const labelWidth = nodeSpacingH - connectorStrokeWidth * 2;
        const labelHeight = ypStart - labelOffsetV;
        const labelOffsetH = Math.floor(labelWidth / -2);

        // These are about layout more than appearance, so they should probably remain inline
        const bigLabelStyle = {
            position: 'absolute',
            width: labelWidth,
            maxHeight: labelHeight + 'px',
            textAlign: 'center',
            marginLeft: labelOffsetH,
        };

        const x = details.x;
        const bottom = this.state.measuredHeight - details.y + labelOffsetV;

        const style = Object.assign({}, bigLabelStyle, {
            bottom: bottom + 'px',
            left: x + 'px',
        });

        const key = details.stage.id + '-big';

        const classNames = ['pipeline-big-label'];
        if (this.stageIsSelected(details.stage)
            || this.stageChildIsSelected(details.stage)) {
            classNames.push('selected');
        }

        return <TruncatingLabel className={classNames.join(' ')} style={style} key={key}>{details.text}</TruncatingLabel>;
    }

    renderSmallLabel(details: LabelInfo) {

        const {
            nodeSpacingH,
            nodeSpacingV,
            curveRadius,
            connectorStrokeWidth,
            nodeRadius,
            smallLabelOffsetV,
        } = this.state.layout;

        const smallLabelWidth = Math.floor(nodeSpacingH - (2 * curveRadius) - (2 * connectorStrokeWidth)); // Fit between lines
        const smallLabelHeight = Math.floor(nodeSpacingV - smallLabelOffsetV - nodeRadius - nodeStrokeWidth);
        const smallLabelOffsetH = Math.floor(smallLabelWidth * -0.5);

        // These are about layout more than appearance, so they should probably remain inline
        const smallLabelStyle = {
            position: 'absolute',
            width: smallLabelWidth,
            maxHeight: smallLabelHeight,
            textAlign: 'center',
        };

        const x = details.x + smallLabelOffsetH;
        const top = details.y + smallLabelOffsetV;

        const style = Object.assign({}, smallLabelStyle, {
            top: top,
            left: x,
        });

        if (details.text.indexOf('komp') !== -1) {
            console.log(JSON.stringify(style, null, 4));
        }

        const key = details.stage.id + '-small';

        const classNames = ['pipeline-small-label'];
        if (this.stageIsSelected(details.stage)) {
            classNames.push('selected');
        }

        return <TruncatingLabel className={classNames.join(' ')} style={style} key={key}>{details.text}</TruncatingLabel>;
    }

    renderCompositeConnection(connection: CompositeConnection, elements: SVGChildren) {
        const {
            sourceNodes,
            destinationNodes,
            skippedNodes,
        } = connection;

        if (skippedNodes.length === 0) {
            // Nothing too complicated, use the old connection drawing code
            this.renderBasicConnections(sourceNodes, destinationNodes, elements);
        } else {
            this.renderSkippingConnections(sourceNodes, destinationNodes, skippedNodes, elements)
        }
    }

    // Connections between columns without any skipping
    renderBasicConnections(sourceNodes: Array<NodeInfo>, destinationNodes: Array<NodeInfo>, elements: SVGChildren) {

        const { connectorStrokeWidth } = this.state.layout;

        // Stroke props common to straight / curved connections
        const connectorStroke = {
            className: 'pipeline-connector',
            strokeWidth: connectorStrokeWidth,
        };

        this.renderHorizontalConnection(sourceNodes[0], destinationNodes[0], connectorStroke, elements);

        // Collapse from previous node(s) to top column node
        for (const previousNode of sourceNodes.slice(1)) {
            this.renderBasicCurvedConnection(previousNode, destinationNodes[0], elements);
        }

        // Expand from top previous node to column node(s)
        for (const destNode of destinationNodes.slice(1)) {
            this.renderBasicCurvedConnection(sourceNodes[0], destNode, elements);
        }
    }

    // Renders a more complex connection, that "skips" one or more nodes
    renderSkippingConnections(sourceNodes: Array<NodeInfo>,
                              destinationNodes: Array<NodeInfo>,
                              skippedNodes: Array<NodeInfo>,
                              elements: SVGChildren) {

        const { connectorStrokeWidth, nodeRadius, curveRadius, nodeSpacingH, nodeSpacingV } = this.state.layout;

        // Stroke props common to straight / curved connections
        const connectorStroke = {
            className: 'pipeline-connector',
            strokeWidth: connectorStrokeWidth,
        };

        // TODO: this should be a style
        const skipConnectorStroke = {
            className: 'pipeline-connector',
            strokeOpacity: 0.25,
            strokeWidth: connectorStrokeWidth,
        };

        const lastSkippedNode = skippedNodes[skippedNodes.length - 1];
        let leftNode, rightNode;

        //--------------------------------------------------------------------------
        //  Draw the "ghost" connections to/from/between skipped nodes

        leftNode = sourceNodes[0];
        for (rightNode of skippedNodes) {
            this.renderHorizontalConnection(leftNode, rightNode, skipConnectorStroke, elements);
            leftNode = rightNode;
        }
        this.renderHorizontalConnection(leftNode, destinationNodes[0], skipConnectorStroke, elements);

        //--------------------------------------------------------------------------
        //  "Collapse" from the source node(s) down toward the first skipped

        leftNode = sourceNodes[0];
        rightNode = skippedNodes[0];

        let midPointX = Math.round((leftNode.x + rightNode.x) / 2);

        for (leftNode of sourceNodes.slice(1)) {
            const key = connectorKey(leftNode, rightNode);

            let x1 = leftNode.x + nodeRadius - (nodeStrokeWidth / 2);
            let y1 = leftNode.y;
            let x2 = midPointX;
            let y2 = rightNode.y;

            const pathData = `M ${x1} ${y1}` + this.svgCurve(x1, y1, x2, y2, midPointX, curveRadius);

            elements.push(
                <path {...connectorStroke} key={key} d={pathData} fill="none" />,
            );
        }

        //--------------------------------------------------------------------------
        //  "Expand" from the last skipped node toward the destination nodes

        leftNode = lastSkippedNode;
        rightNode = destinationNodes[0];

        midPointX = Math.round((leftNode.x + rightNode.x) / 2);

        for (rightNode of destinationNodes.slice(1)) {
            const key = connectorKey(leftNode, rightNode);

            let x1 = midPointX;
            let y1 = leftNode.y;
            let x2 = rightNode.x - nodeRadius + (nodeStrokeWidth / 2);
            let y2 = rightNode.y;

            const pathData = `M ${x1} ${y1}` + this.svgCurve(x1, y1, x2, y2, midPointX, curveRadius);

            elements.push(
                <path {...connectorStroke} key={key} d={pathData} fill="none" />,
            );
        }

        //--------------------------------------------------------------------------
        //  "Main" curve from top of source nodes, around skipped nodes, to top of dest nodes

        leftNode = sourceNodes[0];
        rightNode = destinationNodes[0];
        const key = connectorKey(leftNode, rightNode);

        const skipHeight = nodeSpacingV * 0.5;
        const controlOffsetUpper = curveRadius * 1.6;
        const controlOffsetLower = skipHeight * 1.5;

        // Start point
        let x1 = leftNode.x + nodeRadius - (nodeStrokeWidth / 2);
        let y1 = leftNode.y;

        // Begin curve down point
        let x2 = Math.round((leftNode.x + skippedNodes[0].x) / 2);
        let y2 = y1;
        let cx1 = x2 + controlOffsetUpper;
        let cy1 = y2 + skipHeight * 0.1;

        // End curve down point
        let x3 = skippedNodes[0].x;
        let y3 = y1 + skipHeight;
        let cx2 = x3 - controlOffsetLower;
        let cy2 = y3;

        // Begin curve up point
        let x4 = lastSkippedNode.x;
        let y4 = y3;
        let cx3 = x4 + controlOffsetLower;
        let cy3 = y4;

        // End curve up point
        let x5 = Math.round((lastSkippedNode.x + rightNode.x) / 2);
        let y5 = rightNode.y;
        let cx4 = x5 - controlOffsetUpper;
        let cy4 = y5 + skipHeight * 0.1;

        // End point
        let x6 = rightNode.x - nodeRadius + (nodeStrokeWidth / 2);
        let y6 = rightNode.y;

        // if (x4 - x3 > controlOffsetLower) {
        //     // We've got a pretty wide skip, so let's maybe make it gentler
        //     x3 += 20;
        //     x4 -= 20;
        // }

        // elements.push(debugPoint(x1, y1, 'p1')); // TODO: RM
        // elements.push(debugPoint(x2, y2, 'p2')); // TODO: RM
        // elements.push(debugPoint(x3, y3, 'p3')); // TODO: RM
        // elements.push(debugPoint(x4, y4, 'p4')); // TODO: RM

        const pathData =
            `M ${x1} ${y1}` +
            `L ${x2} ${y2}` + // 1st horizontal
            `C ${cx1} ${cy1} ${cx2} ${cy2} ${x3} ${y3}` + // Curve down
            `L ${x4} ${y4}` + // 2nd horizontal
            `C ${cx3} ${cy3} ${cx4} ${cy4}  ${x5} ${y5}` + // TODO: Curve up
            `L ${x6} ${y6}` + // Last horizontal
            '';

        elements.push(<path {...connectorStroke} key={key} d={pathData} fill="none" />);
    }

    renderHorizontalConnection(leftNode: NodeInfo, rightNode: NodeInfo, connectorStroke, elements: SVGChildren) {

        const { nodeRadius } = this.state.layout;

        const key = connectorKey(leftNode, rightNode);

        const x1 = leftNode.x + nodeRadius - (nodeStrokeWidth / 2);
        const x2 = rightNode.x - nodeRadius + (nodeStrokeWidth / 2);
        const y = leftNode.y;

        elements.push(
            <line {...connectorStroke}
                  key={key}
                  x1={x1}
                  y1={y}
                  x2={x2}
                  y2={y}
            />,
        );
    }

    renderBasicCurvedConnection(leftNode: NodeInfo, rightNode: NodeInfo, elements: SVGChildren) {
        const { nodeRadius, curveRadius, connectorStrokeWidth } = this.state.layout;

        const key = connectorKey(leftNode, rightNode);

        const leftPos = {
            x: leftNode.x + nodeRadius - (nodeStrokeWidth / 2),
            y: leftNode.y,
        };

        const rightPos = {
            x: rightNode.x - nodeRadius + (nodeStrokeWidth / 2),
            y: rightNode.y,
        };

        // Stroke props common to straight / curved connections
        const connectorStroke = {
            className: 'pipeline-connector',
            strokeWidth: connectorStrokeWidth,
        };

        const midPointX = Math.round((leftPos.x + rightPos.x) / 2);

        const pathData = `M ${leftPos.x} ${leftPos.y}` +
            this.svgCurve(leftPos.x, leftPos.y, rightPos.x, rightPos.y, midPointX, curveRadius);

        // elements.push(debugPoint(midPointX, leftPos.y)); // TODO: RM

        elements.push(
            <path {...connectorStroke} key={key} d={pathData} fill="none" />,
        );
    }

    svgCurve(x1, y1, x2, y2, midPointX, curveRadius) {
        const verticalDirection = Math.sign(y2 - y1); // 1 == curve down, -1 == curve up
        const w1 = midPointX - curveRadius - x1 + (curveRadius * verticalDirection);
        const w2 = x2 - curveRadius - midPointX - (curveRadius * verticalDirection);
        const v = y2 - y1 - (2 * curveRadius * verticalDirection); // Will be -ive if curve up
        const cv = verticalDirection * curveRadius;

        return (
            ` l ${w1} 0` // first horizontal line
            + ` c ${curveRadius} 0 ${curveRadius} ${cv} ${curveRadius} ${cv}`  // turn
            + ` l 0 ${v}` // vertical line
            + ` c 0 ${cv} ${curveRadius} ${cv} ${curveRadius} ${cv}` // turn again
            + ` l ${w2} 0` // second horizontal line
        );
    }

    renderNode(node: NodeInfo, elements: SVGChildren) {

        const nodeIsSelected = this.stageIsSelected(node.stage);
        const { nodeRadius, connectorStrokeWidth } = this.state.layout;
        // Use a bigger radius for invisible click/touch target
        const mouseTargetRadius = nodeRadius + (2 * connectorStrokeWidth);

        const resultClean = decodeResultValue(node.state);
        const key = 'n_' + node.name + node.id;

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
                    onClick={() => this.nodeClicked(node)} />,
        );

        // All the nodes are in shared code, so they're rendered at 0,0 so we transform within a <g>
        const groupProps = {
            key,
            transform: `translate(${node.x},${node.y})`,
            className: nodeIsSelected ? 'pipeline-node-selected' : 'pipeline-node',
        };

        elements.push(React.createElement('g', groupProps, ...groupChildren));
    }

    renderSelectionHighlight(elements: SVGChildren) {

        const { nodeRadius, connectorStrokeWidth } = this.state.layout;
        const highlightRadius = nodeRadius + (0.49 * connectorStrokeWidth);
        let selectedNode = null;

        for (const node of this.state.nodes) {
            if (this.stageIsSelected(node.stage)) {
                selectedNode = node;
                break;
            }
        }

        if (selectedNode) {
            const transform = `translate(${selectedNode.x} ${selectedNode.y})`;

            elements.push(
                <g className="pipeline-selection-highlight" transform={transform} key="selection-highlight">
                    <circle r={highlightRadius} strokeWidth={connectorStrokeWidth * 1.1} />
                </g>,
            );
        }
    }

    // Put in a function so we can make improvements / multi-select
    stageIsSelected(stage: StageInfo) {
        const { selectedStage } = this.state;

        return selectedStage && selectedStage === stage;
    }

    stageChildIsSelected(stage: StageInfo) {
        const { children } = stage;
        const { selectedStage } = this.state;

        if (children && selectedStage) {
            for (const child of children) {
                if (child === selectedStage) {
                    return true;
                }
            }
        }
        return false;
    }

    nodeClicked(node: NodeInfo) {
        const stage = node.stage;
        const listener = this.props.onNodeClick;

        if (listener) {
            listener(stage.name, stage.id);
        }

        // Update selection
        this.setState({ selectedStage: stage });
    }

    render() {
        const {
            nodes = [],
            connections = [],
            bigLabels = [],
            smallLabels = [],
            measuredWidth,
            measuredHeight,
        } = this.state;

        // These are about layout more than appearance, so they should probably remain inline
        const outerDivStyle = {
            position: 'relative', // So we can put the labels where we need them
            overflow: 'visible' // So long labels can escape this component in layout
        };

        const visualElements = []; // Buffer for children of the SVG

        this.renderSelectionHighlight(visualElements);

        connections.forEach(connection => {
            this.renderCompositeConnection(connection, visualElements);
        });

        nodes.forEach(node => {
            this.renderNode(node, visualElements);
        });

        return (
            <div style={outerDivStyle}>
                <svg width={measuredWidth} height={measuredHeight}>
                    {visualElements}
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
    onNodeClick: PropTypes.func,
};

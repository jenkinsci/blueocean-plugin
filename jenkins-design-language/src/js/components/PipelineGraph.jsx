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

// type NodeInfo = {
//     x: number,
//     y: number,
//     name: string,
//     state: Result,
//     completePercent: number,
//     id: number,
//     stage?: StageInfo
// }; // TODO: RM

type StageNodeInfo = {
    // -- Shared with PlaceholderNodeInfo
    key: string,
    x: number,
    y: number,
    id: number,

    // -- Marker
    isPlaceholder: false,

    // -- Unique
    name: string,
    stage: StageInfo
};

type PlaceholderNodeInfo = {
    // -- Shared with StageNodeInfo
    key: string,
    x: number,
    y: number,
    id: number,

    // -- Marker
    isPlaceholder: true,

    // -- Unique
    type: 'start' | 'end'
}

// TODO: Attempt to extract a "common" node type with intersection operator to remove duplication

type NodeInfo = StageNodeInfo | PlaceholderNodeInfo;

type NodeColumn = {
    topStage?: StageInfo,
    nodes: Array<NodeInfo>,
}

type NodeColumns = Array<NodeColumn>;  // TODO: Remove this, just use Array<NodeColumn>

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
    return 'c_' + leftNode.key + '_to_' + rightNode.key;
}

// For Debugging  TODO: REMOVE THIS
let _debugKey = 111;
function debugPoint(cx, cy, message = 'debug', fill = 'rgba(255,0,200,0.5)') {
    const key = 'debug_' + (++_debugKey);

    return (
        <circle cx={cx} cy={cy} r="3" fill={fill} key={key} title={message} />
    );
}
function debugPointX(cx, cy, message = 'debug', stroke = 'rgba(0,255,128,0.5)') {
    const key = 'debug_' + (++_debugKey);

    return (
        <g key={key} title={message} stroke={stroke} strokeWidth={1}>
            <line x1={cx - 5} y1={cy - 5} x2={cx + 5} y2={cy + 5} />
            <line x1={cx - 5} y1={cy + 5} x2={cx + 5} y2={cy - 5} />
        </g>
    );
}

export class PipelineGraph extends Component {

    // Flow typedefs
    state: {
        nodes: Array<NodeInfo>,
        connections: Array<CompositeConnection>,
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

        const stageNodeColumns = this.createNodeColumns(newStages);

        const startNode = { // TODO: Can we get away with fewer properties for these?
            x: 0,
            y: 0,
            name: 'Start',
            id: -1,
            isPlaceholder: true,
            key: 'start-node',
            type: 'start',
        };

        const endNode = {  // TODO: Can we get away with fewer properties for these?
            x: 0,
            y: 0,
            name: 'End',
            id: -2,
            isPlaceholder: true,
            key: 'end-node',
            type: 'end',
        };

        const nodeColumns = [{ nodes: [startNode] }, ...stageNodeColumns, { nodes: [endNode] }];

        this.positionNodes(nodeColumns);


        // TODO: Big Labels
        // TODO: Small Labels
        // TODO: Create connections

        return this.stagesUpdatedOLD(newStages);
    }

    /**
     * Generate an array of columns, each being an array of NodeInfo, based on the top-level stages
     */
    createNodeColumns(topLevelStages: Array<StageInfo> = []): Array<NodeColumn> {

        const nodeColumns = [];

        for (const topStage of topLevelStages) {
            // If stage has children, we don't draw a node for it, just its children
            const stagesForColumn =
                topStage.children && topStage.children.length ? topStage.children : [topStage];

            nodeColumns.push({
                topStage,
                nodes: stagesForColumn.map(nodeStage => ({
                    x: 0,
                    y: 0,
                    name: nodeStage.name,
                    id: nodeStage.id,
                    stage: nodeStage,
                    isPlaceholder: false,
                    key: 'n_' + nodeStage.id
                }))
            });
        }

        return nodeColumns;
    }

    /**
     * Walks the columns of nodes giving them x and y positions
     */
    positionNodes(nodeColumns: NodeColumns) {

        const { nodeSpacingH, nodeSpacingV } = this.state.layout;

        let xp = nodeSpacingH / 2;

        for (let column of nodeColumns) {
            let yp = ypStart;

            for (let node of column.nodes) {
                node.x = xp;
                node.y = yp;

                yp += nodeSpacingV;
            }

            xp += nodeSpacingV;
        }
    }

    stagesUpdatedOLD(newStages: Array<StageInfo> = []) {    // TODO: Remove this once it's replicated nicely

        const { nodeSpacingH, nodeSpacingV } = this.state.layout;

        // The structures we're populating in this method
        const nodes: Array<NodeInfo> = [];
        const connections: Array<CompositeConnection> = [];
        const bigLabels: Array<LabelInfo> = [];
        const smallLabels: Array<LabelInfo> = [];

        // Location of next node to create
        let xp = nodeSpacingH / 2;
        let yp = ypStart;

        // Other state we need to maintain across columns
        let connectionSourceNodes: Array<NodeInfo> = []; // Column for last non-skipped stage, for connection generation
        let mostColumnNodes = 0; // "Tallest" column, for size calculation
        let skippedNodes = []; // Rendered nodes that need to be skipped by the next set of connections

        let placeholderId = -1;
        const placeholderStage = (name) => ({
            children: [],
            completePercent: 100,
            id: (--placeholderId),
            name: name,
            state: 'unknown',
            title: name,
        });

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
                    continue;
                }

                const node:StageNodeInfo = {
                    x: xp,
                    y: yp,
                    name: nodeStage.name,
                    // state: nodeStage.state,
                    // completePercent: nodeStage.completePercent,
                    id: nodeStage.id,
                    key: 'n_' + nodeStage.id,
                    stage: nodeStage,
                    isPlaceholder: false
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

        const { connectorStrokeWidth, nodeRadius, curveRadius, nodeSpacingV } = this.state.layout;

        // Stroke props common to straight / curved connections
        const connectorStroke = {
            className: 'pipeline-connector',
            strokeWidth: connectorStrokeWidth,
        };

        const skipConnectorStroke = {
            className: 'pipeline-connector-skipped',
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
        const controlOffsetUpper = curveRadius * 1.54;
        const controlOffsetLower = skipHeight * 0.257;
        const controlOffsetMid = skipHeight * 0.2;
        const inflectiontOffset = Math.round(skipHeight * 0.7071); // cos(45º)-ish

        // Start point
        let p1x = leftNode.x + nodeRadius - (nodeStrokeWidth / 2);
        let p1y = leftNode.y;

        // Begin curve down point
        let p2x = Math.round((leftNode.x + skippedNodes[0].x) / 2);
        let p2y = p1y;
        let c1x = p2x + controlOffsetUpper;
        let c1y = p2y;

        // End curve down point
        let p4x = skippedNodes[0].x;
        let p4y = p1y + skipHeight;
        let c4x = p4x - controlOffsetLower;
        let c4y = p4y;

        // Curve down midpoint / inflection
        let p3x = skippedNodes[0].x - inflectiontOffset;
        let p3y = skippedNodes[0].y + inflectiontOffset;
        let c2x = p3x - controlOffsetMid;
        let c2y = p3y - controlOffsetMid;
        let c3x = p3x + controlOffsetMid;
        let c3y = p3y + controlOffsetMid;

        // Begin curve up point
        let p5x = lastSkippedNode.x;
        let p5y = p4y;
        let c5x = p5x + controlOffsetLower;
        let c5y = p5y;

        // End curve up point
        let p7x = Math.round((lastSkippedNode.x + rightNode.x) / 2);
        let p7y = rightNode.y;
        let c8x = p7x - controlOffsetUpper;
        let c8y = p7y;

        // Curve up midpoint / inflection
        let p6x = lastSkippedNode.x + inflectiontOffset;
        let p6y = lastSkippedNode.y + inflectiontOffset;
        let c6x = p6x - controlOffsetMid;
        let c6y = p6y + controlOffsetMid;
        let c7x = p6x + controlOffsetMid;
        let c7y = p6y - controlOffsetMid;

        // End point
        let p8x = rightNode.x - nodeRadius + (nodeStrokeWidth / 2);
        let p8y = rightNode.y;


        const pathData =
            `M ${p1x} ${p1y}` +
            `L ${p2x} ${p2y}` + // 1st horizontal
            `C ${c1x} ${c1y} ${c2x} ${c2y} ${p3x} ${p3y}` + // Curve down (upper)
            `C ${c3x} ${c3y} ${c4x} ${c4y} ${p4x} ${p4y}` + // Curve down (lower)
            `L ${p5x} ${p5y}` + // 2nd horizontal
            `C ${c5x} ${c5y} ${c6x} ${c6y} ${p6x} ${p6y}` + // Curve up (lower)
            `C ${c7x} ${c7y} ${c8x} ${c8y} ${p7x} ${p7y}` + // Curve up (upper)
            `L ${p8x} ${p8y}` + // Last horizontal
            '';

        elements.push(<path {...connectorStroke} key={key} d={pathData} fill="none" />);

        // elements.push(debugPoint(c1x, c1y, 'c1')); // TODO: RM
        // elements.push(debugPoint(c2x, c2y, 'c2')); // TODO: RM
        // elements.push(debugPoint(c3x, c3y, 'c3')); // TODO: RM
        // elements.push(debugPoint(c4x, c4y, 'c4')); // TODO: RM

        // elements.push(debugPointX(p2x, p2y, 'p2', 'rgba(0,128,40,0.5')); // TODO: RM
        // elements.push(debugPointX(p3x, p3y, 'p3', 'rgba(0,128,40,0.5')); // TODO: RM
        // elements.push(debugPointX(p4x, p4y, 'p4', 'rgba(0,128,40,0.65')); // TODO: RM
    }

    renderHorizontalConnection(leftNode: NodeInfo, rightNode: NodeInfo, connectorStroke: Object, elements: SVGChildren) {

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

    svgCurve(x1: number, y1: number, x2: number, y2: number, midPointX: number, curveRadius: number) {
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

        let nodeIsSelected = false;
        const { nodeRadius, connectorStrokeWidth } = this.state.layout;
        // Use a bigger radius for invisible click/touch target
        const mouseTargetRadius = nodeRadius + (2 * connectorStrokeWidth);

        const key = node.key;

        let groupChildren = [];

        if (node.isPlaceholder === true) {
            // TODO: render placeholder dot
            groupChildren.push(getGroupForResult('unknown', 0, nodeRadius));
        } else {
            const completePercent = node.stage.completePercent || 0;
            const title = node.stage.title;
            const resultClean = decodeResultValue(node.stage.state);
            // TODO: Clean these lines up into destructure ^^^

            groupChildren.push(getGroupForResult(resultClean, completePercent, nodeRadius));

            if (title) {
                groupChildren.push(<title>{ title }</title>);
            }

            nodeIsSelected = this.stageIsSelected(node.stage);
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
            if (node.isPlaceholder === false && this.stageIsSelected(node.stage)) {
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
        if (node.isPlaceholder === false) {
            const stage = node.stage;
            const listener = this.props.onNodeClick;

            if (listener) {
                listener(stage.name, stage.id);
            }

            // Update selection
            this.setState({ selectedStage: stage });
        }
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

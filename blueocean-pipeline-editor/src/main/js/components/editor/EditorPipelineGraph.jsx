// @flow

import React, { Component, PropTypes } from 'react';

import {getAddIconGroup} from './common';
import type { StageInfo } from '../../services/PipelineStore';
import pipelineValidator from '../../services/PipelineValidator';

// Dimensions used for layout, px
export const defaultLayout = {
    nodeSpacingH: 120,
    nodeSpacingV: 70,
    nodeRadius: 12.5,
    innerDotRadius: 9.3,
    placeholderRadius: 11,
    startRadius: 7.5,
    curveRadius: 12,
    connectorStrokeWidth: 3.2,
    addStrokeWidth: 1.7,
    labelOffsetV: 25,
    smallLabelOffsetV: 20
};

// Typedefs

type StageNodeInfo = {
    // -- Shared with PlaceholderNodeInfo
    key: string,
    x: number,
    y: number,
    nodeId: number,
    parentStage?: StageInfo,

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
    nodeId: number,
    parentStage?: StageInfo,

    // -- Marker
    isPlaceholder: true,

    // -- Unique
    type: "start" | "add"
}

// TODO: Attempt to extract a "common" node type with intersection operator to remove duplication

type NodeInfo = StageNodeInfo | PlaceholderNodeInfo;

type ConnectionInfo = [NodeInfo, NodeInfo];

type LabelInfo = {
    //key: string,
    x: number,
    y: number,
    text: string,
    node: NodeInfo,
    stage?: StageInfo
};

type LayoutInfo = typeof defaultLayout;

type Props = {
    stages: Array<StageInfo>,
    layout?: Object,
    onStageSelected?: (stage:?StageInfo) => void,
    onCreateStage?: (parentStage:StageInfo) => void,
    selectedStage?: ?StageInfo
};

type State = {
    nodes: Array<NodeInfo>,
    connections: Array<ConnectionInfo>,
    bigLabels: Array<LabelInfo>,
    smallLabels: Array<LabelInfo>,
    measuredWidth: number,
    measuredHeight: number,
    layout: LayoutInfo
};

function nodeHasErrors(graphNode) {
    return graphNode.stage && pipelineValidator.hasValidationErrors(graphNode.stage);
}

type DefaultProps = typeof EditorPipelineGraph.defaultProps;
export class EditorPipelineGraph extends Component<DefaultProps, Props, State> {

    static defaultProps = {};

    //static propTypes = {...}
    // TODO: React proptypes ^^^

    startNode:PlaceholderNodeInfo;
    state:State;

    // Not stored in State because we need to set/read them synchronously
    selectedNode:?NodeInfo = null;
    selectedStage:?StageInfo = null;

    constructor(props:Props) {
        super(props);
        this.state = {
            nodes: [],
            connections: [],
            bigLabels: [],
            smallLabels: [],
            measuredWidth: 0,
            measuredHeight: 0,
            layout: Object.assign({}, defaultLayout, props.layout)
        };
    }

    componentWillMount() {
        this.handleProps(this.props, {stages: []});
    }

    componentWillReceiveProps(nextProps:Props) {
        this.handleProps(nextProps, this.props);
    }

    handleProps(nextProps:Props, oldProps:Props) {

        let newState = null; // null == no new state
        let needsLayout = false;

        if (nextProps.layout != oldProps.layout) {
            newState = {...newState, layout: Object.assign({}, defaultLayout, oldProps.layout)};
            needsLayout = true;
        }

        if (nextProps.stages !== oldProps.stages) {
            needsLayout = true;
        }
needsLayout = true;
        if (nextProps.selectedStage !== oldProps.selectedStage) {

            this.selectedStage = nextProps.selectedStage;

            if (!needsLayout) {
                // We don't need to re-generate the children but we need to find the right existing node
                let selectedNode = this.selectedNode;

                for (let node of this.state.nodes) {
                    if (node.isPlaceholder === false && node.stage === nextProps.selectedStage) {
                        selectedNode = node;
                        break;
                    }
                }

                this.selectedNode = selectedNode;
            }
        }

        const doLayoutIfNeeded = () => {
            if (needsLayout && nextProps.stages) {
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

    addConnectionDetails(connections:Array<ConnectionInfo>, previousNodes:Array<NodeInfo>, columnNodes:Array<NodeInfo>) {
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

    stagesUpdated(newStages:Array<StageInfo> = []) {

        // FIXME: Should we calculate based on expected text size guesstimate?
        const ypStart = 60;

        const { nodeSpacingH, nodeSpacingV, nodeRadius } = this.state.layout;

        // If we have a new layout, selectedNode will be invalid and we need to find it again
        const selectedStage = this.selectedStage;
        let selectedNode = null;

        var nodes:Array<NodeInfo> = [];
        var connections:Array<ConnectionInfo> = [];
        var bigLabels:Array<LabelInfo> = [];
        var smallLabels:Array<LabelInfo> = [];

        // next node position
        var xp = nodeSpacingH / 4;
        var yp = ypStart;

        var previousNodes:Array<NodeInfo> = [];
        var mostColumnNodes = 1;
        var placeholderId = -1;

        // 1. First we create a non-stage node for the "start" position
        const startNode:NodeInfo = this.startNode = {
            key: "s_" + placeholderId,
            x: xp,
            y: yp,
            name: "Start",
            nodeId: placeholderId,
            isPlaceholder: true,
            type: "start"
        };

        nodes.push(startNode);
        previousNodes.push(startNode);

        // 2. Give it a small label
        smallLabels.push({
            x: xp,
            y: yp,
            text: "Start",
            node: startNode
        });

        xp += nodeSpacingH; // Start node has its own column

        // 3. For reach top-level stage we have a column of node(s)
        for (const topStage of newStages) {

            yp = ypStart;

            // If stage has children, we don't draw a node for it, just its children
            const nodeStages = topStage.children && topStage.children.length ?
                topStage.children : [topStage];

            const columnNodes:Array<NodeInfo> = [];

            for (const nodeStage of nodeStages) {
                const nodeId = nodeStage.id;
                const node = {
                    key: "n_" + nodeStage.id,
                    x: xp,
                    y: yp,
                    name: nodeStage.name,
                    nodeId: nodeId,
                    stage: nodeStage,
                    isPlaceholder: false,
                    parentStage: topStage
                };

                if (nodeStage === selectedStage) {
                    selectedNode = node;
                }

                columnNodes.push(node);

                if (nodeStage != topStage) {
                    // Only separate child nodes need a smallLabel, as topStage already has a bigLabel
                    smallLabels.push({
                        x: xp,
                        y: yp,
                        text: nodeStage.name,
                        stage: nodeStage,
                        node
                    });
                }

                yp += nodeSpacingV;
            }

            // Always have a single bigLabel per top-level stage

            bigLabels.push({
                x: xp,
                y: ypStart,
                text: topStage.name,
                stage: topStage,
                node: columnNodes[0]
            });

            // Now add a placeholder for "add parallel stage" node.

            placeholderId--;
            const addStagePlaceholder:NodeInfo = {
                key: "a_" + placeholderId,
                x: xp,
                y: yp,
                name: "Add",
                nodeId: placeholderId,
                isPlaceholder: true,
                type: "add",
                parentStage: topStage
            };

            // Placeholder "add" doesn't go in "columnNodes" because we don't connect from it to the next column.
            nodes.push(addStagePlaceholder);
            yp += nodeSpacingV;

            // Add connections from last column to these new nodes

            if (previousNodes.length) {
                // add placeholders first to appear underneath
                this.addConnectionDetails(connections, [previousNodes[0]], [addStagePlaceholder]);
                // then add all other connections
                this.addConnectionDetails(connections, previousNodes, columnNodes);
            }

            xp += nodeSpacingH;
            mostColumnNodes = Math.max(mostColumnNodes, nodeStages.length + 1); // +1 for "add"
            nodes.push(...columnNodes);
            previousNodes = columnNodes;
        }

        // 4. Add a final "add" placeholder for new top-level stages

        placeholderId--;
        const addTopLevelStagePlaceholder:NodeInfo = {
            key: "a_" + placeholderId,
            x: xp,
            y: ypStart,
            name: "Add",
            nodeId: placeholderId,
            isPlaceholder: true,
            type: "add"
        };

        nodes.push(addTopLevelStagePlaceholder);
        xp += nodeSpacingH;

        if (previousNodes.length) {
            this.addConnectionDetails(connections, previousNodes.slice(0, 1), [addTopLevelStagePlaceholder]);
        }

        // 5. Calc dimensions
        var measuredWidth = xp - Math.floor(nodeSpacingH * 0.75);
        const measuredHeight = ypStart + ((mostColumnNodes-1) * nodeSpacingV) + (2 * nodeRadius);

        this.selectedNode = selectedNode;
        this.setState({
            nodes,
            connections,
            bigLabels,
            smallLabels,
            measuredWidth,
            measuredHeight
        });
    }

    renderBigLabel(details:LabelInfo) {

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

        const stage = details.stage;
        const key = (stage ? stage.id : details.text) + "-big"; // TODO: Replace with a key on LabelInfo

        const classNames = ["pipeline-big-label"];
        if (this.nodeIsSelected(details.node)
            || (stage && this.stageChildIsSelected(stage))) {
            classNames.push("selected");
        }
        if (nodeHasErrors(details.node)) {
            classNames.push("errors");
        }

        return <div className={classNames.join(" ")} style={style} key={key}>{details.text}</div>;
    }

    renderSmallLabel(details:LabelInfo) {

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

        const stage = details.stage;
        const key = (stage ? stage.id : details.text) + "-small"; // TODO: Replace with a key on LabelInfo

        const classNames = ["pipeline-small-label"];
        if (this.nodeIsSelected(details.node)) {
            classNames.push("selected");
        }
        if (nodeHasErrors(details.node)) {
            classNames.push("errors");
        }

        return <div className={classNames.join(" ")} style={style} key={key}>{details.text}</div>;
    }

    renderConnection(connection:ConnectionInfo) {

        const { nodeRadius, curveRadius, connectorStrokeWidth } = this.state.layout;

        const [leftNode, rightNode] = connection;
        const placeholderLine = leftNode.isPlaceholder || rightNode.isPlaceholder;
        const isConnectedToAdd = rightNode.type === 'add';
        const key = leftNode.key + "_con_" + rightNode.key;

        const leftPos = {
            x: leftNode.x,
            y: leftNode.y
        };

        const rightPos = {
            x: rightNode.x,
            y: rightNode.y
        };

        // Stroke props common to straight / curved connections
        let connectorStroke:any = {
            className: isConnectedToAdd ? "pipeline-connector placeholder" : "pipeline-connector",
            strokeWidth: connectorStrokeWidth
        };

        if (placeholderLine) {
            //connectorStroke.strokeDasharray = "5,2";
        }

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

    getSVGForNode(node:NodeInfo) {

        const {nodeRadius, startRadius, addStrokeWidth, innerDotRadius, placeholderRadius} = this.state.layout;
        const nodeIsSelected = this.nodeIsSelected(node);

        if (node.isPlaceholder === true) {
            if (node.type === "start") {
                return <circle r={startRadius} className="start-node" stroke="none"/>;
            }

            return getAddIconGroup(placeholderRadius, addStrokeWidth);
        }

        return (<g>
            <circle className="editor-graph-node" r={nodeRadius} />
            <circle className="editor-graph-node-inner" r={innerDotRadius} />
        </g>);
    }

    renderNode(node:NodeInfo) {

        const nodeIsSelected = this.nodeIsSelected(node);
        const { nodeRadius, connectorStrokeWidth } = this.state.layout;

        // Use a bigger radius for invisible click/touch target
        const mouseTargetRadius = nodeRadius + (2 * connectorStrokeWidth);

        const key = node.key;

        const completePercent = node.completePercent || 0;
        const groupChildren = [this.getSVGForNode(node)];

        const classNames = ["editor-graph-nodegroup"];
        if (nodeHasErrors(node)) {
            classNames.push("errors");
            const alertIcon =
                `<svg class="alerticon" width="20px" height="20px" viewBox="13 9 20 20" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                    <!-- Generator: Sketch 42 (36781) - http://www.bohemiancoding.com/sketch -->
                    <desc>Created with Sketch.</desc>
                    <defs>
                        <path d="M8.0197096,1.74273849 C8.56110904,0.780250597 9.44018119,0.782544345 9.9802904,1.74273849 L17.0197096,14.2572615 C17.561109,15.2197494 17.1073772,16 16.0049107,16 L1.99508929,16 C0.893231902,16 0.440181194,15.2174557 0.980290398,14.2572615 L8.0197096,1.74273849 Z" id="path-1"></path>
                        <mask id="mask-2" maskContentUnits="userSpaceOnUse" maskUnits="objectBoundingBox" x="0" y="0" width="20" height="20">
                            <rect x="0" y="0" width="20" height="20" fill="white"></rect>
                            <use xlink:href="#path-1" fill="black"></use>
                        </mask>
                        <rect id="path-3" x="8" y="6" width="2" height="4"></rect>
                        <mask id="mask-4" maskContentUnits="userSpaceOnUse" maskUnits="objectBoundingBox" x="0" y="0" width="2" height="4" fill="white">
                            <use xlink:href="#path-3"></use>
                        </mask>
                        <rect id="path-5" x="8" y="12" width="2" height="2"></rect>
                        <mask id="mask-6" maskContentUnits="userSpaceOnUse" maskUnits="objectBoundingBox" x="0" y="0" width="2" height="2" fill="white">
                            <use xlink:href="#path-5"></use>
                        </mask>
                    </defs>
                    <g id="Group-10" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd" transform="translate(15, 9)">
                        <g id="Triangle-2">
                            <use fill="#CE373A" fill-rule="evenodd" xlink:href="#path-1"></use>
                            <use stroke="#FFFFFF" mask="url(#mask-2)" stroke-width="2" xlink:href="#path-1"></use>
                        </g>
                        <use id="Rectangle-17" stroke="#FFFFFF" mask="url(#mask-4)" stroke-width="2" fill="#D8D8D8" xlink:href="#path-3"></use>
                        <use id="Rectangle-17-Copy" stroke="#FFFFFF" mask="url(#mask-6)" stroke-width="2" fill="#D8D8D8" xlink:href="#path-5"></use>
                    </g>
                </svg>`;
            groupChildren.push(<g transform="translate(1,-1)" dangerouslySetInnerHTML={{ __html: alertIcon }} />);
        }

        if (nodeIsSelected) {
            classNames.push("selected");
        }

        // Add an invisible click/touch target, coz the nodes are small and (more importantly)
        // many are hollow.
        groupChildren.push(
            <circle r={mouseTargetRadius}
                    cursor="pointer"
                    className="pipeline-node-hittarget"
                    fillOpacity="0"
                    stroke="none"
                    onClick={e => this.nodeClicked(node, e)}/>
        );

        // All the nodes are in shared code, so they're rendered at 0,0 so we transform within a <g>
        const groupProps = {
            key,
            transform: `translate(${node.x},${node.y})`,
            className: classNames.join(' '),
        };

        return React.createElement("g", groupProps, ...groupChildren);
    }

    nodeIsSelected(node:NodeInfo) {
        return this.selectedNode === node;
    }

    stageChildIsSelected(stage:StageInfo) {
        return this.selectedNode && this.selectedNode.parentStage === stage;
    }

    nodeClicked(node:NodeInfo, event) {
        event.stopPropagation();

        const {onStageSelected} = this.props;

        if (node.isPlaceholder === false) {
            const stage = node.stage;

            if (onStageSelected) {
                onStageSelected(stage);
            }

            // Update selection
            this.selectedNode = node;
            this.selectedStage = stage;
            this.forceUpdate();

        } else if (node.type === "start") {

            if (onStageSelected) {
                onStageSelected(null);
            }

            // Update selection
            this.selectedNode = node;
            this.selectedStage = null;
            this.forceUpdate();

        } else if (node.type === "add") {
            this.addStage(node.parentStage);
        }
    }

    addStage(parentStage:?StageInfo) {
        this.props.onCreateStage(parentStage);
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
            overflow: "visible", // So long labels can escape this component in layout,
            margin: '30px auto', // need to center here, justify-content: center cuts it off
            //height: (measuredHeight + 80) + "px"
        };

        return (
            <div style={outerDivStyle}>
                <svg className="editor-graph-svg"
                    width={measuredWidth} height={measuredHeight}>
                    {connections.map(conn => this.renderConnection(conn))}
                    {nodes.map(node => this.renderNode(node))}
                </svg>
                {bigLabels.map(label => this.renderBigLabel(label))}
                {smallLabels.map(label => this.renderSmallLabel(label))}
            </div>
        );
    }
}

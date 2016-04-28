import React, { Component, PropTypes } from 'react';
import {describeArcAsPath, polarToCartesian} from './SVG.jsx';

export const pipelineStageState = {
    // See: io.jenkins.blueocean.rest.model.BlueRun.BlueRunState
    queued: "queued", // May run in future
    running: "running",
    // See: io.jenkins.blueocean.rest.model.BlueRun.BlueRunResult
    success: "success",
    failure: "failure",
    notBuilt: "not_built" // Not run, and will not be
};

// Dimensions used for layout, px
const nodeSpacingH = 120;
const nodeSpacingV = 70;
const nodeRadius = 12;
const curveRadius = 12;
const connectorStrokeWidth = 4;

const labelWidth = nodeSpacingH;
const labelOffsetH = Math.floor(labelWidth * -0.5);
const labelOffsetV = 25;

const smallLabelWidth = nodeSpacingH - (2 * curveRadius); // Fit between lines
const smallLabelOffsetH = Math.floor(smallLabelWidth * -0.5);
const smallLabelOffsetV = 20;

const outlineNodeRadius = nodeRadius - (connectorStrokeWidth / 2);

const mouseTargetRadius = nodeRadius + (2 * connectorStrokeWidth); // Slightly bigger than visual nodes

// Colours. FIXME: Probably want to migrate these to stylesheets somehow
const connectorColor = "#979797";

const nodeColorSuccess = "#5BA504";
const nodeColorFailure = "#D54C53";
const nodeColorRunningTrack = "#a9c6e6";
const nodeColorRunningProgress = "#4a90e2";
const nodeColorNotBuilt = connectorColor;
const nodeColorUnexpected = "#ff00ff";

// These are about layout more than appearance, so they should probably remain inline
const bigLabelStyle = {
    position: "absolute",
    width: labelWidth,
    textAlign: "center",
    marginLeft: labelOffsetH,
    marginBottom: labelOffsetV
};

const smallLabelStyle = {
    position: "absolute",
    width: smallLabelWidth,
    textAlign: "center",
    fontSize: "80%",
    marginLeft: smallLabelOffsetH,
    marginTop: smallLabelOffsetV
};

const outerDivStyle = {
    position: "relative",
    overflow: "visible"
};

// Get the outgoing/rhs connection point from a node
function nodeConnectorFrom(fromNode) {
    return {
        x: fromNode.x + nodeRadius - (connectorStrokeWidth / 2),
        y: fromNode.y
    };
}

// Get the incoming/lhs connection point from a node
function nodeConnectorTo(toNode) {
    return {
        x: toNode.x - nodeRadius + (connectorStrokeWidth / 2),
        y: toNode.y
    };
}

export default class PipelineGraph extends Component {

    constructor(props) {
        super(props);
        this.state = {
            nodes: [],
            connections: [],
            bigLabels: [],
            smallLabels: [],
            measuredWidth: 0,
            measuredHeight: 0
        };
    }

    componentWillMount() {
        this.stagesUpdated(this.props.stages);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.stages !== this.props.stages) {
            this.stagesUpdated(nextProps.stages);
        }
    }

    addConnectionDetails(connections, previousNodes, columnNodes) {
        // Connect to top of previous/next column. Curves added when creating SVG

        // Collapse from previous node(s) to top column node
        for (let previousNode of previousNodes) {
            connections.push([previousNode, columnNodes[0]]);
        }

        // Expand from top previous node to column node(s) - first one done already above
        for (let columnNode of columnNodes.slice(1)) {
            connections.push([previousNodes[0], columnNode]);
        }
    }

    stagesUpdated(newStages = []) {
        var nodes = [];
        var connections = [];
        var bigLabels = [];
        var smallLabels = [];

        // FIXME: Should we calculate based on expected text size guesstimate?
        const ypStart = 50;

        // next node position
        var xp = nodeSpacingH / 2;
        var yp;

        var previousNodes = [];
        var mostColumnNodes = 0;

        // For reach top-level stage we have a column of node(s)
        for (let topStage of newStages) {

            yp = ypStart;

            // Always have a single bigLabel per top-level stage
            bigLabels.push({
                x: xp,
                y: yp,
                text: topStage.name
            });

            // If stage has children, we don't draw a node for it, just its children
            let nodeStages = topStage.children && topStage.children.length ?
                topStage.children : [topStage];

            let columnNodes = [];

            for (let nodeStage of nodeStages) {
                let node = {
                    x: xp,
                    y: yp,
                    name: nodeStage.name,
                    state: nodeStage.state,
                    completePercent: nodeStage.completePercent
                };

                columnNodes.push(node);

                // Only separate child nodes need a smallLabel, as topStage already has a bigLabel
                if (nodeStage != topStage) {
                    smallLabels.push({
                        x: xp,
                        y: yp,
                        text: nodeStage.name
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

    createBigLabel(details) {

        const x = details.x;
        const bottom = this.state.measuredHeight - details.y;

        const style = Object.assign({}, bigLabelStyle, {
            bottom: bottom + "px",
            left: x + "px"
        });

        return <div className="pipeline-big-label" style={style} key={details.text}>{details.text}</div>;
    }

    createSmallLabel(details) {

        const x = details.x;
        const top = details.y;

        const style = Object.assign({}, smallLabelStyle, {
            top: top,
            left: x
        });

        return <div className="pipeline-small-label" style={style} key={details.text}>{details.text}</div>;
    }

    renderConnection(connection) {
        const [leftNode,rightNode] = connection;
        const leftPos = nodeConnectorFrom(leftNode);
        const rightPos = nodeConnectorTo(rightNode);
        const key = leftNode.name + "_con_" + rightNode.name;

        // Stroke settings
        const connectorStroke = {
            stroke: connectorColor,
            strokeWidth: connectorStrokeWidth
        };

        if (leftPos.y == rightPos.y) {
            // Nice horizontal line
            return <line {...connectorStroke} key={key} x1={leftPos.x} y1={leftPos.y}
                                              x2={rightPos.x} y2={rightPos.y}/>;
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

    renderNode(node) {

        const state = node.state ? node.state.toLowerCase() : undefined;
        const key = "n_" + node.name;

        let arcDegrees = 90; // Default to a quarter-circle when spinning

        if (state == pipelineStageState.running) {
            arcDegrees = (3.6 * node.completePercent) || 0;
        }

        // Ugly node that looks like it doesn't belong, to highlight bad data :D
        let groupChildren = [<circle r={nodeRadius} fill={nodeColorUnexpected}/>];

        if (state == pipelineStageState.success) {
            // Solid green
            groupChildren = [<circle r={nodeRadius} fill={nodeColorSuccess}/>];
        }
        else if (state == pipelineStageState.failure) {
            // Solid red
            groupChildren = [<circle r={nodeRadius} fill={nodeColorFailure}/>];
        }
        else if (state == pipelineStageState.notBuilt) {
            // Grey outline
            groupChildren = [
                <circle r={outlineNodeRadius} fill="none" stroke={nodeColorNotBuilt} strokeWidth={connectorStrokeWidth}/>
            ];
        }
        else if (state == pipelineStageState.queued || state == pipelineStageState.running) {
            // Circular progress bar, spinning when "queued"
            let progressClassName = state == pipelineStageState.queued ? "spin" : undefined;

            groupChildren = [
                <circle r={outlineNodeRadius} fill="none" stroke={nodeColorRunningTrack} strokeWidth={connectorStrokeWidth}/>,
                <path className={progressClassName} fill="none" stroke={nodeColorRunningProgress} strokeWidth={connectorStrokeWidth}
                      d={describeArcAsPath(0, 0, outlineNodeRadius, 0, arcDegrees)}/>

            ];
        }

        // If somebody is listening for clicks, we'll add an invisible click/touch target
        // Because the nodes are small, and (more importantly) many are hollow.
        if (this.props.onNodeClick) {
            groupChildren.push(
                <circle r={mouseTargetRadius}
                        cursor="pointer"
                        fillOpacity="0"
                        stroke="none"
                        onClick={() => this.props.onNodeClick(node.name)}/>
            );
        }

        // NB: We draw all the nodes at 0,0 and transform within a <g> for consistency, but it's only strictly required
        // to make the spinning animations work.
        const groupProps = {
            key,
            transform: `translate(${node.x},${node.y})`
        };

        return React.createElement("g", groupProps, ...groupChildren);
    }

    render() {
        const {
            nodes = [],
            connections = [],
            bigLabels = [],
            smallLabels = [],
            measuredWidth,
            measuredHeight } = this.state;

        return (
            <div style={outerDivStyle}>
                <svg width={measuredWidth} height={measuredHeight}>
                    {connections.map(conn => this.renderConnection(conn))}
                    {nodes.map(node => this.renderNode(node))}
                </svg>
                {bigLabels.map(label => this.createBigLabel(label))}
                {smallLabels.map(label => this.createSmallLabel(label))}
            </div>
        );
    }
}

PipelineGraph.propTypes = {
    stages: PropTypes.array,
    onNodeClick: PropTypes.func
};
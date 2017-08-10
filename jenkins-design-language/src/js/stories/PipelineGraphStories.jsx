import React, {Component, PropTypes} from 'react';
import { storiesOf } from '@kadira/storybook';
import {PipelineGraph, defaultLayout} from '../components/PipelineGraph';

import { StatusIndicator } from '../components';
const validResultValues = StatusIndicator.validResultValues;

storiesOf('PipelineGraph', module)
    .add('Mixed', renderMultiParallelPipeline)
    .add('Long names', renderLongNames)
    .add('Duplicate Names', renderWithDuplicateNames)
    .add('Fat', renderFlatPipelineFat)
    .add('Legend', renderFlatPipeline)
    .add('Constructor', renderConstructomatic)
    .add('Listeners', renderListenersPipeline)
    .add('Parallel', renderParallelPipeline)
    .add('Parallel (Deep)', renderParallelPipelineDeep);

function renderFlatPipeline() {

    const stages = [
        makeNode("Success", [], validResultValues.success),
        makeNode("Failure", [], validResultValues.failure),
        makeNode("Running", [], validResultValues.running),
        makeNode("Slow", [], validResultValues.running, 150),
        makeNode("Queued", [], validResultValues.queued),
        makeNode("Unstable", [], validResultValues.unstable),
        makeNode("Aborted", [], validResultValues.aborted),
        makeNode("Not Built", [], validResultValues.not_built),
        makeNode("Bad data", [], "this is not my office")
    ];

    // Reduce spacing just to make this graph smaller
    const layout = { nodeSpacingH: 90 };

    return <div><PipelineGraph stages={stages} layout={layout}/></div>;
}

function renderWithDuplicateNames() {

    const stages = [
        makeNode("Build"),
        makeNode("Test"),
        makeNode("Browser Tests", [
            makeNode("Internet Explorer"),
            makeNode("Chrome")
        ]),
        makeNode("Test"),
        makeNode("Staging"),
        makeNode("Production")
    ];

    return (
        <div>
            <PipelineGraph stages={stages}/>
        </div>
    );
}

function renderFlatPipelineFat() {

    const stages = [
        makeNode("Success", [], validResultValues.success),
        makeNode("Failure", [], validResultValues.failure),
        makeNode("Running", [
            makeNode("Job 1", [], validResultValues.running),
            makeNode("Job 2", [], validResultValues.running),
            makeNode("Job 3", [], validResultValues.running)
        ]),
        makeNode("Queued", [
            makeNode("Job 4", [], validResultValues.queued),
            makeNode("Job 5", [], validResultValues.queued),
            makeNode("Job 6", [], validResultValues.queued),
            makeNode("Job 7", [], validResultValues.queued),
            makeNode("Job 8", [], validResultValues.queued)
        ]),
        makeNode("Not Built", [], validResultValues.not_built),
        makeNode("Bad data", [], "this is not my office")
    ];

    const layout = {
        connectorStrokeWidth: 10,
        nodeRadius: 20,
        curveRadius: 10,
    };

    return (
        <div style={{ padding: 10 }}>
            <h1>Same data, different layout</h1>
            <h3>Normal</h3>
            <PipelineGraph stages={stages}/>
            <h3>Fat</h3>
            <PipelineGraph stages={stages} layout={layout}/>
        </div>
    );
}


function renderConstructomatic() {

    const stages = [
        makeNode("Success", [], validResultValues.success),
        makeNode("Failure", [], validResultValues.failure),
        makeNode("Running", [
            makeNode("Job 1", [], validResultValues.running),
            makeNode("Job 2", [], validResultValues.running),
            makeNode("Job 3", [], validResultValues.running)
        ]),
        makeNode("Skipped", [], validResultValues.skipped),
        makeNode("Queued", [
            makeNode("Job 4", [], validResultValues.queued),
            makeNode("This is Job number 5", [], validResultValues.queued),
            makeNode("Job 6", [], validResultValues.queued),
            makeNode("Job 7", [], validResultValues.queued),
            makeNode("Job 8", [], validResultValues.queued)
        ]),
        makeNode("Not Built", [], validResultValues.not_built),
        makeNode("Bad data", [], "this is not my office")
    ];

    return <PipelineGraphConstructionKit stages={stages}/>;
}

function renderListenersPipeline() {

    const stages = [
        makeNode("Build", [], validResultValues.success),
        makeNode("Test", [], validResultValues.success),
        makeNode("Browser Tests", [
            makeNode("Internet Explorer", [], validResultValues.queued),
            makeNode("Chrome", [], validResultValues.queued)
        ]),
        makeNode("Dev"),
        makeNode("Dev"), // Make sure it works with dupe names
        makeNode("Staging"),
        makeNode("Production")
    ];

    function nodeClicked(...values) {
        console.log('Node clicked', values);
    }

    return <div><PipelineGraph stages={stages} onNodeClick={nodeClicked}/></div>;
}

function renderParallelPipeline() {

    const stages = [
        makeNode("Build"),
        makeNode("Test"),
        makeNode("Browser Tests", [
            makeNode("Internet Explorer"),
            makeNode("Chrome")
        ]),
        makeNode("Dev but with long label"),
        makeNode("Staging"),
        makeNode("Production")
    ];

    return (
        <div>
            <p>
                Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium
                consequatur corporis dolores, dolorum eius explicabo hic impedit
                laborum magni non qui quibusdam sapiente sed sit velit veritatis vero.
                Error, quibusdam!
            </p>
            <PipelineGraph stages={stages}/>
        </div>
    );
}

function renderMultiParallelPipeline() {

    const stages = [
        makeNode("Build", [], validResultValues.success),
        makeNode("Test", [
            makeNode("JUnit", [], validResultValues.success),
            makeNode("DBUnit", [], validResultValues.success),
            makeNode("Jasmine", [], validResultValues.success)
        ]),
        makeNode("Browser Tests", [
            makeNode("Firefox", [], validResultValues.success),
            makeNode("Edge", [], validResultValues.failure),
            makeNode("Safari", [], validResultValues.running, 60),
            makeNode("Chrome", [], validResultValues.running, 120)
        ]),
        makeNode("Skizzled", [], validResultValues.skipped),
        makeNode("Foshizzle", [], validResultValues.skipped),
        makeNode("Dev", [
            makeNode("US-East", [], validResultValues.success),
            makeNode("US-West", [], validResultValues.success),
            makeNode("APAC", [], validResultValues.success),
        ], validResultValues.success),
        makeNode("Staging", [], validResultValues.skipped),
        makeNode("Production")
    ];

    return <div><PipelineGraph stages={stages} selectedStage={stages[0]}/></div>;
}

function renderLongNames() {

    const stages = [
        makeNode("Build something with a long and descriptive name that takes up a shitload of space", [], validResultValues.success),
        makeNode("Test", [
            makeNode("JUnit", [], validResultValues.success),
            makeNode("DBUnit", [], validResultValues.success),
            makeNode("Jasmine", [], validResultValues.success)
        ]),
        makeNode("Browser Tests", [
            makeNode("Firefox", [], validResultValues.success),
            makeNode("Das komputermaschine ist nicht auf mittengraben unt die gerfingerpoken. Watchen das blinkenlights.", [], validResultValues.failure),
            makeNode("RubberbabybuggybumpersbetyoudidntknowIwasgoingtodothat", [], validResultValues.running, 60),
            makeNode("Chrome", [], validResultValues.running, 120)
        ]),
        makeNode("Dev"),
        makeNode("Staging"),
        makeNode("Production")
    ];

    return <div><PipelineGraph stages={stages} selectedStage={stages[0]}/></div>;
}

function renderParallelPipelineDeep() {

    const stages = [
        makeNode("Build", [], validResultValues.success),
        makeNode("Test", [], validResultValues.success),
        makeNode("Browser Tests", [
            makeNode("Internet Explorer", [], validResultValues.success),
            makeNode("Firefox", [], validResultValues.running),
            makeNode("Edge", [], validResultValues.failure),
            makeNode("Safari", [], validResultValues.running),
            makeNode("LOLpera", [], validResultValues.queued),
            makeNode("Chrome", [], validResultValues.queued)
        ]),
        makeNode("Dev", [], validResultValues.not_built),
        makeNode("Staging", [], validResultValues.not_built),
        makeNode("Production", [], validResultValues.not_built)
    ];

    return <div><PipelineGraph stages={stages}/></div>;
}

let __id = 1;

/// Simple helper for data generation
function makeNode(name, children = [], state = validResultValues.not_built, completePercent) {
    completePercent = completePercent || ((state == validResultValues.running) ? Math.floor(Math.random() * 60 + 20) : 50);
    const id = __id++;
    return {name, children, state, completePercent, id};
}

/// Wrap a PipelineGraph with some controls to tweak the layout properties
class PipelineGraphConstructionKit extends Component {
    constructor(props) {
        super(props);
        this.state = {layout: defaultLayout};
    }

    control(label, property, min, max) {
        const value = this.state.layout[property];

        const changed = (e) => {
            const value = e.target.value;

            const layout = Object.assign({}, this.state.layout);
            layout[property] = parseInt(value);

            this.setState({layout});
        };

        return (
            <tr>
                <td>{label}</td>
                <td><input type="range" min={min} max={max} defaultValue={value} onChange={changed}/></td>
                <td>{value}</td>
            </tr>
        );
    }

    render() {

        const wrapperStyle = {
            margin: "5px",
            padding: "10px",
            border: "dashed 1px #ccc",
            borderRadius: "10px"
        };

        const tableStyle = {
            width: "auto",
            borderSpacing: "5px",
            borderCollapse: "separate"
        };

        const controlDivStyle = {
            display: "flex",
            justifyContent: "center"
        };

        return (
            <div style={wrapperStyle}>
                <h1>PipelineGraph Construct-o-matic&trade;</h1>
                <div style={controlDivStyle}>
                    <table style={tableStyle}>
                        <tbody>
                        {this.control("Line Thickness", "connectorStrokeWidth", 1, 20)}
                        {this.control("Curve Radius", "curveRadius", 0, 50)}
                        {this.control("H Spacing", "nodeSpacingH", 10, 200)}
                        {this.control("V Spacing", "nodeSpacingV", 10, 200)}
                        </tbody>
                    </table>
                    <table style={tableStyle}>
                        <tbody>
                        {this.control("Node radius", "nodeRadius", 5, 40)}
                        {this.control("Big Label offset", "labelOffsetV", 0, 100)}
                        {this.control("Small Label offset", "smallLabelOffsetV", 0, 100)}
                        </tbody>
                    </table>
                </div>
                <PipelineGraph stages={this.props.stages}
                               layout={this.state.layout}
                               onNodeClick={this.props.onNodeClick}/>
            </div>
        );
    }
}

PipelineGraphConstructionKit.propTypes = {
    stages: PropTypes.array,
    onNodeClick: PropTypes.func
};

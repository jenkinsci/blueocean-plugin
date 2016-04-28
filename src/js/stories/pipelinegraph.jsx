import React from 'react';
import { storiesOf } from '@kadira/storybook';
import PipelineGraph, {pipelineStageState} from '../components/PipelineGraph.jsx';

storiesOf('PipelineGraph', module)
    .add('Mixed', renderMultiParallelPipeline)
    .add('Legend', renderFlatPipeline)
    .add('Listeners', renderListenersPipeline)
    .add('Parallel', renderParallelPipeline)
    .add('Parallel (Deep)', renderParallelPipelineDeep);

function renderFlatPipeline() {

    const stages = [
        makeNode("Success", [], pipelineStageState.success),
        makeNode("Failure", [], pipelineStageState.failure),
        makeNode("Running", [], pipelineStageState.running),
        makeNode("Queued", [], pipelineStageState.queued),
        makeNode("Not Built", [], pipelineStageState.notBuilt),
        makeNode("Bad data", [], "this is not my office")
    ];

    return <div><PipelineGraph stages={stages}/></div>;
}

function renderListenersPipeline() {

    const stages = [
        makeNode("Build", [], pipelineStageState.success),
        makeNode("Test", [], pipelineStageState.success),
        makeNode("Browser Tests", [
            makeNode("Internet Explorer", [], pipelineStageState.queued),
            makeNode("Chrome", [], pipelineStageState.queued)
        ]),
        makeNode("Dev"),
        makeNode("Staging"),
        makeNode("Production")
    ];

    return <div><PipelineGraph stages={stages} onNodeClick={nodeName => console.log('Clicked', nodeName)}/></div>;
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
        makeNode("Build", [], pipelineStageState.success),
        makeNode("Test", [
            makeNode("JUnit", [], pipelineStageState.success),
            makeNode("DBUnit", [], pipelineStageState.success),
            makeNode("Jasmine", [], pipelineStageState.success)
        ]),
        makeNode("Browser Tests", [
            makeNode("Firefox", [], pipelineStageState.success),
            makeNode("Edge", [], pipelineStageState.failure),
            makeNode("Safari", [], pipelineStageState.running),
            makeNode("Chrome", [], pipelineStageState.queued)
        ]),
        makeNode("Dev"),
        makeNode("Staging"),
        makeNode("Production")
    ];

    return <div><PipelineGraph stages={stages}/></div>;
}

function renderParallelPipelineDeep() {

    const stages = [
        makeNode("Build", [], pipelineStageState.success),
        makeNode("Test", [], pipelineStageState.success),
        makeNode("Browser Tests", [
            makeNode("Internet Explorer", [], pipelineStageState.success),
            makeNode("Firefox", [], pipelineStageState.running),
            makeNode("Edge", [], pipelineStageState.failure),
            makeNode("Safari", [], pipelineStageState.running),
            makeNode("LOLpera", [], pipelineStageState.queued),
            makeNode("Chrome", [], pipelineStageState.queued)
        ]),
        makeNode("Dev", [], pipelineStageState.notBuilt),
        makeNode("Staging", [], pipelineStageState.notBuilt),
        makeNode("Production", [], pipelineStageState.notBuilt)
    ];

    return <div><PipelineGraph stages={stages}/></div>;
}

function makeNode(name, children = [], state = pipelineStageState.notBuilt) {
    const completePercent = (state == pipelineStageState.running) ? Math.floor(Math.random() * 60 + 20) : 50;
    return {name, children, state, completePercent};
}
import React, {Component} from 'react';
import {ExtensionPoint, store, actions} from '../../blue-ocean';

import withPipelines from './withPipelines'

function PiplineListHeader(props) {
    const count = props.pipelines ? props.pipelines.length : 0;
    return <h2>{count} Pipelines</h2>;
}

function renderHomepagePipeline(pipeline) {
    return <div key={pipeline.name}>
        <h3>{pipeline.name}</h3>
        <ExtensionPoint name="jenkins.pipeline.pipelineRow" pipeline={pipeline}/>
    </div>
}

class PipelinesPage extends Component {

    render() {
        const {pipelines = []} = this.props;

        return <article>
            <h1>Home</h1>
            <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ad architecto autem deleniti, dicta
                exercitationem explicabo facere harum hic inventore laborum magnam magni maiores molestias nemo
                recusandae rem saepe! Illo, perferendis?</p>

            <PiplineListHeader pipelines={pipelines}/>
            {pipelines.map(renderHomepagePipeline)}

            <input ref="newPipelineName" type="text" />
            <button onClick={() => this.addNewPipeline()}>Add Pipeline</button>
        </article>
    }

    addNewPipeline() {
        const newPipeline = {
            name: this.refs.newPipelineName.getDOMNode().value,
            status: "green"
        };
        this.props.dispatch({type:actions.ADD_PIPELINE, pipeline:newPipeline});
    }
}

export default withPipelines(PipelinesPage);
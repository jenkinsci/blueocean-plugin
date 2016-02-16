import React, {Component} from 'react';

/** My first extension */
export default class MyPipelineRowExtension extends Component {
    render() {
        return <div className={'pipelineStatus_'+this.props.pipeline.status}>
            {this.props.pipeline.status}
        </div>
    }
}

import React, {Component} from 'react';

import PipelineViewStore from '../stores/PipelineViewStore.js';

export default class TopNavPipelineCounts extends Component {
    componentWillMount() {
        PipelineViewStore.registerListener(function() {
            this.setState({});
        }.bind(this));
    }
    
    render() {
        return <div>
            Num Pipelines: {PipelineViewStore.getPipelines().length}
        </div>;
    }
}

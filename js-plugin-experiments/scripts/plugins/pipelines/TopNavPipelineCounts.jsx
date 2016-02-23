import React, {Component} from 'react';

import withPipelines from './withPipelines'

class TopNavPipelineCounts extends Component {

    render() {
        const {pipelines = []} = this.props;
        return <div>
            Num Pipelines: {pipelines.length}
        </div>;
    }
}

export default withPipelines(TopNavPipelineCounts);
import React, { Component, PropTypes } from 'react';
import { observer } from 'mobx-react';

import Extensions from '@jenkins-cd/js-extensions';

@observer
export class PipelineTrends extends Component {

    propTypes = {
        locale: PropTypes.string,
        t: PropTypes.func,
        pipeline: PropTypes.object,
        params: PropTypes.object,
    };

    contextTypes = {
        config: PropTypes.object.isRequired,
        params: PropTypes.object.isRequired,
        pipelineService: PropTypes.object.isRequired,
    };

    render() {
        return (
                    <h1>Trends</h1>
        );
    }
}

export default PipelineTrends;

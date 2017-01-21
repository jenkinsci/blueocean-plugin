import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubLoadingStep extends React.Component {

    render() {
        return (
            <FlowStep {...this.props} title="Loading..." />
        );
    }

}

GithubLoadingStep.propTypes = {
    flowManager: PropTypes.object,
};

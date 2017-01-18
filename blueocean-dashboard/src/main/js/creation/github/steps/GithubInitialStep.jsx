import React, { PropTypes } from 'react';
import FlowStep from '../../flow2/FlowStep';
import { observer } from 'mobx-react';

@observer
export default class GithubInitialStep extends React.Component {

    componentDidMount() {
        this.props.flowManager.findExistingCredential();
    }

    render() {
        return (
            <FlowStep {...this.props} title="Loading..." />
        );
    }

}

GithubInitialStep.propTypes = {
    flowManager: PropTypes.object,
};

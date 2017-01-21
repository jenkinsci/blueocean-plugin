import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubConfirmDiscoverStep extends React.Component {

    confirmDiscover() {
        this.props.flowManager.confirmDiscover();
    }

    render() {
        return (
            <FlowStep {...this.props} title="You sure about that buddy?">
                <div>
                    <button onClick={() => this.confirmDiscover()}>Create</button>
                </div>
            </FlowStep>
        );
    }

}

GithubConfirmDiscoverStep.propTypes = {
    flowManager: PropTypes.object,
};

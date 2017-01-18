import React, { PropTypes } from 'react';
import FlowStep from '../../flow2/FlowStep';
import { observer } from 'mobx-react';

@observer
export default class GithubConfirmDiscoverStep extends React.Component {

    confirmDiscover(discover) {
        this.props.flowManager.confirmDiscover(discover);
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

import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubUnknownErrorStep extends React.Component {

    render() {
        return (
            <FlowStep {...this.props} title="Unknown Error" error>
                <div className="instructions">
                    An unknown error has occurred. You may try again.
                </div>

                <p className="instructions">
                    Message: {this.props.message}
                </p>
            </FlowStep>
        );
    }

}

GithubUnknownErrorStep.propTypes = {
    flowManager: PropTypes.object,
    message: PropTypes.string,
};

import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class BbLoadingStep extends React.Component {

    render() {
        return (
            <FlowStep {...this.props} title="Loading..." loading scrollOnActive={false} />
        );
    }

}

BbLoadingStep.propTypes = {
    flowManager: PropTypes.object,
};

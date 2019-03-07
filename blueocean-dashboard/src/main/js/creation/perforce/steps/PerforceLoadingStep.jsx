import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class PerforceLoadingStep extends React.Component {
    render() {
        return <FlowStep {...this.props} title="P4 Loading..." loading scrollOnActive={false} />;
    }
}

PerforceLoadingStep.propTypes = {
    flowManager: PropTypes.object,
};

import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubInvalidOrgStep extends React.Component {

    render() {
        const { flowManager } = this.props;

        return (
            <FlowStep {...this.props} title="Error" error>
                <div>A top-level job named "{flowManager.selectedOrganization.name}" already exists. In order to proceed with creation,
                it must renamed or removed!</div>
            </FlowStep>
        );
    }

}

GithubInvalidOrgStep.propTypes = {
    flowManager: PropTypes.object,
};

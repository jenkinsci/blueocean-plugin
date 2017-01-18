import React, { PropTypes } from 'react';
import FlowStep from '../../flow2/FlowStep';
import { observer } from 'mobx-react';

@observer
export default class GithubChooseDiscoverStep extends React.Component {

    selectDiscover(discover) {
        this.props.flowManager.selectDiscover(discover);
    }

    render() {
        return (
            <FlowStep {...this.props} title="Do you want to create a Pipeline for one repository or automatically discover?">
                <div>
                    <button onClick={() => this.selectDiscover(false)}>Just One</button>
                </div>
                <div>
                    <button onClick={() => this.selectDiscover(true)}>Auto Discover!!</button>
                </div>
            </FlowStep>
        );
    }

}

GithubChooseDiscoverStep.propTypes = {
    flowManager: PropTypes.object,
};

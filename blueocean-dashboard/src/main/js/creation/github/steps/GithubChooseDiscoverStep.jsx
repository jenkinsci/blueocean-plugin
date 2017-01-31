import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubChooseDiscoverStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            discover: null,
        };
    }

    selectDiscover(discover) {
        this.setState({
            discover,
        });
        this.props.flowManager.selectDiscover(discover);
    }

    render() {
        // const { flowManager } = this.props;
        const title = 'Do you want to create a Pipeline for one repository or automatically discover?';
        // const existing = flowManager.existingAutoDiscover ? 'This organization is already set to "Automatically Discover."' : '';
        const option1Class = this.state.discover === false ? 'u-selected' : '';
        const option2Class = this.state.discover === true ? 'u-selected' : '';
        // const option2Class = this.state.discover === true || existing ? 'u-selected' : '';

        return (
            <FlowStep {...this.props} className="github-choose-discover-step" title={title}>
                { /* existing && <p className="instructions">{existing}</p> */ }

                <div className="toggle layout-large">
                    <button className={`monochrome ${option1Class}`} onClick={() => this.selectDiscover(false)}>
                        <h1 className="title">Just one repository</h1>
                        <p className="text">
                            Recommended if you haven't created a Pipeline before or do not
                            have any repositories containing a <em>Jenkinsfile</em>
                        </p>
                    </button>

                    <button className={`monochrome ${option2Class}`} onClick={() => this.selectDiscover(true)}>
                        <h1 className="title">Automatically discover</h1>
                        <p className="text">
                            Actively discovers new <em>Jenkinsfiles</em> in this organization's
                            repositories and creates Pipelines automatically.
                        </p>
                    </button>
                </div>
            </FlowStep>
        );
    }

}

GithubChooseDiscoverStep.propTypes = {
    flowManager: PropTypes.object,
};

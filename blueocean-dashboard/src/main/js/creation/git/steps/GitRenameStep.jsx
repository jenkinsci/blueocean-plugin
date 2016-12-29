import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import { TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';

/**
 * Shows the current progress after creation was initiated.
 */
@observer
export default class GitRenameStep extends React.Component {

    constructor(props) {
        super(props);

        this.title = 'Naming conflict';
        this.pipelineName = '';
    }

    _onChange(val) {
        this.pipelineName = val;
    }

    _onSave() {
        this.props.flowManager.saveRenamedPipeline(this.pipelineName);
    }

    render() {
        const conflictName = 'PLACEHOLDER';

        return (
            <FlowStep {...this.props} title={this.title}>
                <p>A pipeline with the same name "{conflictName}" already exists in the same folder. A unique name is required.</p>

                <div className="rename-container">
                    <TextInput
                      className="text-pipeline"
                      placeholder="Pipeline name"
                      onChange={val => this._onChange(val)}
                    />

                    <button className="button-save" onClick={() => this._onSave()}>Save</button>
                </div>

            </FlowStep>
        );
    }
}

GitRenameStep.propTypes = {
    flowManager: PropTypes.string,
};

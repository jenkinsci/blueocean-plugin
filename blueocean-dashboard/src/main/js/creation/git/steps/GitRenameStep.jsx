import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';

import { FormElement, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';

/**
 * Shows the current progress after creation was initiated.
 */
@observer
export default class GitRenameStep extends React.Component {

    constructor(props) {
        super(props);

        this.title = 'Naming conflict';

        this.state = {
            pipelineName: '',
            isNameValid: null,
        };
    }

    _onChange(name) {
        this._checkPipelineName(name);
    }

    _checkPipelineName = debounce((name) => {
        this.props.flowManager.checkPipelineNameAvailable(name)
            .then(available => this._validateName(name, available));
    }, 500);

    _validateName(pipelineName, available) {
        const isNameValid = !!pipelineName && available;
        this.setState({
            pipelineName,
            isNameValid,
        });
    }

    _onSave() {
        this.props.flowManager.saveRenamedPipeline(this.state.pipelineName);
    }

    render() {
        let headingText = '';

        if (this.state.isNameValid === null) {
            headingText = `${this.props.pipelineError}. A unique name is required.`;
        } else if (this.state.isNameValid === false) {
            headingText = `The name '${this.state.pipelineName}' is not available. Please try a different name.`;
        } else if (this.state.isNameValid === true) {
            headingText = `Success! '${this.state.pipelineName}' is available.`;
        }

        return (
            <FlowStep className="git-step-rename" {...this.props} title={this.title}>
                <FormElement title={headingText}>
                    <TextInput
                      className="text-pipeline"
                      placeholder="Pipeline name"
                      onChange={val => this._onChange(val)}
                    />

                    <button disabled={!this.state.isNameValid}
                      onClick={() => this._onSave()}
                    >
                        Save
                    </button>
                </FormElement>

            </FlowStep>
        );
    }
}

GitRenameStep.propTypes = {
    flowManager: PropTypes.string,
    pipelineError: PropTypes.string,
};

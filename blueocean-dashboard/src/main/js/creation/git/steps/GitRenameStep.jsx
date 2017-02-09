import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';

import { FormElement, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';

let t = null;

/**
 * Handling renaming when a name conflict occurs during creation.
 */
@observer
export default class GitRenameStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            pipelineName: '',
            isNameValid: null,
        };

        t = this.props.flowManager.translate;
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
        const disabled = !this.props.flowManager.isRenameEnabled;
        let headingText = '';

        if (this.state.isNameValid === null) {
            headingText = t('creation.git.step2.name_required', { 0: this.props.pipelineName });
        } else if (this.state.isNameValid === false) {
            headingText = t('creation.git.step2.name_unavailable', { 0: this.state.pipelineName });
        } else if (this.state.isNameValid === true) {
            headingText = t('creation.git.step2.name_available', { 0: this.state.pipelineName });
        }

        return (
            <FlowStep {...this.props} className="git-step-rename" title={t('creation.git.step2.title')} disabled={disabled}>
                <FormElement title={headingText}>
                    <TextInput
                      className="text-pipeline"
                      placeholder={t('creation.git.step2.text_name_placeholder')}
                      onChange={val => this._onChange(val)}
                    />

                    <button disabled={!this.state.isNameValid}
                      onClick={() => this._onSave()}
                    >
                        {t('creation.git.step2.button_save')}
                    </button>
                </FormElement>

            </FlowStep>
        );
    }
}

GitRenameStep.propTypes = {
    flowManager: PropTypes.string,
    pipelineName: PropTypes.string,
};

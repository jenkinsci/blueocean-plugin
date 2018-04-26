import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import Extensions from '@jenkins-cd/js-extensions';
import { FormElement, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../flow2/FlowStep';

import { CreatePipelineOutcome } from './GitCreationApi';
import STATE from './GitCreationState';

let t = null;

function validateUrl(url) {
    return !!url && !!url.trim();
}

export function isSshRepositoryUrl(url) {
    if (!validateUrl(url)) {
        return false;
    }

    if (/^ssh:\/\/.*/.test(url)) {
        return true;
    }

    if (/^[^@:]+@.*/.test(url)) {
        return true;
    }

    return false;
}

/**
 * Component that accepts repository URL and credentials to initiate
 * creation of a new pipeline.
 */
@observer
export default class GitConnectStep extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            repositoryUrl: null,
            repositoryErrorMsg: null,
            credentialErrorMsg: null,
            selectedCredential: null,
        };

        t = this.props.flowManager.translate;
    }

    componentWillMount() {
        this._selectedCredentialChange(this.props.flowManager.noCredentialsOption);
    }

    _repositoryUrlChange(value) {
        this.setState({
            repositoryUrl: value,
        });

        this._updateRepositoryErrorMsg();
    }

    _getRepositoryErrorMsg(outcome) {
        if (this.state.repositoryErrorMsg) {
            return this.state.repositoryErrorMsg;
        } else if (outcome === CreatePipelineOutcome.INVALID_URI) {
            return t('creation.git.step1.repo_error_invalid');
        }

        return null;
    }

    _updateRepositoryErrorMsg = debounce(() => {
        if (validateUrl(this.state.repositoryUrl)) {
            this.setState({
                repositoryErrorMsg: null,
            });
        }
    }, 200);

    _selectedCredentialChange(credential) {
        const oldId = this.state.selectedCredential && this.state.selectedCredential.id;
        const newId = credential && credential.id;
        if (oldId === newId) {
            return;
        }

        this.setState({
            selectedCredential: credential,
        });
    }

    _getCredentialErrorMsg(outcome) {
        if (this.state.credentialErrorMsg) {
            return this.state.credentialErrorMsg;
        } else if (outcome === CreatePipelineOutcome.INVALID_CREDENTIAL) {
            return t('creation.git.step1.credentials_error_invalid');
        }

        return null;
    }

    _onCreateCredentialClosed = credential => {
        this._selectedCredentialChange(credential || this.props.flowManager.noCredentialsOption);
    };

    _performValidation() {
        if (!validateUrl(this.state.repositoryUrl)) {
            this.setState({
                repositoryErrorMsg: t('creation.git.step1.repo_error_required'),
            });

            return false;
        }

        return true;
    }

    _beginCreation() {
        const isValid = this._performValidation();

        if (!isValid) {
            return;
        }

        this.props.flowManager.createPipeline(this.state.repositoryUrl, this.state.selectedCredential);
    }

    render() {
        const { flowManager } = this.props;
        const { repositoryUrl } = this.state;
        const repositoryErrorMsg = this._getRepositoryErrorMsg(flowManager.outcome);
        const credentialErrorMsg = this._getCredentialErrorMsg(flowManager.outcome);

        const disabled = flowManager.stateId !== STATE.STEP_CONNECT && flowManager.stateId !== STATE.COMPLETE;
        const createButtonLabel = !disabled ? t('creation.git.step1.create_button') : t('creation.git.step1.create_button_progress');

        return (
            <FlowStep {...this.props} className="git-step-connect" title={t('creation.git.step1.title')} disabled={disabled}>
                <p className="instructions">
                    {t('creation.git.step1.instructions')} &nbsp;
                    <a href="https://jenkins.io/doc/book/pipeline/jenkinsfile/" target="_blank">
                        {t('creation.git.step1.instructions_link')}
                    </a>
                </p>

                <FormElement title={t('creation.git.step1.repo_title')} errorMessage={repositoryErrorMsg}>
                    <TextInput className="text-repository-url" onChange={val => this._repositoryUrlChange(val)} />
                </FormElement>

                <Extensions.Renderer
                    extensionPoint="jenkins.credentials.selection"
                    className="credentials-selection-git"
                    onComplete={this._onCreateCredentialClosed}
                    type="git"
                    repositoryUrl={repositoryUrl}
                />

                {isSshRepositoryUrl(repositoryUrl) &&
                    credentialErrorMsg && <FormElement className="public-key-display" errorMessage={t('creation.git.step1.credentials_publickey_invalid')} />}

                <button className="button-create-pipeline" onClick={() => this._beginCreation()} disabled={!validateUrl(repositoryUrl)}>
                    {createButtonLabel}
                </button>
            </FlowStep>
        );
    }
}

GitConnectStep.propTypes = {
    flowManager: PropTypes.object,
};

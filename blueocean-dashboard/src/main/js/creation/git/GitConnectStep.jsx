import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import Extensions from '@jenkins-cd/js-extensions';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import { Dropdown, FormElement, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../flow2/FlowStep';

import { CreateCredentialDialog } from '../credentials/CreateCredentialDialog';
import { CreatePipelineOutcome } from './GitCreationApi';
import STATE from './GitCreationState';

let t = null;

function validateUrl(url) {
    return !!url && !!url.trim();
}

function isSshRepositoryUrl(url) {
    if (!validateUrl(url)) {
        return false;
    }

    if (/ssh:\/\/.*/.test(url)) {
        return true;
    }

    if (/[^@:]+@.*/.test(url)) {
        return true;
    }

    return false;
}

function isNonSshRepositoryUrl(url) {
    if (!validateUrl(url)) {
        return false;
    }
    return !isSshRepositoryUrl(url) && /[^@:]+:\/\/.*/.test(url);
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
            showCreateCredentialDialog: false,
        };

        t = this.props.flowManager.translate;
    }

    componentWillMount() {
        const { noCredentialsOption } = this.props.flowManager;
        this._selectedCredentialChange(noCredentialsOption);
    }

    _bindDropdown(dropdown) {
        this.dropdown = dropdown;
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

    _onCreateCredentialClick() {
        this.setState({
            showCreateCredentialDialog: true,
        });
    }

    _onCreateCredentialClosed(credential) {
        const newState = {
            showCreateCredentialDialog: false,
        };

        if (credential) {
            newState.selectedCredential = credential;
        }

        this.setState(newState);

        // TODO: control this more cleanly via a future 'selectedOption' prop on Dropdown
        if (this.dropdown) {
            this.dropdown.setState({
                selectedOption: credential,
            });
        }
    }

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
        const { noCredentialsOption } = this.props.flowManager;
        const { flowManager } = this.props;
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

                <ReactCSSTransitionGroup
                    transitionName="slide-down"
                    transitionAppear
                    transitionAppearTimeout={300}
                    transitionEnterTimeout={300}
                    transitionLeaveTimeout={300}
                >
                    {isSshRepositoryUrl(this.state.repositoryUrl) && (
                        <Extensions.Renderer
                            extensionPoint="jenkins.credentials.selection"
                            onComplete={credential => this._onCreateCredentialClosed(credential)}
                            type="git"
                            repositoryUrl={this.state.repositoryUrl}
                        />
                    )}

                    {isNonSshRepositoryUrl(this.state.repositoryUrl) && (
                        <FormElement title={t('creation.git.step1.credentials')} errorMessage={credentialErrorMsg}>
                            <Dropdown
                                ref={dropdown => this._bindDropdown(dropdown)}
                                className="dropdown-credentials"
                                options={flowManager.credentials}
                                defaultOption={noCredentialsOption}
                                labelField="displayName"
                                onChange={opt => this._selectedCredentialChange(opt)}
                            />

                            <button className="button-create-credential btn-secondary" onClick={() => this._onCreateCredentialClick()}>
                                {t('creation.git.step1.create_credential_button')}
                            </button>
                        </FormElement>
                    )}
                </ReactCSSTransitionGroup>

                {this.state.showCreateCredentialDialog && (
                    <CreateCredentialDialog flowManager={flowManager} onClose={cred => this._onCreateCredentialClosed(cred)} />
                )}

                {isSshRepositoryUrl(this.state.repositoryUrl) &&
                    credentialErrorMsg && <FormElement className="public-key-display" errorMessage={t('creation.git.step1.credentials_publickey_invalid')} />}

                <button className="button-create-pipeline" onClick={() => this._beginCreation()} disabled={!validateUrl(this.state.repositoryUrl)}>
                    {createButtonLabel}
                </button>
            </FlowStep>
        );
    }
}

GitConnectStep.propTypes = {
    flowManager: PropTypes.object,
};

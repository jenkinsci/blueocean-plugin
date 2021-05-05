import React from 'react';
import Extensions from '@jenkins-cd/js-extensions';
import {
    Fetch,
    UrlBuilder,
    locationService,
    ContentPageHeader,
    pipelineService,
    Paths,
    RunApi,
    activityService,
    i18nTranslator,
} from '@jenkins-cd/blueocean-core-js';
import { Dialog, TextArea, RadioButtonGroup, TextInput, FormElement, Alerts } from '@jenkins-cd/design-language';
import { isSshRepositoryUrl } from './GitUtils';

import ScmContentApi, { LoadError } from './api/ScmContentApi';

import { convertInternalModelToJson, convertJsonToPipeline, convertPipelineToJson, convertJsonToInternalModel } from './services/PipelineSyntaxConverter';
import pipelineValidator from './services/PipelineValidator';
import pipelineStore from './services/PipelineStore';
import { observer } from 'mobx-react';
import saveApi from './SaveApi';
import { EditorMain } from './components/editor/EditorMain';
import { CopyPastePipelineDialog } from './components/editor/CopyPastePipelineDialog';

const Base64 = { encode: data => btoa(data), decode: str => atob(str) };
const t = i18nTranslator('blueocean-pipeline-editor');

class SaveDialog extends React.Component {
    constructor(props) {
        super(props);
        const { branch } = this.props;
        this.state = { branch: branch };
        this.branchOptions = [{ branch: branch, toString: () => ['Commit to ', <i>{branch}</i>] }, { branch: '', toString: () => `Commit to new branch` }];
    }

    save() {
        const { functions } = this.props;
        this.setState({ errorMessage: null, branchError: false });
        this.setState({ saving: true });
        functions.save(this.state.branch, this.state.commitMessage, (...args) => this.showError(...args));
    }

    cancel() {
        if (!this.state.saving) {
            this.props.cancel();
        }
    }

    showError(err, saveRequest) {
        const { functions } = this.props;
        let errorMessage = err.message ? err.message : err.errors ? err.errors.map(e => <div>{e.error}</div>) : err;
        if (err.responseBody && err.responseBody.message) {
            // GH JSON is dumped as a string in err.responseBody.message
            // error: 409.
            if (err.responseBody.message.indexOf('error: 409.') >= 0) {
                if (this.props.branch !== saveRequest.content.branch) {
                    errorMessage = ['The branch ', <i>{saveRequest.content.branch}</i>, ' already exists'];
                    this.setState({ branchError: errorMessage });
                    errorMessage = null;
                } else {
                    errorMessage = [
                        <div>Your Pipeline was changed by another user.</div>,
                        <div>
                            <a
                                href="#"
                                onClick={() => functions.overwriteChanges(this.state.branch, this.state.commitMessage, (...args) => this.showError(...args))}
                            >
                                Keep my changes
                            </a>{' '}
                            or{' '}
                            <a href="#" onClick={() => functions.discardChanges()}>
                                discard my changes
                            </a>
                        </div>,
                    ];
                }
                //errorMessage = ['An error occurred saving to Github: ', ...errorMessage];
            } else if (errorMessage === 'Server Error') {
                errorMessage = err.responseBody.message;
            } else if (err.response && err.response.status === 500) {
                errorMessage = err.responseBody.message;
            } else if (/^fetch failed:.*/.test(errorMessage)) {
                errorMessage = err.responseBody.message;
            }
        }
        if (err.responseBody && err.responseBody.errors && err.responseBody.errors.length) {
            errorMessage = (
                <div>
                    <div>
                        <strong>{errorMessage}</strong>
                    </div>
                    {err.responseBody.errors.map(e => (
                        <div>
                            <em>{e.code}</em>: {e.message}
                        </div>
                    ))}
                </div>
            );
        }
        this.setState({ saving: false, errorMessage });
    }

    processBranchName(event) {
        const cleanBranchName = event.target.value.replace(/\s/g, '');

        this.branchOptions[1].branch = cleanBranchName;
        this.setState({ branch: cleanBranchName });
    }

    render() {
        const { branch } = this.props;
        const { errorMessage } = this.state;

        const buttons = [
            <button className="btn-primary" onClick={() => this.save()} disabled={this.state.saving}>
                Save & run
            </button>,
            <button className="btn-link btn-secondary" disabled={this.state.saving} onClick={() => this.cancel()}>
                {t('editor.page.common.cancel', { default: 'Cancel' })}
            </button>,
        ];

        return (
            <Dialog onDismiss={() => this.cancel()} title="Save Pipeline" buttons={buttons} className="save-pipeline-dialog">
                {errorMessage && (
                    <div style={{ marginBottom: '10px' }}>
                        <Alerts type="Error" title="Error" message={errorMessage} />
                    </div>
                )}
                <div style={{ width: '400px', marginBottom: '16px' }}>Saving the pipeline will commit a Jenkinsfile to the repository.</div>
                <FormElement title="Description">
                    <TextArea
                        placeholder="What changed?"
                        defaultValue=""
                        width="100%"
                        cols={2}
                        disabled={this.state.saving}
                        onChange={value => this.setState({ commitMessage: value })}
                    />
                </FormElement>
                <RadioButtonGroup
                    options={this.branchOptions}
                    defaultOption={this.branchOptions[0]}
                    onChange={o => this.setState({ branch: o.branch })}
                    disabled={this.state.saving}
                />
                <div className="indent-form" style={{ marginBottom: '-6px' }}>
                    <FormElement className="customBranch" errorMessage={this.state.branchError}>
                        <div className="TextInput">
                            <input
                                className="TextInput-control"
                                type="text"
                                placeholder="my-new-branch"
                                value={this.state.branch}
                                onChange={event => this.processBranchName(event)}
                                disabled={this.state.branch !== this.branchOptions[1].branch || this.state.saving}
                            />
                        </div>
                    </FormElement>
                </div>
            </Dialog>
        );
    }
}

SaveDialog.propTypes = {
    branch: React.PropTypes.string,
    functions: React.PropTypes.object,
    cancel: React.PropTypes.function,
};

@observer
class PipelineLoader extends React.Component {
    state = {};

    constructor(props) {
        super(props);

        this.contentApi = new ScmContentApi();

        this.state = {
            scriptPath: null,
            scmSource: null,
            credential: null,
            sha: null,
        };
    }

    componentWillMount() {
        pipelineStore.setPipeline(null); // reset any previous loaded pipeline
        this.loadPipeline();
    }

    componentDidMount() {
        this.context.router.setRouteLeaveHook(this.props.route, e => this.routerWillLeave(e));
        this.priorUnload = window.onbeforeunload;
        window.onbeforeunload = e => this.routerWillLeave(e);
        pipelineStore.addListener((this.pipelineUpdated = p => this.checkForModification()));
        document.addEventListener(
            'keydown',
            (this.openPipelineScriptDialog = e => {
                if (e.keyCode == 83 && (e.metaKey || e.ctrlKey)) {
                    e.preventDefault();
                    this.showPipelineScript();
                }
            }),
            false
        );
    }

    componentWillUnmount() {
        window.onbeforeunload = this.priorUnload;
        pipelineStore.removeListener(this.pipelineUpdated);
        document.removeEventListener('keypress', this.openPipelineScriptDialog);
    }

    routerWillLeave(e) {
        if (this.pipelineIsModified) {
            const t = 'There are unsaved changes, discard them?';
            if (e) {
                e.returnValue = t;
            }
            return t;
        }
    }

    checkForModification() {
        if (!this.lastPipeline) {
            this.lastPipeline = JSON.stringify(convertInternalModelToJson(pipelineStore.pipeline));
            return;
        }
        if (!this.pipelineIsModified) {
            this.pipelineIsModified = JSON.stringify(convertInternalModelToJson(pipelineStore.pipeline)) !== this.lastPipeline;
        }
    }

    makeEmptyPipeline() {
        // maybe show a dialog the user can choose
        // empty or template
        pipelineStore.setPipeline({
            agent: { type: 'any' },
            children: [],
        });
        this.forceUpdate();
    }

    showLoadingError(
        err,
        generalMessage = (
            <div>
                There was an error loading the pipeline from the Jenkinsfile in this repository. Correct the error by editing the Jenkinsfile using the
                declarative syntax then commit it back to the repository.
            </div>
        ),
        title = 'Error loading Pipeline'
    ) {
        this.showErrorDialog(
            <div className="errors">
                {generalMessage}
                <div>&nbsp;</div>
                <div>
                    <i>{this.extractErrorMessage(err)}</i>
                </div>
            </div>,
            {
                buttonRow: (
                    <button className="btn-primary" onClick={() => this.cancel()}>
                        Go Back
                    </button>
                ),
                onClose: () => this.cancel(),
                title,
            }
        );
    }

    loadPipeline() {
        const { pipeline } = this.props.params;
        this.opener = locationService.previous;

        if (!pipeline) {
            this.makeEmptyPipeline();
            return; // no pipeline to load
        }

        this.loadPipelineMetadata().then(() => {
            this.loadBranchMetadata();
            this.loadContent();
        });
    }

    refreshPipeline(onComplete) {
        this.loadContent(onComplete);
    }

    loadPipelineMetadata() {
        const { organization, pipeline } = this.props.params;
        const split = pipeline.split('/');
        const team = split[0];
        this.href = Paths.rest.pipeline(organization, team);
        return pipelineService
            .fetchPipeline(this.href, { useCache: true })
            .then(pipeline => this._savePipelineMetadata(pipeline))
            .catch(err => {
                this.showErrorDialog(err);
            });
    }

    loadBranchMetadata() {
        const { organization, pipeline, branch } = this.props.params;
        if (!branch) {
            const split = pipeline.split('/');
            const team = split[0];
            const repo = split.length > 1 ? split[1] : team;
            const { id: scmId, apiUrl } = this.state.scmSource;
            const orgRestUrl = UrlBuilder.buildRestUrl(organization);
            let repositoryUrl = `${orgRestUrl}scm/${scmId}/organizations/${team}/repositories/${repo}/`;
            // the team is wrong and do match the github org used so get the branch by searching
            // github repo from the job
            if (scmId == 'github') {
                repositoryUrl = `${orgRestUrl}scm/github/repository?jobName=${repo}&apiUrl=${apiUrl}`;
            } else if (apiUrl) {
                repositoryUrl += `?apiUrl=${apiUrl}`;
            }
            // in case of error it's ignored and we use hardcoded master
            // olamy this could be changed to main which is the new default on github
            return Fetch.fetchJSON(repositoryUrl)
                .then(({ defaultBranch }) => {
                    this.defaultBranch = defaultBranch || 'master';
                })
                .catch(err => (this.defaultBranch = 'master'));
        }

        this.defaultBranch = branch;
        return new Promise(resolve => resolve(null));
    }

    _savePipelineMetadata(pipeline) {
        this.setState({
            scriptPath: pipeline.scriptPath,
            scmSource: pipeline.scmSource,
        });
    }

    checkForNestedSequentialStages(internalModelPipeline, handler) {
        for (let i = 0; i < internalModelPipeline.children.length; i++) {
            for (let j = 0; j < internalModelPipeline.children[i].children.length; j++) {
                if (typeof internalModelPipeline.children[i].children[j].stages !== 'undefined') {
                    handler(internalModelPipeline.children[i].children[j].name); //name of stage that contains sequential parallel stages
                }
            }
        }

        handler(false);
    }

    loadContent(onComplete) {
        const { organization, pipeline, branch, path = this.state.scriptPath } = this.props.params;
        this.contentApi
            .loadContent({ organization, pipeline, branch, path })
            .then(({ content }) => {
                if (!content.base64Data) {
                    throw { type: LoadError.JENKINSFILE_NOT_FOUND };
                }
                const pipelineScript = Base64.decode(content.base64Data);
                this.setState({ sha: content.sha });

                convertPipelineToJson(pipelineScript, (p, err) => {
                    if (!err) {
                        const internal = convertJsonToInternalModel(p);

                        //check if pipeline contains sequential parallel stages and show error if it does
                        this.checkForNestedSequentialStages(internal, errParentStageName => {
                            if (errParentStageName) {
                                const nestedSequentialStagesError = [
                                    {
                                        error: t('editor.page.common.pipeline.nestedSequentialStagesError', { 0: errParentStageName }),
                                    },
                                ];

                                this.showLoadingError(nestedSequentialStagesError);
                            }
                        });

                        if (onComplete) {
                            onComplete(internal);
                        } else {
                            pipelineStore.setPipeline(internal);
                            this.forceUpdate();
                        }
                    } else {
                        this.showLoadingError(err);
                        if (err[0].location) {
                            // revalidate in case something missed it (e.g. create an empty stage then load/save)
                            pipelineValidator.validate();
                        }
                    }
                });
            })
            .catch(err => {
                if (err.type === LoadError.JENKINSFILE_NOT_FOUND) {
                    if (onComplete) onComplete();
                    this.makeEmptyPipeline();
                } else if (err.type === LoadError.TOKEN_NOT_FOUND || err.type === LoadError.TOKEN_REVOKED) {
                    // if there already is a dialog, make sure to show it: an invalid credential was selected
                    this.showCredentialDialog({ loading: !this.state.dialog });
                } else {
                    if (onComplete) onComplete();
                    this.showLoadingError(err);
                }
            });
    }

    overwriteChanges(saveToBranch, commitMessage, errorHandler) {
        const currentPipeline = pipelineStore.pipeline;
        this.refreshPipeline(() => {
            pipelineStore.setPipeline(currentPipeline);
            this.save(saveToBranch, commitMessage, errorHandler);
        });
    }

    discardChanges() {
        this.refreshPipeline(pipeline => {
            pipelineStore.setPipeline(pipeline);
            this.setState({ showSaveDialog: false });
        });
    }

    showPipelineScript() {
        this.setState({ dialog: <CopyPastePipelineDialog onClose={() => this.closeDialog()} /> });
    }

    cancel() {
        const { organization, pipeline, branch } = this.props.params;
        const { router } = this.context;
        const location = {};
        location.pathname = branch == null ? '/' : UrlBuilder.buildPipelineUrl(organization, pipeline);
        location.query = null;

        if (this.opener) {
            router.goBack();
        } else {
            router.push(location);
        }
    }

    goToActivity() {
        const { organization, pipeline, branch } = this.props.params;
        const { router } = this.context;
        const location = UrlBuilder.buildPipelineUrl(organization, pipeline);
        activityService.removeItem(activityService.pagerKey(organization, pipeline, branch));
        router.push(location);
    }

    closeDialog() {
        this.setState({ dialog: null });
    }

    extractErrorMessage(err) {
        let errorMessage = err;
        if (err instanceof String || typeof err === 'string') {
            errorMessage = err;
        } else if (err instanceof Array || typeof err === 'array') {
            errorMessage = err.map(e => <div>{this.extractErrorMessage(e.error)}</div>);
        } else if (err.responseBody && err.responseBody.message) {
            // Github error
            errorMessage = err.responseBody.message;
            // error: 409.
            if (errorMessage.indexOf('error: 409.') >= 0) {
                if (this.props.params.branch !== saveRequest.content.branch) {
                    errorMessage = ['the branch ', <i>{saveRequest.content.branch}</i>, ' already exists'];
                } else {
                    errorMessage = ['the pipeline was modified outside of the editor'];
                }
                errorMessage = ['An error occurred saving to Github: ', ...errorMessage];
            } else if (errorMessage === 'Server Error') {
                errorMessage = err.responseBody.message;
            } else if (err.response && err.response.status === 500) {
                errorMessage = err.responseBody.message;
            }
        } else if (err.message) {
            errorMessage = err.message;
        }
        return errorMessage;
    }

    showErrorDialog(err, { saveRequest, buttonRow, onClose, title } = {}) {
        const buttons = buttonRow || [
            <button className="btn-primary" onClick={() => this.closeDialog()}>
                Ok
            </button>,
        ];

        this.setState({
            showSaveDialog: false,
            dialog: (
                <Dialog onDismiss={() => (onClose ? onClose() : this.closeDialog())} title={title || 'Error'} className="Dialog--error" buttons={buttons}>
                    <div style={{ width: '28em' }}>{this.extractErrorMessage(err)}</div>
                </Dialog>
            ),
        });
    }

    showCredentialDialog({ loading = false } = {}) {
        const { branch = this.defaultBranch } = this.props.params;
        const pipeline = pipelineService.getPipeline(this.href);
        const { scmSource } = pipeline;

        // if (!scmSource || !scmSource.id || (scmSource.id === 'git' && !isSshRepositoryUrl(scmSource.apiUrl))) {
        //     this.showLoadingError('', 'Saving Pipelines is unsupported using http/https repositories. Please use SSH instead.', 'No save access');
        //     return;
        // }

        // if showing this dialog with a credential, the write test failed
        // except for git, where we need to prompt with the user's public key so they can continue
        // in the case the user has a credential, like a github token there is only one
        // so we don't want to prompt them for another, instead they should contact someone
        // to fix the permissions
        if (this.state.credential && this.state.credential.id && scmSource.id !== 'git') {
            this.showLoadingError('', 'You do not appear to be able to save to this repository', 'No save access');
            return;
        }

        const title = this.getScmTitle(scmSource.id);
        const githubConfig = {
            scmId: scmSource.id,
            apiUrl: scmSource.apiUrl,
        };
        // hide the dialog until it reports as ready (i.e. credential fetch is done)
        const dialogClassName = `dialog-token ${loading ? 'loading' : ''}`;

        //FIXME: should show a message about existing credentials failing

        this.setState({
            dialog: (
                <Dialog title={title} className={dialogClassName} buttons={[]} onDismiss={() => this.cancel()}>
                    <Extensions.Renderer
                        extensionPoint="jenkins.credentials.selection"
                        onStatus={status => this.onCredentialStatus(status)}
                        onComplete={cred => this.onCredentialSelected(cred)}
                        type={scmSource.id}
                        githubConfig={githubConfig}
                        pipeline={{ fullName: pipeline.fullName }}
                        requirePush
                        branch={branch}
                        dialog
                        existingFailed
                    />
                </Dialog>
            ),
        });
    }

    getScmTitle(scmId) {
        let scmLabel = '';

        if (scmId === 'github') {
            scmLabel = 'GitHub';
        } else if (scmId === 'github-enterprise') {
            scmLabel = 'GitHub Enterprise';
        } else if (scmId === 'bitbucket-cloud') {
            scmLabel = 'Bitbucket Cloud';
        } else if (scmId === 'bitbucket-server') {
            scmLabel = 'Bitbucket Server';
        } else if (scmId === 'git') {
            scmLabel = 'Git';
        }

        if (scmLabel) {
            return `Connect to ${scmLabel}`;
        }

        return 'Unknown SCM Provider';
    }

    onCredentialStatus(status) {
        if (status === 'promptReady') {
            this.showCredentialDialog();
        }
    }

    onCredentialSelected(credential) {
        this.setState({
            credential,
        });

        this.loadContent(internal => {
            if (internal) {
                // may be no pipeline here
                pipelineStore.setPipeline(internal);
            }
            this.setState({ dialog: null });
        });
    }

    showSaveDialog() {
        pipelineValidator.validate(err => {
            if (!pipelineValidator.hasValidationErrors(pipelineStore.pipeline)) {
                this.setState({ showSaveDialog: true });
            } else {
                this.showErrorDialog('There are validation errors, please check the pipeline.');
            }
        });
    }

    save(saveToBranch, commitMessage, errorHandler) {
        const { organization, pipeline, branch = this.defaultBranch } = this.props.params;
        const pipelineJson = convertInternalModelToJson(pipelineStore.pipeline);
        const split = pipeline.split('/');
        const team = split[0];
        const repo = split[1];
        const saveMessage = commitMessage || (this.state.isSaved ? 'Updated Jenkinsfile' : 'Added Jenkinsfile');
        convertJsonToPipeline(JSON.stringify(pipelineJson), (pipelineScript, err) => {
            if (!err) {
                const saveParams = {
                    organization,
                    pipeline: team,
                    repo,
                    sourceBranch: branch,
                    targetBranch: saveToBranch || this.defaultBranch,
                    sha: this.state.sha,
                    message: saveMessage,
                    path: this.state.scriptPath,
                    content: pipelineScript,
                };

                // TODO: building the body so we can pass it down is a little awkward
                const body = this.contentApi.buildSaveContentRequest(saveParams);
                this.contentApi
                    .saveContent(saveParams)
                    .then(data => {
                        this.pipelineIsModified = false;
                        this.lastPipeline = JSON.stringify(convertInternalModelToJson(pipelineStore.pipeline));
                        // If this is a save on the same branch that already has a Jenkinsfile, just re-run it
                        if (this.state.isSaved && branch === data.content.branch) {
                            RunApi.startRun({ _links: { self: { href: this.href + 'branches/' + encodeURIComponent(branch) + '/' } } })
                                .then(() => this.goToActivity())
                                .catch(err => errorHandler(err, body));
                        } else {
                            // if a different branch, call indexing so this one gets picked up
                            // only time we have 'github' is when we are using an org folder
                            // in which case use the existing saveApi
                            const { id: scmId, apiUrl } = this.state.scmSource;
                            saveApi.index(this.href, () => this.goToActivity(), err => errorHandler(err));
                        }
                        this.setState({ sha: data.sha, isSaved: true });
                    })
                    .catch(err => {
                        errorHandler(err, body);
                    });
            } else {
                errorHandler(err);
            }
        });
    }

    render() {
        const { pipeline: pipelineName, branch } = this.props.params;
        const { pipelineScript } = this.state;
        const pipeline = pipelineService.getPipeline(this.href);
        const repo = pipelineName && pipelineName.split('/')[1];
        let title = pipeline ? decodeURIComponent(pipeline.fullDisplayName.replace('/', ' / ')) : pipelineName;
        if (branch || repo) {
            title += ' / ' + (branch || repo);
        }

        return (
            <div className="pipeline-page">
                <Extensions.Renderer extensionPoint="pipeline.editor.css" />
                <ContentPageHeader>
                    <div className="u-flex-grow">
                        <h1>{pipeline && title}</h1>
                    </div>
                    {pipelineStore.pipeline && (
                        <div className="editor-page-header-controls">
                            <button className="btn-link inverse" onClick={() => this.cancel()}>
                                {t('editor.page.common.cancel', { default: 'Cancel' })}
                            </button>
                            {pipelineName && (
                                <button className="btn-primary inverse" onClick={() => this.showSaveDialog()}>
                                    {t('editor.page.common.save', { default: 'Save' })}
                                </button>
                            )}
                        </div>
                    )}
                </ContentPageHeader>
                {pipelineStore.pipeline && (
                    <div className="pipeline-editor">
                        <EditorMain />
                    </div>
                )}
                {this.state.dialog}
                {this.state.showSaveDialog && (
                    <SaveDialog branch={branch || this.defaultBranch} cancel={() => this.setState({ showSaveDialog: false })} functions={this} />
                )}
            </div>
        );
    }
}

PipelineLoader.contextTypes = {
    router: React.PropTypes.object,
    location: React.PropTypes.object,
};

PipelineLoader.propTypes = {
    params: React.PropTypes.object,
};

export const EditorPage = PipelineLoader;

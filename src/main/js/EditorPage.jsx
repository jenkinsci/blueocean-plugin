import React from 'react';
import { Link } from 'react-router';
import Extensions from '@jenkins-cd/js-extensions';
import {
        Fetch, getRestUrl, buildPipelineUrl, locationService,
        ContentPageHeader, pipelineService, Paths, RunApi, ToastService,
    } from '@jenkins-cd/blueocean-core-js';
import {
    Dialog,
    TextArea,
    RadioButtonGroup,
    TextInput,
    FormElement,
    Alerts,
} from '@jenkins-cd/design-language';
import { convertInternalModelToJson, convertJsonToPipeline, convertPipelineToJson, convertJsonToInternalModel } from './services/PipelineSyntaxConverter';
import pipelineValidator from './services/PipelineValidator';
import pipelineStore from './services/PipelineStore';
import { observer } from 'mobx-react';
import { observable, action } from 'mobx';
import saveApi from './SaveApi';
import { EditorMain } from './components/editor/EditorMain.jsx';

const Base64 = { encode: (data) => btoa(data), decode: (str) => atob(str) };

class SaveDialog extends React.Component {
    constructor(props) {
        super(props);
        const { branch } = this.props;
        this.state = { branch: branch };
        this.branchOptions = [
           { branch: branch, toString: () => ['Commit to ', <i>{branch}</i>]},
           { branch: '', toString: () => `Commit to new branch`},
       ];
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
        let errorMessage = err.message ? err.message : (err.errors ? err.errors.map(e => <div>{e.error}</div>) : err);
        if (err.responseBody && err.responseBody.message) { 
            errorMessage = err.responseBody.message;    
            // error: 409.
            if (errorMessage.indexOf('error: 409.') >= 0) {
                if (this.props.branch !== saveRequest.content.branch) {
                    errorMessage = ['The branch ', <i>{saveRequest.content.branch}</i>, ' already exists'];
                    this.setState({ branchError: errorMessage });
                    errorMessage = null;
                } else {
                    errorMessage = [
                        <div>Your Pipeline was changed by another user.</div>,
                        <div>
                            <a href="#" onClick={() => functions.overwriteChanges(this.state.branch, this.state.commitMessage, (...args) => this.showError(...args))}>
                                Keep my changes
                            </a> or <a href="#" onClick={() => functions.discardChanges()}>discard my changes</a>
                        </div>,
                    ];
                }
                //errorMessage = ['An error occurred saving to Github: ', ...errorMessage];
            }
        }
        this.setState({ saving: false, errorMessage });
    }

    render() {
        const { branch } = this.props;
        const { errorMessage } = this.state;
        
        const buttons = [
            <button className="btn-primary" onClick={() => this.save()} disabled={this.state.saving}>Save & run</button>,
            <button className="btn-link btn-secondary" disabled={this.state.saving} onClick={() => this.cancel()}>Cancel</button>,
        ];
        
        return (
            <Dialog onDismiss={() => this.cancel()} title="Save Pipeline" buttons={buttons} className="save-pipeline-dialog">
                {errorMessage && <div style={{marginBottom: '10px'}}><Alerts type="Error" title="Error" message={errorMessage} /></div>}
                <div style={{width: '400px', marginBottom: '16px'}}>Saving the pipeline will commit a Jenkinsfile to the repository.</div>
                <FormElement title="Description">
                    <TextArea placeholder="What changed?" defaultValue="" width="100%" cols={2} disabled={this.state.saving}
                        onChange={value => this.setState({commitMessage: value})} />
                </FormElement>
                <RadioButtonGroup options={this.branchOptions} defaultOption={this.branchOptions[0]}
                    onChange={o => this.setState({branch: o.branch})} disabled={this.state.saving} />
                <div className="indent-form" style={{marginBottom: '-6px'}}>
                <FormElement className="customBranch" errorMessage={this.state.branchError}>
                    <TextInput placeholder="my-new-branch" onChange={value => this.setState({branch: this.branchOptions[1].branch = value})}
                        disabled={this.state.branch !== this.branchOptions[1].branch || this.state.saving} />
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
    state = {}
    
    componentWillMount() {
        pipelineStore.setPipeline(null); // reset any previous loaded pipeline
        this.loadPipeline();
    }
    
    componentDidMount() {
        this.context.router.setRouteLeaveHook(this.props.route, e => this.routerWillLeave(e));
        this.priorUnload = window.onbeforeunload;
        window.onbeforeunload = e => this.routerWillLeave(e);
        pipelineStore.addListener(this.pipelineUpdated = p => this.checkForModification());
    }

    componentWillUnmount() {
        window.onbeforeunload = this.priorUnload;
        pipelineStore.removeListener(this.pipelineUpdated);
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

    loadPipeline(onComplete) {
        const { organization, pipeline, branch } = this.props.params;
        this.opener = locationService.previous;
        
        Fetch.fetchJSON(`${getRestUrl(this.props.params)}scm/content/?branch=${encodeURIComponent(branch)}&path=Jenkinsfile`)
        .then( ({ content }) => {
            const pipelineScript = Base64.decode(content.base64Data);
            this.setState({sha: content.sha});
            convertPipelineToJson(pipelineScript, (p, err) => {
                if (!err) {
                    const internal = convertJsonToInternalModel(p);
                    if (onComplete) {
                        onComplete(internal);
                    } else {
                        pipelineStore.setPipeline(internal);
                        this.forceUpdate();
                    }
                } else {
                    this.showErrorDialog(
                        <div className="errors">
                            <div>There was an error loading the pipeline</div>
                            <div>{err.map(e => <div>{e.error}</div>)}</div>
                        </div>
                        , {
                            buttonRow: <button className="btn-primary" onClick={() => this.cancel()}>Go Back</button>,
                            onClose: () => this.cancel()
                        });
                    if(err[0].location) {
                        // revalidate in case something missed it (e.g. create an empty stage then load/save)
                        pipelineValidator.validate();
                    }
                }
            });
        })
        .catch(err => {
            if (err.response.status != 404) {
                this.showErrorDialog(err);
            } else {
                // maybe show a dialog the user can choose
                // empty or template
                pipelineStore.setPipeline({
                    agent: { type: 'any' },
                    children: [],
                });
                
                ToastService.newToast({
                    style: 'info',
                    caption: "No pipeline found",
                    text: "Creating a blank pipeline",
                });
            }
        });
        
        this.href = Paths.rest.pipeline(organization, pipeline);
        pipelineService.fetchPipeline(this.href, { useCache: true })
        .then(pipeline => this.forceUpdate())
        .catch(err => {
            // No pipeline, use org folder
            const team = pipeline.split('/')[0];
            this.href = Paths.rest.pipeline(organization, team);
            pipelineService.fetchPipeline(this.href, { useCache: true })
            .then(pipeline => this.forceUpdate())
            .catch(err => {
                this.showErrorDialog(err);
            });
        });
    }

    overwriteChanges(saveToBranch, commitMessage, errorHandler) {
        const currentPipeline = pipelineStore.pipeline;
        this.loadPipeline(() => {
            pipelineStore.setPipeline(currentPipeline);
            this.save(saveToBranch, commitMessage, errorHandler);
        });
    }

    discardChanges() {
        this.loadPipeline(pipeline => {
            pipelineStore.setPipeline(pipeline);
            this.setState({ showSaveDialog: false });
        });
    }

    cancel() {
        const { organization, pipeline, branch } = this.props.params;
        const { router } = this.context;
        const location = {};
        location.pathname = branch == null ? '/' : buildPipelineUrl(organization, pipeline);
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
        const location = buildPipelineUrl(organization, pipeline);
        router.push(location);
    }
    
    closeDialog() {
        this.setState({ dialog: null });
    }
    
    showErrorDialog(err, { saveRequest, buttonRow, onClose } = {}) {
        let errorMessage = err;
        if (err instanceof String || typeof err === 'string') {
            errorMessage = err;
        }
        else if (err.responseBody && err.responseBody.message) {
            // Github error
            errorMessage = err.responseBody.message;
            // error: 409.
            if (errorMessage.indexOf('error: 409.') >= 0) {
                if (this.props.params.branch !== saveRequest.content.branch) {
                    errorMessage = ['the branch ', <i>{saveRequest.content.branch}</i>, ' already exists'];
                } else {
                    errorMessage = ['the pipeline was modified ouside of the editor'];
                }
                errorMessage = ['An error occurred saving to Github: ', ...errorMessage];
            }
        }
        else if (err.message) {
            errorMessage = err.message;
        }
        const buttons = buttonRow || [
            <button className="btn-primary" onClick={() => this.closeDialog()}>Ok</button>,
        ];
        
//        this.setState({
//            showSaveDialog: false,
//            dialog: <Alerts type="Error" title="Error" message={errorMessage} />
//        });
        
        this.setState({
            showSaveDialog: false,
            dialog: (
            <Dialog onDismiss={() => onClose ? onClose() : this.closeDialog()} title="Error" className="Dialog--error" buttons={buttons}>
                <div style={{width: '28em'}}>
                    {errorMessage}
                </div>
            </Dialog>
        )});
    }
    
    showSaveDialog() {
        pipelineValidator.validate(err => {
            if (!pipelineValidator.hasValidationErrors(pipelineStore.pipeline)) {
                this.setState({showSaveDialog: true});
            } else {
                this.showErrorDialog("There are validation errors, please check the pipeline.");
            }
        });
    }
    
    save(saveToBranch, commitMessage, errorHandler) {
        const { organization, pipeline, branch } = this.props.params;
        const pipelineJson = convertInternalModelToJson(pipelineStore.pipeline);
        const split = pipeline.split('/');
        const team = split[0];
        const repo = split[1];
        const saveMessage = commitMessage || (this.state.sha ? 'Updated Jenkinsfile' : 'Added Jenkinsfile');
        convertJsonToPipeline(JSON.stringify(pipelineJson), (pipelineScript, err) => {
            if (!err) {
                const body = {
                    "content": {
                      "message": saveMessage,
                      "path": "Jenkinsfile",
                      branch: saveToBranch || 'master',
                      repo: repo,
                      "sha": this.state.sha,
                      "base64Data": Base64.encode(pipelineScript),
                    }
                };
                const pipelineObj = pipelineService.getPipeline(this.href);
                Fetch.fetchJSON(`${getRestUrl({organization: organization, pipeline: team})}scm/content/`, {
                    fetchOptions: {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(body),
                    }
                })
                .then(data => {
                    this.pipelineIsModified = false;
                    this.lastPipeline = JSON.stringify(convertInternalModelToJson(pipelineStore.pipeline));
                    // If this is a save on the same branch that already has a Jenkinsfile, just re-run it
                    if (this.state.sha && branch === body.content.branch) {
                        RunApi.startRun({ _links: { self: { href: this.href + 'branches/' + branch + '/' }}})
                            .then(() => this.goToActivity())
                            .catch(err => errorHandler(err, body));//this.showErrorDialog(err));
                    } else {
                        // otherwise, call indexing so this branch gets picked up
                        saveApi.index(organization, team, () => this.goToActivity(), err => errorHandler(err));//this.showErrorDialog(err));
                    }
                    this.setState({ sha: data.sha });
                })
                .catch(err => {
                    errorHandler(err, body);//this.showErrorDialog(err, { saveRequest: body });
                });
            } else {
                errorHandler(err);//this.showErrorDialog(err);
            }
        });
    }

    render() {
        const { branch } = this.props.params;
        const { pipelineScript } = this.state;
        const pipeline = pipelineService.getPipeline(this.href);
        const repo = this.props.params.pipeline.split('/')[1];
        return (<div className="pipeline-page">
            <Extensions.Renderer extensionPoint="pipeline.editor.css"/>
            <ContentPageHeader>
                <div className="u-flex-grow">
                    <h1>
                        {pipeline && (decodeURIComponent(pipeline.fullDisplayName.replace('/', ' / ')) + ' / ' + (branch || repo))}
                    </h1>
                </div>
                <div className="editor-page-header-controls">
                    <button className="btn-link inverse" onClick={() => this.cancel()}>Cancel</button>
                    <button className="btn-primary inverse" onClick={() => this.showSaveDialog()}>Save</button>
                </div>
            </ContentPageHeader>
            {pipelineStore.pipeline &&
                <div className="pipeline-editor">
                    <EditorMain />
                </div>
            }
            {this.state.dialog}
            {this.state.showSaveDialog && <SaveDialog branch={branch || 'master'}
                cancel={() => this.setState({showSaveDialog: false})}
                functions={this} />
            }
        </div>);
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

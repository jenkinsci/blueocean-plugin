// @flow

import React, { Component, PropTypes } from 'react';
import { Dialog } from '@jenkins-cd/design-language';
import pipelineStore from '../../services/PipelineStore';
import { convertInternalModelToJson, convertJsonToPipeline, convertPipelineToJson, convertJsonToInternalModel } from '../../services/PipelineSyntaxConverter';
import type { PipelineInfo } from '../../services/PipelineStore';
import type { PipelineJsonContainer } from '../../services/PipelineSyntaxConverter';
import pipelineMetadataService from '../../services/PipelineMetadataService';
import pipelineValidator from '../../services/PipelineValidator';

const PIPELINE_KEY = 'jenkins.pipeline.editor.workingCopy';

type Props = {
    title?: string,
    children: Component<*,*,*>[],
    style?: ?Object,
};

type State = {
    showPipelineScript?: boolean,
    pipelineScript?: string,
    pipelineErrors?: ?string[],
};

type DefaultProps = typeof EditorPage.defaultProps;

export class EditorPage extends Component<DefaultProps, Props, State> {

    static defaultProps = {
        children: (null: any)
    };

    static propTypes = {
        title: PropTypes.string,
        children: PropTypes.array,
        style: PropTypes.object,
    };

    pipelineUpdated: Function;

    state: State = {};

    currentHistoryIndex: number = -1;
    pipelineHistory: PipelineJsonContainer[] = [];

    componentWillMount() {
        let existingPipeline: any = localStorage.getItem(PIPELINE_KEY);
        if (existingPipeline) {
            pipelineMetadataService.getStepListing(steps => {
                existingPipeline = convertJsonToInternalModel(JSON.parse(existingPipeline));
                pipelineStore.setPipeline(existingPipeline);
                pipelineValidator.validate();
            });
        } else {
            this.newPipeline();
        }
        pipelineStore.addListener(this.pipelineUpdated = p => this.savePipelineState());
    }

    componentWillUnmount() {
        pipelineStore.removeListener(this.pipelineUpdated);
    }

    savePipelineState() {
        if (pipelineStore.pipeline.fromHistory) {
            delete pipelineStore.pipeline.fromHistory;
            this.forceUpdate();
            return;
        }
        const json = convertInternalModelToJson(pipelineStore.pipeline);

        this.currentHistoryIndex++;
        if (this.pipelineHistory.length > 0) {
            this.pipelineHistory = this.pipelineHistory.slice(0, this.currentHistoryIndex);
        }
        this.pipelineHistory.push(json);
        localStorage.setItem(PIPELINE_KEY, JSON.stringify(json));
        this.forceUpdate();
    }

    undo() {
        if (this.currentHistoryIndex < 1) {
            return; // no history or already at the oldest
        }
        this.currentHistoryIndex--;
        const json = this.pipelineHistory[this.currentHistoryIndex];
        if (json) {
            const internal = convertJsonToInternalModel(json);
            internal.fromHistory = true;
            pipelineStore.setPipeline(internal);
        }
    }

    updateStateFromPipelineScript(pipeline: string) {
        convertPipelineToJson(pipeline, (p, err) => {
            if (!err) {
                const internal = convertJsonToInternalModel(p);
                this.setState({showPipelineScript: false, pipelineErrors: null}),
                pipelineStore.setPipeline(internal);
            } else {
                this.setState({pipelineErrors: err});
                if(err[0].location) {
                    // revalidate in case something missed it (e.g. create an empty stage then load/save)
                    pipelineValidator.validate();
                }
            }
        });
    }

    showPipelineScriptDialog() {
        if (pipelineStore.pipeline) {
            const json = convertInternalModelToJson(pipelineStore.pipeline);
            convertJsonToPipeline(JSON.stringify(json), (result, err) => {
                if (!err) {
                    this.setState({showPipelineScript: true, pipelineErrors: null, pipelineScript: result});
                } else {
                    this.setState({showPipelineScript: true, pipelineErrors: err, pipelineScript: ''});
                }
            });
        } else {
            this.setState({showPipelineScript: true});
        }
    }

    newPipeline() {
        const newTemplate = require('./NewPipelineTemplate.json');
        if (newTemplate) {
            pipelineStore.setPipeline(convertJsonToInternalModel(newTemplate));
        } else {
            pipelineStore.setPipeline({
                agent: {type: "any"},
                children: [],
            });
        }
    }

    render() {

        let {title = "Create Pipeline", style} = this.props;

        return (
            <div className="editor-page-outer" style={style}>
                <div className="editor-page-header">
                    <h3>{ title }</h3>
                    <div className="editor-page-header-controls">
                        {false && <button disabled={this.currentHistoryIndex <= 0} className="btn-secondary inverse" onClick={() => this.undo()}>Undo</button>}
                        <button className="btn-secondary inverse" onClick={() => this.newPipeline()}>New</button>
                        <button className="btn inverse" onClick={() => this.showPipelineScriptDialog()}>Load/Save</button>
                    </div>
                </div>
                {this.props.children}
                {this.state.showPipelineScript &&
                    <Dialog className="editor-pipeline-dialog" onDismiss={() => this.setState({showPipelineScript: false})}
                        title="Pipeline Script"
                        buttons={<div><button onClick={e => this.updateStateFromPipelineScript(this.refs.pipelineScript.value)}>Update</button></div>}>
                        {!localStorage.getItem('pipeline-editor-usage-blurb-accept') &&
                        <div className="load-save-usage-blurb">
                            This is your pipeline as text, you can copy it 
                            and put it in a Jenkinsfile for your project, 
                            for example in a&nbsp;<a target="_blank" href="http://jenkins.io/doc/book/pipeline/multibranch/">multibranch project</a>. 

                            You can also paste your own&nbsp;<a target="_blank" href="http://jenkins.io/doc/book/pipeline/">Declarative Pipeline</a>&nbsp;script
                            in this text box, and press 'Update' to edit it visually. 
                            Pressing 'Load/Save' will bring you back here with your changes. 

                            You can read more about&nbsp;<a target="_blank" href="http://jenkins.io/doc/book/pipeline/">Declarative Pipelines here</a>. 
                            <div>
                                <button className="btn-secondary" onClick={e => localStorage.setItem('pipeline-editor-usage-blurb-accept', true) || this.forceUpdate()}>Got it!</button>
                            </div>
                        </div>
                        }
                        {this.state.pipelineErrors && !this.state.pipelineErrors[0].location &&
                            <ul className="pipeline-validation-errors">
                                {this.state.pipelineErrors.map(err => <li>{err.error}</li>)}
                            </ul>
                        }
                        {this.state.pipelineErrors && this.state.pipelineErrors[0].location &&
                            <ul className="pipeline-validation-errors">
                                <li onClick={e => { this.state.pipelineErrors.expand = true; this.forceUpdate(); }}>There were validation errors, please check the editor to correct them</li>
                                {this.state.pipelineErrors.expand && this.state.pipelineErrors.map(err => <li>{err.location && err.location.join('/')}: {err.error}</li>)}
                            </ul>
                        }
                        <div className="editor-text-area">
                            <textarea ref="pipelineScript" style={{width: "100%", minHeight: "30em", height: "100%"}} defaultValue={this.state.pipelineScript}/>
                        </div>
                    </Dialog>
                }
            </div>
        );
    }
}

export default EditorPage;

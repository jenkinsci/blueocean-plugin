// @flow

import React, { Component, PropTypes } from 'react';
import { Dialog } from '@jenkins-cd/design-language';
import pipelineStore from '../../services/PipelineStore';
import { convertInternalModelToJson, convertJsonToPipeline, convertPipelineToJson, convertJsonToInternalModel } from '../../services/PipelineSyntaxConverter';
import type { PipelineInfo } from '../../services/PipelineStore';
import type { PipelineJsonContainer } from '../../services/PipelineSyntaxConverter';
import pipelineStepListStore from '../../services/PipelineStepListStore';

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
            pipelineStepListStore.getStepListing(steps => {
                existingPipeline = convertJsonToInternalModel(JSON.parse(existingPipeline));
                pipelineStore.setPipeline(existingPipeline);
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
                    this.setState({showPipelineScript: true, pipelineErrors: err});
                }
            });
        } else {
            this.setState({showPipelineScript: true});
        }
    }

    newPipeline() {
        pipelineStore.setPipeline({
            agent: { isLiteral: true, value: 'any' },
            children: [],
        });
    }

    render() {

        let {title = "Create Pipeline", style} = this.props;

        return (
            <div className="editor-page-outer" style={style}>
                <div className="editor-page-header">
                    <h3>{ title }</h3>
                    <div className="editor-page-header-controls">
                        <button disabled={this.currentHistoryIndex <= 0} className="btn-secondary inverse" onClick={() => this.undo()}>Undo</button>
                        <button className="btn-secondary inverse" onClick={() => this.newPipeline()}>New</button>
                        <button className="btn inverse" onClick={() => this.showPipelineScriptDialog()}>Load/Save</button>
                    </div>
                </div>
                {this.props.children}
                {this.state.showPipelineScript &&
                    <Dialog className="editor-pipeline-dialog" onDismiss={() => this.setState({showPipelineScript: false})}
                        title="Pipeline Script"
                        buttons={<div><button onClick={e => this.updateStateFromPipelineScript(this.refs.pipelineScript.value)}>Update</button></div>}>
                        {this.state.pipelineErrors &&
                            <div className="errors">
                                {this.state.pipelineErrors.map(err => <div className="error">{err}</div>)}
                            </div>
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

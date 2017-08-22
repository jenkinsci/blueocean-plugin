// @flow

import React, { Component, PropTypes } from 'react';
import { Dialog } from '@jenkins-cd/design-language';
import { ContentPageHeader } from '@jenkins-cd/blueocean-core-js';
import pipelineStore from '../../services/PipelineStore';
import { convertInternalModelToJson, convertJsonToPipeline, convertPipelineToJson, convertJsonToInternalModel } from '../../services/PipelineSyntaxConverter';
import type { PipelineInfo } from '../../services/PipelineStore';
import type { PipelineJsonContainer } from '../../services/PipelineSyntaxConverter';
import pipelineValidator from '../../services/PipelineValidator';

const PIPELINE_KEY = 'jenkins.pipeline.editor.workingCopy';

type Props = {
    onClose: ?PropTypes.func,
};

type State = {
    pipelineScript?: PropTypes.string,
    pipelineErrors?: ?PropTypes.string[],
};

type DefaultProps = typeof CopyPastePipelineDialog.defaultProps;

export class CopyPastePipelineDialog extends Component<DefaultProps, Props, State> {
    state: State = {};

    componentWillMount() {
        if (pipelineStore.pipeline) {
            const json = convertInternalModelToJson(pipelineStore.pipeline);
            convertJsonToPipeline(JSON.stringify(json), (result, err) => {
                if (!err) {
                    this.setState({pipelineErrors: null, pipelineScript: result});
                } else {
                    this.setState({pipelineErrors: err, pipelineScript: ''});
                }
            });
        }
    }

    updateStateFromPipelineScript(pipeline: string) {
        convertPipelineToJson(pipeline, (p, err) => {
            if (!err) {
                const internal = convertJsonToInternalModel(p);
                this.setState({pipelineErrors: null}),
                pipelineStore.setPipeline(internal);
                this.props.onClose();
            } else {
                this.setState({pipelineErrors: err});
                if(err[0].location) {
                    // revalidate in case something missed it (e.g. create an empty stage then load/save)
                    pipelineValidator.validate();
                }
            }
        });
    }

    render() {
        return (
            <Dialog className="editor-pipeline-dialog" onDismiss={() => this.props.onClose()}
                title="Pipeline Script"
                buttons={<div><button onClick={e => this.updateStateFromPipelineScript(this.state.pipelineScript)}>Update</button></div>}>
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
                    <textarea onChange={e => this.setState({ pipelineScript: e.target.value})} style={{width: "100%", minHeight: "30em", height: "100%"}} value={this.state.pipelineScript}/>
                </div>
            </Dialog>
        );
    }
}

export default CopyPastePipelineDialog;

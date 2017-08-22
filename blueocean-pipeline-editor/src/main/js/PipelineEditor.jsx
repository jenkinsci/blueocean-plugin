// @flow

import React, { Component, PropTypes } from 'react';
import { Dialog } from '@jenkins-cd/design-language';
import { ContentPageHeader } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import pipelineStore from './services/PipelineStore';
import { convertInternalModelToJson, convertJsonToPipeline, convertPipelineToJson, convertJsonToInternalModel } from './services/PipelineSyntaxConverter';
import type { PipelineInfo } from './services/PipelineStore';
import type { PipelineJsonContainer } from './services/PipelineSyntaxConverter';
import pipelineValidator from './services/PipelineValidator';
import { EditorMain } from './components/editor/EditorMain.jsx';

type Props = {
    pipeline?: string,
};

type State = {
    showPipelineScript?: boolean,
    pipelineScript?: string,
    pipelineErrors?: ?string[],
};

type DefaultProps = typeof PipelineEditor.defaultProps;

export class PipelineEditor extends Component<DefaultProps, Props, State> {
    static propTypes = {
        pipeline: PropTypes.string,
    };

    state: State = {};
    
    constructor() {
        super();
    }

    componentWillMount() {
        this.handleProps(null, this.props);
    }
    
    componentWillReceiveProps(newProps) {
        this.handleProps(this.props, newProps);
    }
    
    handleProps(oldProps, newProps) {
        if (!newProps.pipeline) {
            this.newPipeline();
        } else if (!oldProps || oldProps.pipeline !== newProps.pipeline) {
            this.updateStateFromPipelineScript(newProps.pipeline);
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
        return (
            <div className="pipeline-editor">
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
                <Extensions.Renderer extensionPoint="pipeline.editor.css"/>
                <EditorMain />
            </div>
        );
    }
}

export default PipelineEditor;

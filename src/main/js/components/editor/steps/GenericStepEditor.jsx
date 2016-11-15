// @flow

import React, { Component, PropTypes } from 'react';
import pipelineStepListStore from '../../../services/PipelineStepListStore';

type Props = {
    onChange: Function,
    step: any,
}

type State = {
    stepMetadata: Array<any>,
};

type DefaultProps = typeof GenericStepEditorPanel.defaultProps;

export default class GenericStepEditorPanel extends Component<DefaultProps, Props, State> {
    props:Props;
    state:State;

    constructor(props:Props) {
        super(props);
        this.state = { stepMetadata: null };
    }
    
    componentWillMount() {
        pipelineStepListStore.getStepListing(stepMetadata => {
            this.setState({stepMetadata: stepMetadata});
        });
    }

    render() {
        const { step } = this.props;
        const { stepMetadata } = this.state;

        if (!step || !stepMetadata) {
            return null;
        }
        
        const thisMeta = stepMetadata.filter(md => md.functionName === step.type)[0];

        return (
            <div className="pipeline-editor-step-generic">
                {thisMeta.properties.map(p => {
                    const val = step.data[p.name];
                    return (
                        <div key={p.name}>
                            <label>{p.name}</label>
                            <input defaultValue={val} onChange={e => step.data[p.name] = e.target.value} />
                        </div>
                    );
                })}
            </div>
        );
    }
}

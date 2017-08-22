import React, { Component, PropTypes } from 'react';
import debounce from 'lodash.debounce';

export default class PipelineScriptStepEditor extends React.Component {
    textChanged = debounce(value => {
        this.props.step.data.scriptBlock = value;
        this.props.onChange(this.props.step);
    }, 300);

    render() {
        const { step } = this.props;
        return (<textarea className="editor-step-detail-script"
                  defaultValue={step.data.scriptBlock}
                  onChange={(e) => this.textChanged(e.target.value)} />);
    }
}

PipelineScriptStepEditor.propTypes = {
    step: PropTypes.any,
    onChange: PropTypes.func,
};

PipelineScriptStepEditor.stepType = 'script'; // FIXME do this a better way

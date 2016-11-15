import React, { Component, PropTypes } from 'react';

export default class ScriptStepEditor extends React.Component {
    textChanged(event) {
        this.props.step.script = event.target.value;
        this.props.onChange(this.props.step);
    }

    render() {
        const { step } = this.props;
        return <textarea className="editor-step-detail-script"
                  defaultValue={step.data.script}
                  onChange={(e) => this.textChanged(e)} />
    }
}

ScriptStepEditor.propTypes = {
    step: PropTypes.any,
    onChange: PropTypes.func,
};

ScriptStepEditor.stepType = 'sh'; // FIXME do this a better way

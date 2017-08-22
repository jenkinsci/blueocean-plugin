import React, { Component, PropTypes } from 'react';
import debounce from 'lodash.debounce';

export default class ScriptStepEditor extends React.Component {
    textChanged = debounce(script => {
        this.props.step.data.script = script;
        this.props.onChange(this.props.step);
    }, 300);

    render() {
        const { step } = this.props;
        return (<textarea className="editor-step-detail-script"
                  defaultValue={step.data.script}
                  onChange={(e) => this.textChanged(e.target.value)} />);
    }
}

ScriptStepEditor.propTypes = {
    step: PropTypes.any,
    onChange: PropTypes.func,
};

ScriptStepEditor.stepType = 'sh'; // FIXME do this a better way

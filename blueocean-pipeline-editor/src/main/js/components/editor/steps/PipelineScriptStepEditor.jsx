import React, { Component, PropTypes } from 'react';
import debounce from 'lodash.debounce';
import { getArg, setArg } from '../../../services/PipelineMetadataService';

export default class PipelineScriptStepEditor extends React.Component {
    textChanged = debounce(value => {
        setArg(this.props.step, 'scriptBlock', value);
        this.props.onChange(this.props.step);
    }, 300);

    render() {
        const { step } = this.props;
        return (<textarea className="editor-step-detail-script"
                  defaultValue={getArg(step, 'scriptBlock').value}
                  onChange={(e) => this.textChanged(e.target.value)} />);
    }
}

PipelineScriptStepEditor.propTypes = {
    step: PropTypes.any,
    onChange: PropTypes.func,
};

PipelineScriptStepEditor.stepType = 'script'; // FIXME do this a better way

import React, { Component, PropTypes } from 'react';
import debounce from 'lodash.debounce';
import { getArg, setArg } from '../../../services/PipelineMetadataService';

export default class BuildStepEditor extends React.Component {
    textChanged = debounce(script => {
        setArg(this.props.step, 'script', script);
        this.props.onChange(this.props.step);
    }, 300);

    render() {
        const { step } = this.props;
        return (<textarea className="editor-step-detail-script"
                  defaultValue={getArg(this.props.step, 'script').value}
                  onChange={(e) => this.textChanged(e.target.value)} />);
    }
}

BuildStepEditor.propTypes = {
    step: PropTypes.any,
    onChange: PropTypes.func,
};

BuildStepEditor.stepType = '_build'; // FIXME do this a better way

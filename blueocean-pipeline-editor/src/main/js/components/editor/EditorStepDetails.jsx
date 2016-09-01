// @flow

import React, {Component, PropTypes} from 'react';

import type {StepInfo} from './common';

type Props = {
    step?: ?StepInfo,
    onDataChange?: (newValue:any) => void
}

export class EditorStepDetails extends Component {

    props:Props;

    state:{
        body: string
    };

    static defaultProps = {};

    constructor(props:Props) {
        super(props);

        const step = props.step;

        this.state = {
            body: step ? step.data : ""
        };
    }

    componentWillReceiveProps(nextProps:Props) {
        if (nextProps.step !== this.props.step) {
            const body = nextProps.step ? nextProps.step.data : "";
            this.setState({body});
        }
    }

    textChanged(event: *) {
        // TODO: Look up the correct type for event in this case
        this.setState({body: event.target.value});
    }

    commitValue() {
        const {onDataChange} = this.props;
        if (onDataChange) {
            onDataChange(this.state.body);
        }
    }

    render() {

        const {step} = this.props;
        const {body} = this.state;

        if (!step) {
            return (
                <div className="editor-step-detail no-step">
                    <p>Select or create a step</p>
                </div>
            );
        }

        return (
            <div className="editor-step-detail">
                <h4 className="editor-step-detail-label">{step.label}</h4>
                <textarea className="editor-step-detail-script"
                          value={body || ""}
                          onChange={(e) => this.textChanged(e)}
                          onBlur={(e) => this.commitValue()}/>
            </div>
        );
    }
}

// @flow

import React, { Component, PropTypes } from 'react';
import pipelineMetadataService from '../../services/PipelineMetadataService';
import type { PipelineInfo, StageInfo } from '../../services/PipelineStore';
import { Dropdown } from '@jenkins-cd/design-language';
import { Split } from './Split';
import { TextInput } from '@jenkins-cd/design-language';

type Props = {
    node: PipelineInfo|StageInfo,
    onChange: (environment: Object[]) => any,
};

type State = {
};

type DefaultProps = typeof EnvironmentConfiguration.defaultProps;

export class EnvironmentConfiguration extends Component<DefaultProps, Props, State> {
    props:Props;
    state:State;

    constructor(props:Props) {
        super(props);
        this.state = { environments: null, selectedEnvironment: props.node && props.node.environment };
    }

    componentWillMount() {
    }

    componentDidMount() {
    }

    addEnvironmentEntry() {
        if (!this.props.node.environment) {
            this.props.node.environment = [];
        }
        this.props.node.environment.push({
            key: '',
            value: {
                isLiteral: true,
                value: '',
            }
        });
        this.props.onChange();
    }

    removeEnviromentEntry(entry) {
        this.props.node.environment = this.props.node.environment.filter(e => e != entry);
        this.props.onChange();
    }

    render() {
        const { node } = this.props;

        if (!node) {
            return null;
        }

        return (<div className="environment-select">
            <h4>Environment Configuration</h4>
            <Split>
                <span>Name</span>
                <span>Value</span>
                <button className="add" onClick={e => this.addEnvironmentEntry()}>Add</button>
            </Split>
            {node.environment && node.environment.map((env, idx) => <div className="environment">
                <Split>
                    <TextInput key={idx} defaultValue={env.key} onChange={val => { env.key = val; this.props.onChange(); }} />
                    <TextInput key={'val'+idx} defaultValue={env.value.value} onChange={val => { env.value.value = val; this.props.onChange(); }} />
                    <button className="remove" onClick={e => { this.removeEnviromentEntry(env); this.props.onChange(); }}>Remove</button>
                </Split>
            </div>)}
        </div>);
    }
}

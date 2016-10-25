/**
 * Created by cmeyers on 10/21/16.
 */

import React from 'react';

import MultiStepFlow from '../MultiStepFlow';

import GitStatus from './GitCreationStatus';
import GitCreationApi from './GitCreationApi';
import GitCreationManager from './GitCreationManager';

import ConnectStep from './ConnectStep';
import CompletedStep from './CompletedStep';

export default class GitCreationFlow extends React.Component {

    constructor(props) {
        super(props);

        this._api = new GitCreationApi();
        this._manager = new GitCreationManager(
            this._api,
            (status) => this._onStatusChanged(status),
        );

        this.state = {
            flowStatus: GitStatus.NOT_STARTED,
        };
    }

    _onStatusChanged(flowStatus) {
        this.setState({
            flowStatus,
        });
    }

    render() {
        return (
            <MultiStepFlow {...this.props}>
                <ConnectStep manager={this._manager} />
                <CompletedStep flowStatus={this.state.flowStatus} />
            </MultiStepFlow>
        );
    }
}

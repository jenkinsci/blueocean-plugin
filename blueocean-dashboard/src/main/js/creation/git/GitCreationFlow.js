/**
 * Created by cmeyers on 10/21/16.
 */

import React from 'react';

import MultiStepFlow from '../MultiStepFlow';

import { credentialsManager } from '../credentials';

import GitStatus from './GitCreationStatus';
import GitCreationApi from './GitCreationApi';
import GitCreationManager from './GitCreationManager';

import ConnectStep from './GitConnectStep';
import CompletedStep from './GitCompletedStep';

/**
 * Handles the overall two-step of creating a pipeline from a Git repo.
 * Updates the status of the "CompletedStep" as the manager reports back status.
 */
export default class GitCreationFlow extends React.Component {

    constructor(props) {
        super(props);

        this._api = new GitCreationApi();
        this._manager = new GitCreationManager(
            this._api,
            (status) => this._onStatusChanged(status),
        );

        this.credsManager = credentialsManager;

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
                <ConnectStep manager={this._manager} credentialsManager={this.credsManager} />
                <CompletedStep manager={this._manager} flowStatus={this.state.flowStatus} />
            </MultiStepFlow>
        );
    }
}

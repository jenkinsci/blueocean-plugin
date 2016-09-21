/**
 * Created by cmeyers on 8/30/16.
 */

import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';

import { capable } from '../index';
import { RunApi as runApi } from '../index';
import { ToastUtils } from '../index';

const stopProp = (event) => {
    event.stopPropagation();
};

const CAPABILITY_MULTIBRANCH_PIPELINE = 'io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline';
const CAPABILITY_MULTIBRANCH_BRANCH = 'io.jenkins.blueocean.rest.model.BlueBranch';
const CAPABILITY_SIMPLE_PIPELINE = 'org.jenkinsci.plugins.workflow.job.WorkflowJob';
const PIPELINE_CAPABILITIES = [CAPABILITY_SIMPLE_PIPELINE, CAPABILITY_MULTIBRANCH_PIPELINE, CAPABILITY_MULTIBRANCH_BRANCH];

/**
 * ReplayButton allows a pipeline or branch to be re-run when in a failure state.
 */
export class ReplayButton extends Component {

    constructor(props) {
        super(props);

        this.state = {
            replaying: false,
        };
    }

    _onReplayClick() {
        if (this.state.replaying) {
            return;
        }

        this.setState({
            replaying: true,
        });

        runApi.replayRun(this.props.latestRun)
            .then(runInfo => ToastUtils.createRunStartedToast(this.props.runnable, runInfo, this.props.onNavigation))
            .then(runDetailsUrl => this._afterReplayStarted(runDetailsUrl));
    }

    _afterReplayStarted(runDetailsUrl) {
        if (this.props.autoNavigate && this.props.onNavigation) {
            this.props.onNavigation(runDetailsUrl);
        }
    }

    render() {
        const outerClass = this.props.className ? this.props.className : '';
        const outerClassNames = outerClass.split(' ');
        const innerButtonClass = outerClassNames.indexOf('icon-button') === -1 ? 'btn inverse' : '';

        const status = this.props.latestRun ? this.props.latestRun.result : '';
        const failedStatus = status && (status.toLowerCase() === 'failure' || status.toLowerCase() === 'aborted');
        const isPipeline = capable(this.props.runnable, PIPELINE_CAPABILITIES);

        const replayLabel = 'Re-run';

        if (!isPipeline || !failedStatus) {
            return null;
        }

        return (
            <div className={`replay-button-component ${outerClass}`} onClick={(event => stopProp(event))}>
                <a className={`replay-button ${innerButtonClass}`} title={replayLabel} onClick={() => this._onReplayClick()}>
                    <Icon size={24} icon="replay" />
                    <span className="button-label">{replayLabel}</span>
                </a>
            </div>
        );
    }
}

ReplayButton.propTypes = {
    className: PropTypes.string,
    runnable: PropTypes.object,
    latestRun: PropTypes.object,
    autoNavigate: PropTypes.bool,
    onNavigation: PropTypes.func,
};

/**
 * Created by cmeyers on 8/30/16.
 */

import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';

import { capable, RunApi as runApi, ToastUtils } from '../index';
import Security from '../security';
import i18nTranslator from '../i18n/i18n';

const { permit } = Security;
const translate = i18nTranslator('blueocean-web');

const stopProp = (event) => {
    event.stopPropagation();
};

const CAPABILITY_MULTIBRANCH_PIPELINE = 'io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline';
const CAPABILITY_MULTIBRANCH_BRANCH = 'io.jenkins.blueocean.rest.model.BlueBranch';
const CAPABILITY_SIMPLE_PIPELINE = 'org.jenkinsci.plugins.workflow.job.WorkflowJob';
const PIPELINE_CAPABILITIES = [CAPABILITY_SIMPLE_PIPELINE, CAPABILITY_MULTIBRANCH_PIPELINE, CAPABILITY_MULTIBRANCH_BRANCH];

function isRunFinished(run) {
    return !!(run && run.state === 'FINISHED');
}

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

    componentWillReceiveProps(nextProps) {
        const statusChanged = isRunFinished(this.props.latestRun) !== isRunFinished(nextProps.latestRun);

        if (statusChanged) {
            this.setState({
                replaying: false,
            });
        }
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
        if (!this.props.runnable || !this.props.latestRun) {
            return null;
        }

        const outerClass = this.props.className ? this.props.className : '';
        const outerClassNames = outerClass.split(' ');
        const innerButtonClass = outerClassNames.indexOf('icon-button') === -1 ? 'btn inverse' : '';

        const isFinished = isRunFinished(this.props.latestRun);
        const isPipeline = capable(this.props.runnable, PIPELINE_CAPABILITIES);
        const hasPermission = permit(this.props.runnable).start();

        const replayLabel = translate('toast.re-run', { defaultValue: 'Re-run' });

        if (!isFinished || !isPipeline || !hasPermission) {
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

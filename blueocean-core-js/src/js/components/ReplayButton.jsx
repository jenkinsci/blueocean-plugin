/**
 * Created by cmeyers on 8/30/16.
 */

import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';

import { RunApi as runApi, ToastUtils } from '../index';
import Security from '../security';
import { stopProp } from '../utils';
import i18nTranslator from '../i18n/i18n';

const { permit } = Security;
const translate = i18nTranslator('blueocean-web');

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
            .then(run => ToastUtils.createRunStartedToast(this.props.runnable, run, this.props.onNavigation))
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
        const isReplayable = this.props.latestRun.replayable;
        const hasPermission = permit(this.props.runnable).start();

        const replayLabel = translate('run.rerun');

        if (!isFinished || !isReplayable || !hasPermission) {
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

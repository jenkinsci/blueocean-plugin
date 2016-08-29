/**
 * Created by cmeyers on 8/26/16.
 */

import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';

import { RunApi as runApi } from '../';
import { SseBus as sseBus } from '../';
import { ToastService as toastService } from '../';
import { buildRunDetailsUrl } from '../UrlBuilder';

const stopProp = (event) => {
    event.stopPropagation();
};

/**
 * Run Buttons allows a pipeline or branch to be run and also be stopped thereafter.
 */
export class RunButton extends Component {

    constructor(props) {
        super(props);

        this.subscriptionId = null;

        this.state = {
            running: false,
            stopping: false,
        };
    }

    componentWillReceiveProps(nextProps) {
        this._updateState(nextProps);
    }

    componentDidMount() {
        this.subscriptionId = sseBus.subscribeToJob(
            (runData, event) => this._onJobEvent(runData, event),
            (event) => this._filterJob(event)
        );
    }

    componentWillUnmount() {
        if (this.subscriptionId) {
            sseBus.unsubscribe(this.subscriptionId);
        }
    }

    _updateState(nextProps) {
        const oldStatus = this.props.latestRun && this.props.latestRun.state || '';
        const newStatus = nextProps.latestRun && nextProps.latestRun.state || '';

        // if the state of the run changed, then assume it's no longer trying to stop
        if (oldStatus !== newStatus) {
            this.setState({
                stopping: false,
            });
        }
    }

    _onJobEvent(runData, event) {
        const name = decodeURIComponent(
            event.job_ismultibranch ? event.blueocean_job_branch_name : event.blueocean_job_pipeline_name
        );
        const runId = event.jenkins_object_id;

        if (event.jenkins_event === 'job_run_started') {
            const runDetailsUrl = buildRunDetailsUrl(runData);

            toastService.newToast({
                text: `Started "${name}" #${runId}`,
                action: 'Open',
                onActionClick: () => {
                    if (this.props.onNavigation) {
                        this.props.onNavigation(runDetailsUrl);
                    }
                },
            });
        } else if (event.jenkins_event === 'job_run_ended' && runData.result === 'ABORTED') {
            toastService.newToast({
                text: `Stopped "${name}" #${runId}`,
            });
        }

    }

    _filterJob(event) {
        return event.blueocean_job_rest_url === this.props.runnable._links.self.href;
    }

    _onRunClick() {
        runApi.startRun(this.props.runnable);

        const name = this.props.runnable.name;

        toastService.newToast({
            text: `Queued "${name}"`,
        });
    }

    _onStopClick() {
        if (this.state.stopping) {
            return;
        }

        this.setState({
            stopping: true,
        });

        runApi.stopRun(this.props.latestRun);

        const name = this.props.runnable.name;
        const runId = this.props.latestRun.id;

        toastService.newToast({
            text: `Stopping "${name}" #${runId}...`,
        });
    }

    render() {
        const outerClass = this.props.className ? this.props.className : '';
        const outerClassNames = outerClass.split(' ');
        const innerButtonClass = outerClassNames.indexOf('icon-button') === -1 ? 'btn inverse' : '';
        const stopClass = this.state.stopping ? 'stopping' : '';

        const status = this.props.latestRun ? this.props.latestRun.state : '';
        const runningStatus = status && (status.toLowerCase() === 'running' || status.toLowerCase() === 'queued');

        const runLabel = this.props.runText || 'Run';
        const stopLabel = this.state.stopping ? 'Stopping...' : 'Stop';

        return (
            <div className={`run-button-component ${outerClass}`} onClick={(event => stopProp(event))}>
                { !runningStatus &&
                <a className={`run-button ${innerButtonClass}`} title={runLabel} onClick={() => this._onRunClick()}>
                    <Icon size={24} icon="play_circle_outline" />
                    <span className="button-label">{runLabel}</span>
                </a>
                }

                { runningStatus &&
                <a className={`stop-button ${innerButtonClass} ${stopClass}`} title={stopLabel} onClick={() => this._onStopClick()}>
                    <div className="btn-icon"></div>
                    <span className="button-label">{stopLabel}</span>
                </a>
                }
            </div>
        );
    }
}

RunButton.propTypes = {
    className: PropTypes.string,
    runnable: PropTypes.object,
    latestRun: PropTypes.object,
    onNavigation: PropTypes.func,
    runText: PropTypes.string,
};

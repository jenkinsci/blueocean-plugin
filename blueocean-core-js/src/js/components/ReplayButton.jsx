/**
 * Created by cmeyers on 8/30/16.
 */

import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';

import { RunApi as runApi } from '../';
import { ToastService as toastService } from '../';
import { buildRunDetailsUrl } from '../UrlBuilder';

const stopProp = (event) => {
    event.stopPropagation();
};

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
            .then((runInfo) => this._nextAction(runInfo));
    }

    _nextAction(runInfo) {
        const runDetailsUrl = buildRunDetailsUrl(runInfo._links.self.href);

        if (this.props.autoNavigate && this.props.onNavigation) {
            this.props.onNavigation(runDetailsUrl);
        } else {
            const name = this.props.runnable.name;
            const runId = runInfo.id;

            toastService.newToast({
                text: `Queued "${name}" #${runId}`,
                action: 'Open',
                onActionClick: () => {
                    if (this.props.onNavigation) {
                        this.props.onNavigation(runDetailsUrl);
                    }
                },
            });
        }
    }

    render() {
        const outerClass = this.props.className ? this.props.className : '';
        const outerClassNames = outerClass.split(' ');
        const innerButtonClass = outerClassNames.indexOf('icon-button') === -1 ? 'btn inverse' : '';

        const status = this.props.latestRun ? this.props.latestRun.result : '';
        const failedStatus = status && (status.toLowerCase() === 'failed' || status.toLowerCase() === 'aborted');

        const replayLabel = 'Re-run';

        return (
            <div className={`replay-button-component ${outerClass}`} onClick={(event => stopProp(event))}>
                { failedStatus &&
                <a className={`replay-button ${innerButtonClass}`} title={replayLabel} onClick={() => this._onReplayClick()}>
                    <Icon size={24} icon="replay" />
                    <span className="button-label">{replayLabel}</span>
                </a>
                }
            </div>
        );
    }
}

ReplayButton.propTypes = {
    className: PropTypes.string,
    runnable: PropTypes.object,
    latestRun: PropTypes.object,
    autoNavigate: PropTypes.object,
    onNavigation: PropTypes.func,
};

/**
 * Created by cmeyers on 6/28/16.
 */
import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { Icon } from 'react-material-icons-blue';
import { Favorite, LiveStatusIndicator } from '@jenkins-cd/design-language';

const stopProp = (event) => {
    event.stopPropagation();
};

/**
 * PipelineCard displays an informational card about a Pipeline and its status.
 *
 * Properties:
 * router: instance of RouterContext
 * item: pipeline or branch
 * capabilities: array of capability strings for the item
 * status: 'result' or 'status' value e.g. 'success', 'failure', etc.
 * estimatedDuration: time in millis over which the progress indicator will update.
 * startTime: ISO-8601 string indicating when tracking of progress begins from.
 * organization: name of org
 * pipeline: name of pipeline
 * branch: name of branch
 * commitId: ID of commit
 * favorite: whether or not the pipeline is favorited
 * onRunClick: callback invoked when 'Run Again' is clicked
 * onFavoriteToggle: callback invokved when favorite checkbox is toggled.
 */
export class PipelineCard extends Component {

    static _getBackgroundClass(status) {
        return status && status.length > 0 ?
            `${status.toLowerCase()}-bg-lite` :
            'unknown-bg-lite';
    }

    constructor(props) {
        super(props);

        this.state = {
            favorite: false,
        };
    }

    componentWillMount() {
        this._updateState(this.props);
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.favorite !== nextProps.favorite) {
            this._updateState(nextProps);
        }
    }

    _updateState(props) {
        this.setState({
            favorite: props.favorite,
        });
    }

    _onRunClick() {
        if (this.props.onRunClick) {
            this.props.onRunClick(this.props.item);
        }
    }

    _onRunAgainClick() {
        if (this.props.onRunAgainClick) {
            this.props.onRunAgainClick(this.props.item);
        }
    }

    _onFavoriteToggle() {
        const value = !this.state.favorite;
        this.setState({
            favorite: value,
        });

        if (this.props.onFavoriteToggle) {
            this.props.onFavoriteToggle(value);
        }
    }

    render() {
        const { capabilities, status, commitId, startTime, estimatedDuration } = this.props;
        const bgClass = PipelineCard._getBackgroundClass(status);
        const hasRunningStatus = !status || (status.toLowerCase() !== 'running' && status.toLowerCase() !== 'queued');
        const hasFailedStatus = status && (status.toLowerCase() === 'failure' || status.toLowerCase() === 'aborted');
        const isPipeline = capabilities && capabilities.indexOf('org.jenkinsci.plugins.workflow.job.WorkflowJob') >= 0;
        const commitText = commitId ? commitId.substr(0, 7) : '';

        const activityUrl = `/organizations/${encodeURIComponent(this.props.organization)}/` +
        `${encodeURIComponent(this.props.fullName)}/activity`;

        const navigateToRunDetails = () => {
            const runUrl = `/organizations/${encodeURIComponent(this.props.organization)}/` +
                `${encodeURIComponent(this.props.fullName)}/detail/` +
                `${this.props.branch || this.props.pipeline}/${encodeURIComponent(this.props.runId)}/pipeline`;

            this.props.router.push({
                pathname: runUrl,
            });
        };

        return (
            <div className={`pipeline-card ${bgClass}`} onClick={() => navigateToRunDetails()}>
                <LiveStatusIndicator
                  result={status} startTime={startTime} estimatedDuration={estimatedDuration}
                  width={'20px'} height={'20px'} noBackground
                />

                <span className="name">
                    <Link to={activityUrl} onClick={(event) => stopProp(event)}>
                        {this.props.organization} / <span title={this.props.fullName}>{this.props.pipeline}</span>
                    </Link>
                </span>

                { this.props.branch ?
                <span className="branch">
                    <span className="octicon octicon-git-branch"></span>
                    <span className="branchText">{decodeURIComponent(this.props.branch)}</span>
                </span>
                :
                <span className="branch"></span>
                }

                { commitId ?
                <span className="commit">
                    <span className="octicon octicon-git-commit"></span>
                    <pre className="commitId">#{commitText}</pre>
                </span>
                :
                <span className="commit"></span>
                }

                <span className="actions">
                    { hasFailedStatus && isPipeline &&
                    <a className="action-item rerun" title="Run Again" onClick={(event) => {stopProp(event); this._onRunAgainClick();}}>
                        <Icon size={24} icon="replay" />
                    </a>
                    }

                    { hasRunningStatus &&
                    <a className="action-item run" title="Run" onClick={(event) => {stopProp(event); this._onRunClick();}}>
                        <Icon size={24} icon="play_circle_outline" />
                    </a>
                    }

                    <Favorite checked={this.state.favorite} className="dark-white"
                      onToggle={() => this._onFavoriteToggle()}
                    />
                </span>
            </div>
        );
    }
}

PipelineCard.propTypes = {
    router: PropTypes.object,
    item: PropTypes.object,
    capabilities: PropTypes.array,
    status: PropTypes.string,
    startTime: PropTypes.string,
    estimatedDuration: PropTypes.number,
    organization: PropTypes.string,
    fullName: PropTypes.string,
    pipeline: PropTypes.string,
    branch: PropTypes.string,
    commitId: PropTypes.string,
    runId: PropTypes.string,
    favorite: PropTypes.bool,
    onRunClick: PropTypes.func,
    onRunAgainClick: PropTypes.func,
    onFavoriteToggle: PropTypes.func,
};

PipelineCard.defaultProps = {
    favorite: false,
};

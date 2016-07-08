/**
 * Created by cmeyers on 6/28/16.
 */
import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';
import { Favorite, LiveStatusIndicator } from '@jenkins-cd/design-language';

/**
 * PipelineCard displays an informational card about a Pipeline and its status.
 *
 * Properties:
 * status: 'result' or 'status' value e.g. 'success', 'failure', etc.
 * percentage: for status=running, the percent complete
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
            this.props.onRunClick();
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
        const { status, commitId } = this.props;
        const bgClass = PipelineCard._getBackgroundClass(status);
        const showRun = status && (status.toLowerCase() === 'failure' || status.toLowerCase() === 'aborted');
        const commitText = commitId ? commitId.substr(0, 7) : '';

        return (
            <div className={`pipeline-card ${bgClass}`}>
                <LiveStatusIndicator result={this.props.status} width={'20px'} height={'20px'} noBackground />

                <span className="name">
                    {this.props.organization} / {this.props.pipeline}
                </span>

                { this.props.branch &&
                <span className="branch">
                    <span className="octicon octicon-git-branch"></span>
                    <span className="branchText">{this.props.branch}</span>
                </span>
                }

                <span className="commit">
                    <span className="octicon octicon-git-commit"></span>
                    <pre className="commitId">#{commitText}</pre>
                </span>

                <span className="actions">
                    { showRun &&
                    <a className="run" title="Run Again" onClick={() => this._onRunClick()}>
                        <Icon size={24} icon="replay" />
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
    status: PropTypes.string,
    percentage: PropTypes.number, // TODO: might need startTime and estimatedDuration
    organization: PropTypes.string,
    pipeline: PropTypes.string,
    branch: PropTypes.string,
    commitId: PropTypes.string,
    favorite: PropTypes.bool,
    onRunClick: PropTypes.func,
    onFavoriteToggle: PropTypes.func,
};

PipelineCard.defaultProps = {
    favorite: false,
};

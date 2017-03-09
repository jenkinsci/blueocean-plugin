/**
 * Created by cmeyers on 6/28/16.
 */
import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { capable, UrlBuilder, AppConfig, RunButton, ReplayButton, LiveStatusIndicator } from '@jenkins-cd/blueocean-core-js';
import { ExpandablePath, Favorite } from '@jenkins-cd/design-language';

const stopProp = (event) => {
    event.stopPropagation();
};

const BRANCH_CAPABILITY = 'io.jenkins.blueocean.rest.model.BlueBranch';

/**
 * Extract elements from a path string deliminted with forward slashes
 * @param path
 * @param begin
 * @param end
 * @returns {string}
 */
function extractPath(path, begin, end) {
    try {
        return path.split('/').slice(begin, end).join('/');
    } catch (error) {
        return path;
    }
}

/**
 * Takes a pipeline/branch object and returns the fullName, pipelineName and branchName components
 * @param {object} pipeline
 * @param {boolean} isBranch
 * @returns {{pipelineName: string, fullName: string, branchName: string}}
 * @private
 */
function extractNames(pipeline, isBranch) {
    let fullName = null;
    let pipelineName = null;
    let branchName = null;

    if (isBranch) {
        // pipeline.fullName is in the form folder1/folder2/pipeline/branch ...
        // extract "pipeline"
        pipelineName = extractPath(pipeline.fullName, -2, -1);
        // extract everything up to "branch"
        fullName = extractPath(pipeline.fullName, 0, -1);
        branchName = pipeline.name;
    } else {
        pipelineName = pipeline.name;
        fullName = pipeline.fullName;
    }

    return {
        fullName, pipelineName, branchName,
    };
}

/**
 * PipelineCard displays an informational card about a Pipeline and its status.
 *
 * Properties:
 * router: instance of RouterContext
 * item: pipeline or branch
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
            stopping: false,
        };
    }

    componentWillMount() {
        this._updateState(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._updateState(nextProps);
    }

    _navigateToRunDetails() {
        const runUrl = UrlBuilder.buildRunDetailsUrl(this.props.runnable.latestRun);

        this.props.router.push({
            pathname: runUrl,
        });
    }

    _updateState(props) {
        this.setState({
            favorite: props.favorite,
            stopping: false,
        });
    }

    _onRunDetails(url) {
        this.props.router.push(url);
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
        if (!this.props.runnable) {
            return null;
        }
        const runnableItem = this.props.runnable;
        const latestRun = this.props.runnable.latestRun;

        const isBranch = capable(runnableItem, BRANCH_CAPABILITY);
        const names = extractNames(runnableItem, isBranch);
        const organization = runnableItem.organization;
        const fullDisplayName = isBranch ?
            runnableItem.fullDisplayName.split('/').slice(0, -1).join('/') :
            runnableItem.fullDisplayName;

        let status;
        let startTime = null;
        let estimatedDuration = null;
        let commitId = null;

        if (latestRun) {
            status = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
            startTime = latestRun.startTime;
            estimatedDuration = latestRun.estimatedDurationInMillis;
            commitId = latestRun.commitId;
        } else {
            status = 'NOT_BUILT';
        }

        const bgClass = PipelineCard._getBackgroundClass(status);
        const commitText = commitId ? commitId.substr(0, 7) : '';

        const activityUrl = `/organizations/${encodeURIComponent(organization)}/` +
        `${encodeURIComponent(names.fullName)}/activity`;

        let displayPath;
        if (AppConfig.showOrg()) {
            displayPath = `${organization}/${fullDisplayName}`;
        } else {
            displayPath = fullDisplayName;
        }

        return (
            <PipelineCardRenderer bgClass={bgClass}
                                  onClickMain={() => this._navigateToRunDetails()}
                                  status={status}
                                  startTime={startTime}
                                  estimatedDuration={estimatedDuration}
                                  activityUrl={activityUrl}
                                  displayPath={displayPath}
                                  branchText={isBranch && decodeURIComponent(names.branchName)}
                                  commitText={commitId && commitText}
                                  favoriteChecked={this.state.favorite}
                                  onFavoriteToggle={() => this._onFavoriteToggle()}
                                  runnableItem={runnableItem}
                                  latestRun={latestRun}
                                  onRunDetails={url => this._onRunDetails(url)} />
            );
    }
}

PipelineCard.propTypes = {
    router: PropTypes.object,
    runnable: PropTypes.object,
    favorite: PropTypes.bool,
    onFavoriteToggle: PropTypes.func,
};

PipelineCard.defaultProps = {
    favorite: false,
};

export const PipelineCardRenderer = (props) => {

    const {
        bgClass,
        onClickMain,
        status,
        startTime,
        estimatedDuration,
        activityUrl,
        displayPath,
        branchText,
        commitText,
        favoriteChecked,
        onFavoriteToggle,
        runnableItem,
        latestRun,
        onRunDetails,
    } = props;

    return (
        <div className={`pipeline-card ${bgClass}`} onClick={onClickMain}>
            <LiveStatusIndicator result={status}
                                 startTime={startTime}
                                 estimatedDuration={estimatedDuration}
                                 width={'20px'}
                                 height={'20px'}
                                 noBackground />

            <span className="name">
                <Link to={activityUrl} onClick={stopProp}>
                    <ExpandablePath path={displayPath} className="dark-theme" />
                </Link>
            </span>

            { branchText ?
                <span className="branch">
                    <span className="octicon octicon-git-branch"></span>
                    <span className="branchText">{branchText}</span>
                </span>
                :
                <span className="branch"></span>
            }

            { commitText ?
                <span className="commit">
                    <span className="octicon octicon-git-commit"></span>
                    <pre className="commitId">&#35;{commitText}</pre>
                </span>
                :
                <span className="commit"></span>
            }

            <span className="actions" onClick={stopProp}>
                <Favorite checked={favoriteChecked}
                          className="dark-white"
                          onToggle={onFavoriteToggle} />

                <RunButton className="icon-button dark"
                           runnable={runnableItem}
                           latestRun={latestRun}
                           onNavigation={onRunDetails} />

                <ReplayButton className="icon-button dark"
                              runnable={runnableItem}
                              latestRun={latestRun}
                              onNavigation={onRunDetails} />
            </span>
        </div>
    );
};

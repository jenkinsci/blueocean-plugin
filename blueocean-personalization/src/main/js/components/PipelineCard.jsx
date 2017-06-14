/**
 * Created by cmeyers on 6/28/16.
 */
import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import { logging, capable, UrlBuilder, AppConfig, RunButton, ReplayButton, LiveStatusIndicator } from '@jenkins-cd/blueocean-core-js';
import { ExpandablePath, Favorite, ReadableDate } from '@jenkins-cd/design-language';
import { Icon } from '@jenkins-cd/react-material-icons';
import moment from 'moment';

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

    _updateState(props) {
        this.setState({
            favorite: props.favorite,
            stopping: false,
        });
    }

    _navigateToRunDetails = () => {
        const runUrl = UrlBuilder.buildRunDetailsUrl(this.props.runnable.latestRun);

        this.props.router.push({
            pathname: runUrl,
        });
    };

    _onRunDetails = url => {
        this.props.router.push(url);
    };

    _onFavoriteToggle = () => {
        const value = !this.state.favorite;
        this.setState({
            favorite: value,
        });

        if (this.props.onFavoriteToggle) {
            this.props.onFavoriteToggle(value);
        }
    };

    render() {
        if (!this.props.runnable) {
            return null;
        }

        const {
            t,
            locale,
            runnable,
        } = this.props;

        // Required props
        if (!t) {
            PipelineCard.logger.error('PipelineCard requires translate function as "t" prop.');
            return null;
        }

        const latestRun = runnable.latestRun;

        const isBranch = capable(runnable, BRANCH_CAPABILITY);
        const names = extractNames(runnable, isBranch);
        const organizationName = runnable.organization;
        const organizationDisplayName = organizationName === AppConfig.getOrganizationName() ? AppConfig.getOrganizationDisplayName() : organizationName;
        const fullDisplayName = isBranch ?
            runnable.fullDisplayName.split('/').slice(0, -1).join('/') :
            runnable.fullDisplayName;

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

        const commitText = commitId ? commitId.substr(0, 7) : '';

        const activityUrl = `/organizations/${encodeURIComponent(organizationName)}/` +
        `${encodeURIComponent(names.fullName)}/activity`;

        let displayPath;
        if (AppConfig.showOrg()) {
            displayPath = `${organizationDisplayName}/${fullDisplayName}`;
        } else {
            displayPath = fullDisplayName;
        }

        // Calculate datetime of last run

        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();

        const dateFormatShort = t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' });
        const dateFormatLong = t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' });

        let runDateTime = null;

        if (latestRun) {
            // We'll pick the latest time we have. Completion, start, or enque in that order
            const serverTimeISO = latestRun.endTime || latestRun.startTime || latestRun.enQueueTime;

            if (serverTimeISO) {
                // Trim off server TZ skew
                const serverTimeMoment = moment(serverTimeISO);
                if (skewMillis < 0) {
                    serverTimeMoment.add({ milliseconds: Math.abs(skewMillis) });
                } else if (skewMillis > 0) {
                    serverTimeMoment.subtract({ milliseconds: skewMillis });
                }
                runDateTime = serverTimeMoment.toJSON();
            }
        }

        let timeText = runDateTime && (
            <ReadableDate
                date={ runDateTime }
                liveUpdate
                locale={ locale }
                shortFormat={ dateFormatShort }
                longFormat={ dateFormatLong }
            />
        );

        return (
            <PipelineCardRenderer onClickMain={this._navigateToRunDetails}
                                  status={status}
                                  startTime={startTime}
                                  estimatedDuration={estimatedDuration}
                                  activityUrl={activityUrl}
                                  displayPath={displayPath}
                                  branchText={isBranch && decodeURIComponent(names.branchName)}
                                  commitText={commitId && commitText}
                                  timeText={timeText}
                                  favoriteChecked={this.state.favorite}
                                  onFavoriteToggle={this._onFavoriteToggle}
                                  runnableItem={runnable}
                                  latestRun={latestRun}
                                  onRunDetails={this._onRunDetails}
            />
        );
    }
}

PipelineCard.propTypes = {
    router: PropTypes.object,
    runnable: PropTypes.object,
    favorite: PropTypes.bool,
    onFavoriteToggle: PropTypes.func,
    locale: PropTypes.string.isRequired,
    t: PropTypes.func.isRequired,
};

PipelineCard.defaultProps = {
    favorite: false,
};

PipelineCard.contextTypes = {
    config: PropTypes.object.isRequired,
};

PipelineCard.logger = logging.logger('io.jenkins.blueocean.personalization.PipelineCard');

export function PipelineCardRenderer(props) {
    const {
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
        timeText,
    } = props;

    const bgClass = PipelineCard._getBackgroundClass(status);

    return (
        <div className={`pipeline-card ${bgClass}`} onClick={onClickMain}>
            <LiveStatusIndicator result={status}
                                 startTime={startTime}
                                 estimatedDuration={estimatedDuration}
                                 width={'20px'}
                                 height={'20px'}
                                 noBackground
            />

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

            { timeText ?
                <span className="time">
                    <Icon size={ 16 } icon="access_time" style={ { fill: '#fff' } } />
                    <span className="timeText">{timeText}</span>
                </span>
                :
                <span className="time"></span>
            }

            <span className="actions" onClick={stopProp}>
                <ReplayButton className="icon-button dark"
                              runnable={runnableItem}
                              latestRun={latestRun}
                              onNavigation={onRunDetails}
                />

                <RunButton className="icon-button dark"
                           runnable={runnableItem}
                           latestRun={latestRun}
                           onNavigation={onRunDetails}
                />

                <Favorite checked={favoriteChecked}
                          className="dark-white"
                          onToggle={onFavoriteToggle}
                />
            </span>
        </div>
    );
}

PipelineCardRenderer.propTypes = {
    onClickMain: PropTypes.func,
    status: PropTypes.string,
    startTime: PropTypes.string,
    estimatedDuration: PropTypes.number,
    activityUrl: PropTypes.string,
    displayPath: PropTypes.string,
    branchText: PropTypes.node,
    commitText: PropTypes.node,
    favoriteChecked: PropTypes.bool,
    onFavoriteToggle: PropTypes.func,
    runnableItem: PropTypes.object,
    latestRun: PropTypes.object,
    onRunDetails: PropTypes.func,
    timeText: PropTypes.node,
};

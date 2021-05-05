import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/design-language';
import { UrlConfig, AppConfig, logging, ResultPageHeader, TimeManager, UrlBuilder } from '@jenkins-cd/blueocean-core-js';
import { ExpandablePath, ReadableDate, TimeDuration, CommitId } from '@jenkins-cd/design-language';
import ChangeSetToAuthors from './ChangeSetToAuthors';
import { Link } from 'react-router';
import RunIdCell from './RunIdCell';

export class RunIdNavigation extends Component {
    render() {
        const { run, pipeline, branchName, t } = this.props;

        const nextRunId = run._links.nextRun ? /[\/].*runs\/*([0-9]*)/g.exec(run._links.nextRun.href)[1] : '';
        const prevRunId = run._links.prevRun ? /[\/].*runs\/*([0-9]*)/g.exec(run._links.prevRun.href)[1] : '';

        const nextRunUrl = nextRunId ? UrlBuilder.buildRunUrl(pipeline.organization, pipeline.fullName, branchName, nextRunId, 'pipeline') : '';
        const prevRunUrl = prevRunId ? UrlBuilder.buildRunUrl(pipeline.organization, pipeline.fullName, branchName, prevRunId, 'pipeline') : '';

        return (
            <span className="run-nav-container">
                {prevRunUrl && (
                    <Link to={prevRunUrl} title={t('rundetail.header.prev_run', { defaultValue: 'Previous Run' })}>
                        <Icon size={24} icon="HardwareKeyboardArrowLeft" style={{ verticalAlign: 'bottom' }} />
                    </Link>
                )}
                <RunIdCell run={run} />
                {nextRunUrl && (
                    <Link to={nextRunUrl} title={t('rundetail.header.next_run', { defaultValue: 'Next Run' })}>
                        <Icon size={24} icon="HardwareKeyboardArrowRight" style={{ verticalAlign: 'bottom' }} />
                    </Link>
                )}
            </span>
        );
    }
}

RunIdNavigation.propTypes = {
    run: PropTypes.object,
    pipeline: PropTypes.object,
    branchName: PropTypes.string,
    t: PropTypes.func,
};

class RunDetailsHeader extends Component {
    componentWillMount() {
        this._setDuration(this.props);
    }
    componentWillReceiveProps(nextProps) {
        this._setDuration(nextProps);
    }

    _setDuration(props) {
        const { data: run } = props;
        const isRunning = () => run.isRunning() || run.isPaused() || run.isQueued();
        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        const { durationInMillis } = RunDetailsHeader.timeManager.harmonizeTimes(
            {
                startTime: run.startTime,
                durationInMillis: run.durationInMillis,
                isRunning: isRunning(),
            },
            skewMillis
        );
        this.durationInMillis = durationInMillis;
    }

    render() {
        const { data: run, pipeline, t, locale, onCloseClick, onAuthorsClick, onOrganizationClick, topNavLinks, runButton, isMultiBranch } = this.props;

        const { fullDisplayName } = pipeline;
        const changeSet = run.changeSet;
        const status = run.getComputedResult().toLowerCase();
        const estimatedDurationInMillis = run.estimatedDurationInMillis;

        const durationInMillis = run.durationInMillis; // Duration does not skew :)

        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        // the time when we started the run harmonized with offset
        const isRunning = () => run.isRunning() || run.isPaused();
        const { endTime, startTime } = RunDetailsHeader.timeManager.harmonizeTimes(
            {
                endTime: run.endTime,
                startTime: run.startTime,
            },
            skewMillis
        );
        RunDetailsHeader.logger.debug('timeq:', { startTime, endTime, durationInMillis });

        // pipeline name
        const displayName = decodeURIComponent(run.pipeline);

        // Messages
        const branchLabel = run.pullRequest
            ? t('rundetail.header.pullRequest', { defaultValue: 'Pull Request' })
            : t('rundetail.header.branch', { defaultValue: 'Branch' });
        const commitLabel = t('rundetail.header.commit', { defaultValue: 'Commit' });
        const dateFormatShort = t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' });
        const dateFormatLong = t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' });

        const activityUrl = `${UrlBuilder.buildPipelineUrl(run.organization, pipeline.fullName)}/activity`;

        // Sub-trees
        const title = (
            <h1 className="RunDetailsHeader-title">
                {AppConfig.showOrg() && (
                    <span>
                        <a onClick={onOrganizationClick}>
                            {run.organization === AppConfig.getOrganizationName() ? AppConfig.getOrganizationDisplayName() : run.organization}
                        </a>
                        <span>&nbsp;/&nbsp;</span>
                    </span>
                )}
                <Link className="path-link" to={activityUrl}>
                    <ExpandablePath path={fullDisplayName} hideFirst className="dark-theme" iconSize={20} />
                </Link>
                <RunIdNavigation run={run} pipeline={pipeline} branchName={displayName} t={t} />
            </h1>
        );

        const branchUrl = `${UrlBuilder.buildPipelineUrl(run.organization, pipeline.fullName)}/activity?branch=${run.pipeline}`;
        const labelClassName = run.pullRequest ? 'pullRequest' : '';

        const branchSourceDetails = (
            <div className="u-label-value" title={branchLabel + ': ' + displayName}>
                <label className={labelClassName}>{branchLabel}:</label>
                {isMultiBranch ? (
                    <span className={labelClassName}>
                        <Link to={branchUrl}>{displayName}</Link>
                        {!run.pullRequest &&
                            run.branch &&
                            run.branch.url && (
                                <a className="inline-svg" title="Opens branch in a new window" target="_blank" href={run.branch.url}>
                                    <Icon size={14} icon="ActionLaunch" />
                                </a>
                            )}
                    </span>
                ) : (
                    <span>&mdash;</span>
                )}

                {run.pullRequest &&
                    run.pullRequest.url && (
                        <span>
                            <a title="Opens pull request in a new window" target="_blank" href={run.pullRequest.url}>
                                <Icon size={14} icon="ActionLaunch" />
                            </a>
                        </span>
                    )}
            </div>
        );

        const commitIdString = run.commitId || '—';
        const commitUrl = run.commitUrl || '';

        const commitSourceDetails = (
            <div className="u-label-value" title={commitLabel + ': ' + commitIdString}>
                <label className={labelClassName}>{commitLabel}:</label>
                <span className="commit">
                    <CommitId commitId={commitIdString} url={commitUrl} />
                </span>
            </div>
        );

        const durationDetails = (
            <div>
                <Icon size={16} icon="ImageTimelapse" />
                <TimeDuration
                    millis={isRunning() ? this.durationInMillis : durationInMillis}
                    liveUpdate={isRunning()}
                    updatePeriod={1000}
                    locale={locale}
                    t={t}
                />
            </div>
        );

        const endTimeDetails = (
            <div>
                <Icon size={16} icon="DeviceAccessTime" />
                <ReadableDate date={endTime} liveUpdate locale={locale} shortFormat={dateFormatShort} longFormat={dateFormatLong} />
            </div>
        );

        const cause = run => {
            const lastCause = (run && run.causes && run.causes.length > 0 && run.causes[run.causes.length - 1]) || null;
            if (lastCause && lastCause.upstreamProject) {
                const activityUrl = `${UrlConfig.getJenkinsRootURL()}/${lastCause.upstreamUrl}display/redirect?provider=blueocean`;
                const runUrl = `${UrlConfig.getJenkinsRootURL()}/${lastCause.upstreamUrl}${lastCause.upstreamBuild}/display/redirect?provider=blueocean`;

                return (
                    <div className="causes" title={lastCause.shortDescription}>
                        Started by upstream pipeline "<a href={activityUrl}>{lastCause.upstreamProject}</a>" build{' '}
                        <a href={runUrl}>#{lastCause.upstreamBuild}</a>
                    </div>
                );
            }
            const causeMessage = (lastCause && lastCause.shortDescription) || null;
            return (
                <div className="causes" title={causeMessage}>
                    {causeMessage}
                </div>
            );
        };

        return (
            <ResultPageHeader
                startTime={startTime}
                estimatedDurationInMillis={estimatedDurationInMillis}
                title={title}
                status={status}
                onCloseClick={onCloseClick}
                className="RunDetailsHeader"
                topNavLinks={topNavLinks}
                runButton={runButton}
                t={t}
            >
                <div className="RunDetailsHeader-sources">
                    {branchSourceDetails}
                    {commitSourceDetails}
                </div>
                <div className="RunDetailsHeader-times">
                    {durationDetails}
                    {endTimeDetails}
                </div>
                <div className="RunDetailsHeader-messages">
                    <ChangeSetToAuthors changeSet={changeSet} onAuthorsClick={onAuthorsClick} t={t} />
                    {cause(run)}
                </div>
            </ResultPageHeader>
        );
    }
}

RunDetailsHeader.logger = logging.logger('io.jenkins.blueocean.dashboard.RunDetailsPipeline');
RunDetailsHeader.timeManager = new TimeManager();

RunDetailsHeader.propTypes = {
    data: PropTypes.object.isRequired,
    pipeline: PropTypes.object,
    colors: PropTypes.object,
    onOrganizationClick: PropTypes.func,
    onAuthorsClick: PropTypes.func,
    onCloseClick: PropTypes.func,
    t: PropTypes.func,
    locale: PropTypes.string,
    topNavLinks: PropTypes.node,
    runButton: PropTypes.node,
    isMultiBranch: PropTypes.bool,
};

RunDetailsHeader.contextTypes = {
    config: PropTypes.object.isRequired,
};

export { RunDetailsHeader };

import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';
import { AppConfig, logging, ResultPageHeader, TimeManager } from '@jenkins-cd/blueocean-core-js';
import { ExpandablePath, ReadableDate, TimeDuration } from '@jenkins-cd/design-language';
import ChangeSetToAuthors from './ChangeSetToAuthors';
import { Link } from 'react-router';
import { buildPipelineUrl } from '../util/UrlUtils';
import RunIdCell from './RunIdCell';

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
        const { durationInMillis } = RunDetailsHeader.timeManager.harmonizeTimes({
            startTime: run.startTime,
            durationInMillis: run.durationInMillis,
            isRunning: isRunning(),
        }, skewMillis);
        this.durationInMillis = durationInMillis;
    }

    render() {
        const {
            data: run,
            pipeline,
            t,
            locale,
            onCloseClick,
            onAuthorsClick,
            onOrganizationClick,
            onNameClick,
            topNavLinks,
            runButton,
            isMultiBranch,
        } = this.props;

        const { fullDisplayName } = pipeline;
        const changeSet = run.changeSet;
        const status = run.getComputedResult().toLowerCase();
        const estimatedDurationInMillis = run.estimatedDurationInMillis;

        const durationInMillis = run.durationInMillis; // Duration does not skew :)

        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        // the time when we started the run harmonized with offset
        const isRunning = () => run.isRunning() || run.isPaused();
        const {
            endTime,
            startTime,
        } = RunDetailsHeader.timeManager.harmonizeTimes({
            endTime: run.endTime,
            startTime: run.startTime,
        }, skewMillis);
        RunDetailsHeader.logger.debug('timeq:', { startTime, endTime, durationInMillis });

        // pipeline name
        const displayName = decodeURIComponent(run.pipeline);

        // Messages
        const branchLabel = run.pullRequest ?
            t('rundetail.header.pullRequest', { defaultValue: 'Pull Request' }) :
            t('rundetail.header.branch', { defaultValue: 'Branch' });
        const commitLabel = t('rundetail.header.commit', { defaultValue: 'Commit' });
        const durationDisplayFormat = t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' });
        const durationFormat = t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' });
        const durationHintFormat = t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' });
        const dateFormatShort = t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' });
        const dateFormatLong = t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' });

        // Sub-trees
        const title = (
            <h1 className="RunDetailsHeader-title">
                {AppConfig.showOrg() && <span><a onClick={ onOrganizationClick }>{ run.organization === AppConfig.getOrganizationName() ? AppConfig.getOrganizationDisplayName() : run.organization }</a>
                <span>&nbsp;/&nbsp;</span></span>}
                <a className="path-link" onClick={ onNameClick }>
                    <ExpandablePath path={ fullDisplayName } hideFirst className="dark-theme" iconSize={ 20 } />
                </a>
                <span>&nbsp;<RunIdCell run={run} /></span>
            </h1>
        );

        const branchUrl = `${buildPipelineUrl(run.organization, pipeline.fullName)}/activity?branch=${run.pipeline}`;
        const labelClassName = run.pullRequest ? 'pullRequest' : '';

        const branchSourceDetails = (
            <div className="u-label-value" title={branchLabel + ': ' + displayName}>
                <label className={labelClassName}>{ branchLabel }:</label>
                {isMultiBranch ? (
                    <span className={labelClassName}>
                        <Link to={ branchUrl }>{ displayName }</Link>
                        { !run.pullRequest && run.branch && run.branch.url &&
                            <a className="inline-svg" title="Opens branch in a new window" target="_blank" href={ run.branch.url }>
                                <Icon size={14} icon="launch" />
                            </a>
                        }
                    </span>
                  ) : (
                    <span>&mdash;</span>
                  )}

                { run.pullRequest && run.pullRequest.url &&
                    <span>
                        <a title="Opens pull request in a new window" target="_blank" href={run.pullRequest.url}>
                            <Icon size={14} icon="launch" />
                        </a>
                    </span>
                }
            </div>
        );

        const commitIdString = run.commitId || '—';
        const commitSourceDetails = (
            <div className="u-label-value" title={commitLabel + ': ' + commitIdString}>
                <label className={labelClassName}>{ commitLabel }:</label>
                <span className="commit">
                     { commitIdString.substring(0, 7) }
                </span>
            </div>
        );

        const durationDetails = (
            <div>
                <Icon size={ 16 } icon="timelapse" style={ { fill: '#fff' } } />
                <TimeDuration
                    millis={ isRunning() ? this.durationInMillis : durationInMillis }
                    liveUpdate={ isRunning() }
                    updatePeriod={ 1000 }
                    locale={ locale }
                    displayFormat={ durationDisplayFormat }
                    liveFormat={ durationFormat }
                    hintFormat={ durationHintFormat }
                />
            </div>
        );

        const endTimeDetails = (
            <div>
                <Icon size={ 16 } icon="access_time" style={ { fill: '#fff' } } />
                <ReadableDate
                    date={ endTime }
                    liveUpdate
                    locale={ locale }
                    shortFormat={ dateFormatShort }
                    longFormat={ dateFormatLong }
                />
            </div>
        );

        const causeMessage = (run && run.causes.length > 0 && run.causes[run.causes.length - 1].shortDescription) || null;
        const cause = (<div className="causes">{causeMessage}</div>);

        return (
            <ResultPageHeader startTime={ startTime }
                              estimatedDurationInMillis={ estimatedDurationInMillis }
                              title={ title }
                              status={ status }
                              onCloseClick={ onCloseClick }
                              className="RunDetailsHeader"
                              topNavLinks={ topNavLinks }
                              runButton={ runButton }
            >
                <div className="RunDetailsHeader-sources">
                    { branchSourceDetails }
                    { commitSourceDetails }
                </div>
                <div className="RunDetailsHeader-times">
                    { durationDetails }
                    { endTimeDetails }
                </div>
                <div className="RunDetailsHeader-messages">
                    <ChangeSetToAuthors changeSet={ changeSet } onAuthorsClick={ onAuthorsClick } t={ t } />
                    { cause }
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
    onNameClick: PropTypes.func,
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

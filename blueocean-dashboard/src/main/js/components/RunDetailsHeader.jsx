// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';
import { logging, AppConfig } from '@jenkins-cd/blueocean-core-js';
import { ExpandablePath, ReadableDate, TimeDuration } from '@jenkins-cd/design-language';
import ChangeSetToAuthors from './ChangeSetToAuthors';
import { TimeManager } from '../util/serverBrowserTimeHarmonize';
import { ResultPageHeader } from '@jenkins-cd/blueocean-core-js';

class RunDetailsHeader extends Component {

    componentWillMount() {
        const { data: run } = this.props;
        const isRunning = () => run.isRunning() || run.isPaused() || run.isQueued();
        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        const { durationMillis } = RunDetailsHeader.timeManager.harmonizeTimes({
            startTime: run.startTime,
            durationInMillis: run.durationInMillis,
            isRunning: isRunning(),
        }, skewMillis);
        this.durationMillis = durationMillis;
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
        } = this.props;

        const { fullDisplayName } = pipeline;
        const changeSet = run.changeSet;
        const status = run.getComputedResult().toLowerCase();
        const estimatedDurationInMillis = run.estimatedDurationInMillis;

        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        // the time when we started the run harmonized with offset
        const isRunning = () => run.isRunning() || run.isPaused();
        const {
            durationMillis,
            endTime,
            startTime,
        } = RunDetailsHeader.timeManager.harmonizeTimes({
            endTime: run.endTime,
            startTime: run.startTime,
            durationInMillis: run.durationInMillis,
        }, skewMillis);
        RunDetailsHeader.logger.debug('timeq:', { startTime, endTime, durationMillis });

        // pipeline name
        const displayName = decodeURIComponent(run.pipeline);

        // Messages
        const branchLabel = t('rundetail.header.branch', { defaultValue: 'Branch' });
        const commitLabel = t('rundetail.header.commit', { defaultValue: 'Commit' });
        const durationDisplayFormat = t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' });
        const durationFormat = t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' });
        const durationHintFormat = t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' });
        const dateFormatShort = t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' });
        const dateFormatLong = t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' });

        // Sub-trees
        const title = (
            <h1 className="RunDetailsHeader-title">
                {AppConfig.showOrg() && <span><a onClick={ onOrganizationClick }>{ run.organization }</a>
                <span>&nbsp;/&nbsp;</span></span>}
                <a className="path-link" onClick={ onNameClick }>
                    <ExpandablePath path={ fullDisplayName } hideFirst className="dark-theme" iconSize={ 20 } />
                </a>
                <span>#{ run.id }</span>
            </h1>
        );

        const branchSourceDetails = (
            <div>
                <label>{ branchLabel }:</label>
                <span>{ displayName }</span>
            </div>
        );

        const commitSourceDetails = run.commitId && (
                <div>
                    <label>{ commitLabel }:</label>
                    <span className="commit">
                         { run.commitId.substring(0, 7) }
                    </span>
                </div>
            );

        const durationDetails = (
            <div>
                <Icon size={ 20 } icon="timelapse" style={ { fill: '#fff' } } />
                <TimeDuration
                    millis={ isRunning() ? this.durationMillis : durationMillis }
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
                <Icon size={ 20 } icon="access_time" style={ { fill: '#fff' } } />
                <ReadableDate
                    date={ endTime }
                    liveUpdate
                    locale={ locale }
                    shortFormat={ dateFormatShort }
                    longFormat={ dateFormatLong }
                />
            </div>
        );

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
                <div className="RunDetailsHeader-authors">
                    <ChangeSetToAuthors changeSet={ changeSet } onAuthorsClick={ onAuthorsClick } t={ t } />
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
};

RunDetailsHeader.contextTypes = {
    config: PropTypes.object.isRequired,
};

export { RunDetailsHeader };

// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';
import { ExpandablePath, ReadableDate, TimeDuration } from '@jenkins-cd/design-language';
import ChangeSetToAuthors from './ChangeSetToAuthors';
import moment from 'moment';
import { ResultPageHeader } from '@jenkins-cd/blueocean-core-js';

class RunDetailsHeader extends Component {

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
        const durationMillis = run.isRunning() ?
            moment().diff(moment(run.startTime)) : run.durationInMillis;

        // pipeline name
        const displayName = decodeURIComponent(run.pipeline);

        // Messages
        const branchLabel = t('rundetail.header.branch', { defaultValue: 'Branch' });
        const commitLabel = t('rundetail.header.commit', { defaultValue: 'Commit' });
        const durationFormat = t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' });
        const durationHintFormat = t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' });
        const dateFormatShort = t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' });
        const dateFormatLong = t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' });

        // Sub-trees
        const title = (
            <h1 className="RunDetailsHeader-title">
                <a onClick={onOrganizationClick}>{run.organization}</a>
                <span>&nbsp;/&nbsp;</span>
                <a className="path-link" onClick={onNameClick}>
                    <ExpandablePath path={fullDisplayName} hideFirst className="dark-theme" iconSize={20} />
                </a>
                <span>#{run.id}</span>
            </h1>
        );

        const branchSourceDetails = (
            <div>
                <label>{branchLabel}:</label>
                <span>{displayName}</span>
            </div>
        );

        const commitSourceDetails = run.commitId && (
                <div>
                    <label>{commitLabel}:</label>
                    <span className="commit">
                         {run.commitId.substring(0, 7)}
                    </span>
                </div>
        );

        const durationDetails = (
            <div>
                <Icon size={20} icon="timelapse" style={{ fill: '#fff' }} />
                <TimeDuration
                    millis={durationMillis}
                    liveUpdate={run.isRunning()}
                    updatePeriod={1000}
                    locale={locale}
                    liveFormat={durationFormat}
                    hintFormat={durationHintFormat}
                />
            </div>
        );

        const endTimeDetails = (
            <div>
                <Icon size={20} icon="access_time" style={{ fill: '#fff' }} />
                <ReadableDate
                    date={run.endTime}
                    liveUpdate
                    locale={locale}
                    shortFormat={dateFormatShort}
                    longFormat={dateFormatLong}
                />
            </div>
        );

        return (
            <ResultPageHeader title={title}
                              status={status}
                              onCloseClick={onCloseClick}
                              className="RunDetailsHeader"
                              topNavLinks={topNavLinks}
                              runButton={runButton}
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
                    <ChangeSetToAuthors changeSet={changeSet} onAuthorsClick={onAuthorsClick} t={t} />
                </div>
            </ResultPageHeader>
        );
    }
}

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

export { RunDetailsHeader };

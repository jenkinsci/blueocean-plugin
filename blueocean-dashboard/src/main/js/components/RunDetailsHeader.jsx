// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { ExpandablePath, ReadableDate, LiveStatusIndicator, TimeDuration } from '@jenkins-cd/design-language';
import ChangeSetToAuthors from './ChangeSetToAuthors';
import { TimeManager } from '../util/serverBrowserTimeHarmonize';

const logger = logging.logger('io.jenkins.blueocean.dashboard.RunDetailsPipeline');
const timeManager = new TimeManager();

class RunDetailsHeader extends Component {
    componentWillMount() {
        const { data: run } = this.props;
        const isRunning = () => run.isRunning() || run.isPaused() || run.isQueued();
        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        const { durationMillis } = timeManager.harmonizeTimes({
            startTime: run.startTime,
            durationInMillis: run.durationInMillis,
            isRunning: isRunning(),
        }, skewMillis);
        this.durationMillis = durationMillis;
    }
    handleAuthorsClick() {
        if (this.props.onAuthorsClick) {
            this.props.onAuthorsClick();
        }
    }

    handleOrganizationClick() {
        if (this.props.onOrganizationClick) {
            this.props.onOrganizationClick();
        }
    }

    handleNameClick() {
        if (this.props.onNameClick) {
            this.props.onNameClick();
        }
    }

    render() {
        const { data: run, pipeline: { fullDisplayName }, t, locale } = this.props;
        // pipeline name
        const displayName = decodeURIComponent(run.pipeline);

        // Grab author from each change, run through a set for uniqueness
        // FIXME-FLOW: Remove the ":any" cast after completion of https://github.com/facebook/flow/issues/1059
        const changeSet = run.changeSet;
        const status = run.getComputedResult();
        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        // the time when we started the run harmonized with offset
        const isRunning = () => run.isRunning() || run.isPaused();
        const {
            durationMillis,
            endTime,
            startTime,
        } = timeManager.harmonizeTimes({
            endTime: run.endTime,
            startTime: run.startTime,
            durationInMillis: run.durationInMillis,
        }, skewMillis);
        logger.debug('timeq:', { startTime, endTime, durationMillis });
        const onAuthorsClick = () => this.handleAuthorsClick();
        return (
        <div className="pipeline-result run-details-header">
            <section className="status inverse">
                <LiveStatusIndicator result={status} startTime={startTime}
                  estimatedDuration={run.estimatedDurationInMillis}
                  noBackground
                />
            </section>
            <section className="table">
                <h4>
                    <a onClick={() => this.handleOrganizationClick()}>{run.organization}</a>
                    <span>&nbsp;/&nbsp;</span>
                    <a className="path-link" onClick={() => this.handleNameClick()}>
                        <ExpandablePath path={fullDisplayName} hideFirst className="dark-theme" iconSize={20} />
                    </a>
                    <span>&nbsp;#{run.id}</span>
                </h4>

                <div className="row">
                    <div className="commons">
                        <div>
                            <label>{ t('rundetail.header.branch', { defaultValue: 'Branch' }) }</label>
                            <span>{displayName}</span>
                        </div>
                        { run.commitId ?
                        <div>
                            <label>{t('rundetail.header.commit', { defaultValue: 'Commit' })}</label>
                            <span className="commit">
                                {run.commitId.substring(0, 7)}
                            </span>
                        </div>
                        : null }
                        <ChangeSetToAuthors {...{ changeSet, onAuthorsClick, t }} />
                    </div>
                    <div className="times">
                        <div>
                            <Icon {...{
                                size: 20,
                                icon: 'timelapse',
                                style: { fill: '#fff' },
                            }}
                            />
                            <TimeDuration
                              millis={isRunning() ? this.durationMillis : durationMillis}
                              liveUpdate={isRunning()}
                              updatePeriod={1000}
                              locale={locale}
                              displayFormat={t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' })}
                              liveFormat={t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' })}
                              hintFormat={t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })}
                            />
                        </div>
                        <div>
                            <Icon {...{
                                size: 20,
                                icon: 'access_time',
                                style: { fill: '#fff' },
                            }}
                            />
                            <ReadableDate
                              date={endTime}
                              liveUpdate
                              locale={locale}
                              shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                              longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                            />
                        </div>
                    </div>
                </div>
            </section>
        </div>);
    }
}

const { object, func, string } = PropTypes;

RunDetailsHeader.propTypes = {
    data: object.isRequired,
    pipeline: object,
    colors: object,
    onOrganizationClick: func,
    onNameClick: func,
    onAuthorsClick: func,
    t: func,
    locale: string,
};

RunDetailsHeader.contextTypes = {
    config: object.isRequired,
};

export { RunDetailsHeader };

import React, { Component, PropTypes } from 'react';
import {
    CommitHash, ReadableDate, LiveStatusIndicator, TimeDuration,
}
    from '@jenkins-cd/design-language';
import { logging, ReplayButton, RunButton } from '@jenkins-cd/blueocean-core-js';
import { MULTIBRANCH_PIPELINE, SIMPLE_PIPELINE } from '../Capabilities';

import Extensions from '@jenkins-cd/js-extensions';
import { buildRunDetailsUrl } from '../util/UrlUtils';
import IfCapability from './IfCapability';
import { CellRow, CellLink } from './CellLink';

import { TimeManager } from '../util/serverBrowserTimeHarmonize';

const logger = logging.logger('io.jenkins.blueocean.dashboard');
const timeManager = new TimeManager();
/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/runs
 */
export default class Runs extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        // early out
        if (!this.props.run || !this.props.pipeline) {
            return null;
        }
        const { router, location } = this.context;

        const { run, changeset, pipeline, t, locale } = this.props;

        const resultRun = run.result === 'UNKNOWN' ? run.state : run.result;
        const isRunning = () => {
            const running = run.state === 'RUNNING' || run.state === 'PAUSED' || run.state === 'QUEUED';
            logger.warn('isRunning - inner ', running, run.state);
            return running;
        };
        logger.warn('isRunning', isRunning());
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        // the time when we started the run harmonized with offset
        const {
            durationMillis,
            endTime,
            startTime,
        } = timeManager.harmonizeTimes(run, skewMillis);

        const runDetailsUrl = buildRunDetailsUrl(pipeline.organization, pipeline.fullName, decodeURIComponent(run.pipeline), run.id, 'pipeline');

        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        return (
        <CellRow id={`${pipeline.name}-${run.id}`} linkUrl={runDetailsUrl}>
            <CellLink>
                <LiveStatusIndicator result={resultRun} startTime={startTime}
                  estimatedDuration={run.estimatedDurationInMillis}
                />
            </CellLink>
            <CellLink>{run.id}</CellLink>
            <CellLink><CommitHash commitId={run.commitId} /></CellLink>
            <IfCapability className={pipeline._class} capability={MULTIBRANCH_PIPELINE} >
                <CellLink linkUrl={runDetailsUrl}>{decodeURIComponent(run.pipeline)}</CellLink>
            </IfCapability>
            <CellLink>{changeset && changeset.msg || '-'}</CellLink>
            <CellLink>
                <TimeDuration
                  millis={durationMillis}
                  updatePeriod={1000}
                  liveUpdate={isRunning()}
                  locale={locale}
                  displayFormat={t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' })}
                  liveFormat={t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' })}
                  hintFormat={t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })}
                />
            </CellLink>
            <CellLink>
                <ReadableDate
                  date={endTime}
                  liveUpdate
                  locale={locale}
                  shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                  longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                />
            </CellLink>
            <td>
                <Extensions.Renderer extensionPoint="jenkins.pipeline.activity.list.action" {...t} />
                <RunButton
                  className="icon-button"
                  runnable={this.props.pipeline}
                  latestRun={this.props.run}
                  buttonType="stop-only"
                />
                { /* TODO: check can probably removed and folded into ReplayButton once JENKINS-37519 is done */ }
                <IfCapability className={pipeline._class} capability={[MULTIBRANCH_PIPELINE, SIMPLE_PIPELINE]}>
                    <ReplayButton className="icon-button" runnable={pipeline} latestRun={run} onNavigation={openRunDetails} />
                </IfCapability>
            </td>
        </CellRow>
        );
    }
}

const { object, string, any, func } = PropTypes;

Runs.propTypes = {
    run: object,
    pipeline: object,
    result: any.isRequired, // FIXME: create a shape
    data: string,
    locale: string,
    changeset: object.isRequired,
    t: func,
};
Runs.contextTypes = {
    config: object.isRequired,
    router: object.isRequired, // From react-router
    location: object,
};

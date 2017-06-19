import React, { Component, PropTypes } from 'react';
import {
    CommitHash,
    ReadableDate,
    TimeDuration,
    TableRow,
    TableCell,
} from '@jenkins-cd/design-language';
import {
    ReplayButton,
    RunButton,
    LiveStatusIndicator,
    TimeHarmonizer as timeHarmonizer,
} from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { MULTIBRANCH_PIPELINE, SIMPLE_PIPELINE } from '../Capabilities';
import { buildRunDetailsUrl } from '../util/UrlUtils';
import IfCapability from './IfCapability';
import RunMessageCell from './RunMessageCell';

/*
 Rest source: http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/{PIPELINE_NAME}/runs
 */

class RunDetailsRow extends Component {

    // The number of hardcoded actions not provided by extensions
    static actionItemsCount = 2;

    openRunDetails = (newURL) => {
        const { router, location } = this.context;
        location.pathname = newURL;
        router.push(location);
    };

    render() {
        const {
            run,
            pipeline,
            t,
            locale,
            getTimes,
            columns,
            isMultibranch,
        } = this.props;

        if (!run || !pipeline) {
            return null;
        }

        const resultRun = run.result === 'UNKNOWN' ? run.state : run.result;
        const runDetailsUrl = buildRunDetailsUrl(pipeline.organization, pipeline.fullName, decodeURIComponent(run.pipeline), run.id, 'pipeline');

        const {
            durationMillis,
            endTime,
            startTime,
        } = getTimes({
            result: resultRun,
            durationInMillis: run.durationInMillis,
            startTime: run.startTime,
            endTime: run.endTime,
        });

        const isRunning = run.state === 'RUNNING' || run.state === 'PAUSED' || run.state === 'QUEUED';
        const branchName = isMultibranch && decodeURIComponent(run.pipeline);
        const dataProps = {
            'data-pipeline': pipeline.name,
            'data-runid': run.id,
        };

        if (isMultibranch) {
            dataProps['data-branch'] = branchName;
        }

        return (
            <TableRow useRollover columns={columns} {...dataProps}>
                <TableCell linkTo={runDetailsUrl}>
                    <LiveStatusIndicator
                        durationInMillis={durationMillis}
                        result={resultRun}
                        startTime={startTime}
                        estimatedDuration={run.estimatedDurationInMillis}
                    />
                </TableCell>
                <TableCell linkTo={runDetailsUrl}>{run.id}</TableCell>
                <TableCell linkTo={runDetailsUrl}><CommitHash commitId={run.commitId} /></TableCell>
                { isMultibranch && <TableCell linkTo={runDetailsUrl}>{branchName}</TableCell> }
                <TableCell linkTo={runDetailsUrl}><RunMessageCell run={run} t={t} /></TableCell>
                <TableCell linkTo={runDetailsUrl}>
                    <TimeDuration millis={durationMillis}
                                  updatePeriod={1000}
                                  liveUpdate={isRunning}
                                  locale={locale}
                                  displayFormat={t('common.date.duration.display.format', { defaultValue: 'M[ month] d[ days] h[ hours] m[ minutes] s[ seconds]' })}
                                  liveFormat={t('common.date.duration.format', { defaultValue: 'm[ minutes] s[ seconds]' })}
                                  hintFormat={t('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })}
                    />
                </TableCell>
                <TableCell linkTo={runDetailsUrl}>
                    <ReadableDate date={endTime}
                                  liveUpdate
                                  locale={locale}
                                  shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                                  longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                    />
                </TableCell>
                <TableCell className="TableCell--actions">
                    <Extensions.Renderer extensionPoint="jenkins.pipeline.activity.list.action" {...t} />
                    <RunButton
                        className="icon-button"
                        runnable={this.props.pipeline}
                        latestRun={this.props.run}
                        buttonType="stop-only"
                    />
                    { /* TODO: check can probably removed and folded into ReplayButton once JENKINS-37519 is done */ }
                    <IfCapability className={pipeline._class} capability={[MULTIBRANCH_PIPELINE, SIMPLE_PIPELINE]}>
                        <ReplayButton className="icon-button" runnable={pipeline} latestRun={run} onNavigation={this.openRunDetails} />
                    </IfCapability>
                </TableCell>
            </TableRow>
        );
    }
}

RunDetailsRow.propTypes = {
    run: PropTypes.object,
    pipeline: PropTypes.object,
    locale: PropTypes.string,
    changeset: PropTypes.object.isRequired,
    t: PropTypes.func,
    getTimes: PropTypes.func,
    columns: PropTypes.object,
    isMultibranch: PropTypes.boolan,
};

RunDetailsRow.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object,
};

const harmonized = timeHarmonizer(RunDetailsRow);
harmonized.actionItemsCount = RunDetailsRow.actionItemsCount;

export { harmonized as RunDetailsRow };


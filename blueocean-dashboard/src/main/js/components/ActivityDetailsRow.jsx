import React, { Component, PropTypes } from 'react';
import {
    CommitId,
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
import RunIdCell from './RunIdCell';

/*
 Rest source: http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/{PIPELINE_NAME}/runs
 */

class ActivityDetailsRow extends Component {

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
        const changesUrl = buildRunDetailsUrl(pipeline.organization, pipeline.fullName, decodeURIComponent(run.pipeline), run.id, 'changes');

        const {
            durationInMillis,
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
                        durationInMillis={durationInMillis}
                        result={resultRun}
                        startTime={startTime}
                        estimatedDuration={run.estimatedDurationInMillis}
                    />
                </TableCell>
                <TableCell linkTo={runDetailsUrl}><RunIdCell run={run} /></TableCell>
                <TableCell linkTo={runDetailsUrl}><CommitId className="text-with-ellipsis-container" commitId={run.commitId} /></TableCell>
                { isMultibranch && <TableCell linkTo={runDetailsUrl}><span className="text-with-ellipsis-container">{branchName}</span></TableCell> }
                <TableCell><RunMessageCell linkTo={runDetailsUrl} run={run} t={t} changesUrl={changesUrl} /></TableCell>
                <TableCell linkTo={runDetailsUrl}>
                    <TimeDuration millis={durationInMillis}
                                  updatePeriod={1000}
                                  liveUpdate={isRunning}
                                  locale={locale}
                                  t={t}
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

ActivityDetailsRow.propTypes = {
    run: PropTypes.object,
    pipeline: PropTypes.object,
    locale: PropTypes.string,
    changeset: PropTypes.object.isRequired,
    t: PropTypes.func,
    getTimes: PropTypes.func,
    columns: PropTypes.object,
    isMultibranch: PropTypes.bool,
};

ActivityDetailsRow.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object,
};

const harmonized = timeHarmonizer(ActivityDetailsRow);
harmonized.actionItemsCount = ActivityDetailsRow.actionItemsCount;

export { harmonized as ActivityDetailsRow };


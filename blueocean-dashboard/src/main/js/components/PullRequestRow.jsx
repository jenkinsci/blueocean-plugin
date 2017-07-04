import React, { Component, PropTypes } from 'react';
import { ReadableDate, TableRow, TableCell } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { buildRunDetailsUrl } from '../util/UrlUtils';
import RunHistoryButton from './RunHistoryButton';

function noRun(pr, openRunDetails, t, columns) {
    const actions = [
        <RunButton className="icon-button"
                   runnable={pr}
                   latestRun={pr.latestRun}
                   onNavigation={openRunDetails}
        />,

        <Extensions.Renderer extensionPoint="jenkins.pipeline.pullrequests.list.action" {...t} />,
    ];

    const props = {
        t,
        columns,
        actions,
        pullRequestId: pr.pullRequest.id,
        summary: pr.pullRequest.title,
        author: pr.pullRequest.author,
    };

    return <PullRequestRowRenderer {...props} />;
}

export class PullRequestRowRenderer extends Component {
    
    render() {
        const {
            columns,
            runDetailsUrl,
            pipelineName,
            statusIndicator,
            pullRequestId,
            summary,
            author,
            completed,
            actions = [],
        } = this.props;

        const dataProps = {
            'data-pipeline': pipelineName,
        };

        if (pullRequestId) {
            dataProps['data-pr'] = pullRequestId;
        }

        const actionsCell = React.createElement(
            TableCell,
            {
                className: 'TableCell--actions',
            },
            ...actions);

        return (
            <TableRow columns={columns} linkTo={runDetailsUrl} {...dataProps}>
                <TableCell>{ statusIndicator }</TableCell>
                <TableCell>{ pullRequestId || ' - ' }</TableCell>
                <TableCell>{ summary || ' - ' }</TableCell>
                <TableCell>{ author || ' - ' }</TableCell>
                <TableCell>{ completed || ' - ' }</TableCell>
                { actionsCell }
            </TableRow>
        );
    }
}

PullRequestRowRenderer.propTypes = {
    columns: PropTypes.array,
    runDetailsUrl: PropTypes.string,
    pipelineName: PropTypes.string,
    statusIndicator: PropTypes.node,
    pullRequestId: PropTypes.node,
    summary: PropTypes.node,
    author: PropTypes.node,
    completed: PropTypes.node,
    actions: PropTypes.array,
};

export default class PullRequestRow extends Component {

    // The number of hardcoded actions not provided by extensions
    static actionItemsCount = 2;

    openRunDetails = newUrl => {
        const { router, location } = this.context;

        location.pathname = newUrl;
        router.push(location);
    };

    render() {
        const { pr, t, locale, pipeline: contextPipeline, columns } = this.props;

        if (!pr || !pr.pullRequest || !contextPipeline) {
            return null;
        }

        const { latestRun, pullRequest, name } = pr;

        if (!latestRun) {
            return noRun(pr, this.openRunDetails, t, columns);
        }

        const result = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
        const { fullName, organization } = contextPipeline;
        const runDetailsUrl = buildRunDetailsUrl(organization, fullName, decodeURIComponent(latestRun.pipeline), latestRun.id, 'pipeline');

        const statusIndicator = (
            <LiveStatusIndicator
                durationInMillis={latestRun.durationInMillis}
                result={result}
                startTime={latestRun.startTime}
                estimatedDuration={latestRun.estimatedDurationInMillis}
            />
        );

        const completed = (
            <ReadableDate date={latestRun.endTime}
                          liveUpdate
                          locale={locale}
                          shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                          longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
            />
        );

        const actions = [
            <RunButton className="icon-button"
                       runnable={pr}
                       latestRun={pr.latestRun}
                       onNavigation={this.openRunDetails}
            />,

            <RunHistoryButton pipeline={contextPipeline} branchName={pr.name} />,

            <Extensions.Renderer extensionPoint="jenkins.pipeline.pullrequests.list.action" {...t} />,
        ];

        return (
            <PullRequestRowRenderer columns={columns}
                                    runDetailsUrl={runDetailsUrl}
                                    pipelineName={name}
                                    statusIndicator={statusIndicator}
                                    pullRequestId={pullRequest.id}
                                    summary={pullRequest.title}
                                    author={pullRequest.author}
                                    completed={completed}
                                    actions={actions}
            />
        );
    }
}

PullRequestRow.propTypes = {
    pr: PropTypes.object,
    locale: PropTypes.string,
    t: PropTypes.func,
    pipeline: PropTypes.object,
    columns: PropTypes.array,
};

PullRequestRow.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object,
};

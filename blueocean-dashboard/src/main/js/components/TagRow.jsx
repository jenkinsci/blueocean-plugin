import React, { Component, PropTypes } from 'react';
import { ReadableDate, TableRow, TableCell } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { UrlBuilder } from '@jenkins-cd/blueocean-core-js';
import RunHistoryButton from './RunHistoryButton';

function noRun(tg, openRunDetails, t, columns) {
/* see where tg comes from */
/* see where jenkins.pipeline.tags.list.action extension comes from */
    const actions = [

        <RunButton className="icon-button" runnable={tg} latestRun={tg.latestRun} onNavigation={openRunDetails} />,

        <Extensions.Renderer extensionPoint="jenkins.pipeline.tags.list.action" {...t} />,
    ];

    /* see what is equivalent of pr.pullRequest for tg.tag */

    const props = {
        t,
        columns,
        actions,
        tagId: tg.tag.id,
        summary: tg.tag.title,
        author: tg.tag.author,
    };

    return <TagRowRenderer {...props} />;
}

export class TagRowRenderer extends Component {
    render() {
        const { columns, runDetailsUrl, pipelineName, statusIndicator, tagId, summary, author, completed, actions = [] } = this.props;

        const dataProps = {
            'data-pipeline': pipelineName,
        }

        if (tagId) {
            dataProps['data-tg'] = tagId;
        }

        return (
            <TableRow columns={columns} linkTo={runDetailsUrl} {...dataProps}>
                <TableCell>{statusIndicator}</TableCell>
                <TableCell>{tagId || ' - '}</TableCell>
                <TableCell>{summary || ' - '}</TableCell>
                <TableCell>{author || ' - '}</TableCell>
                <TableCell>{completed || ' - '}</TableCell>
                <TableCell className="TableCell--actions">{actions}</TableCell>
            </TableRow>
        );
    }
}

TagRowRenderer.propTypes = {
    columns: PropTypes.array,
    runDetailsUrl: PropTypes.string,
    pipelineName: PropTypes.string,
    statusIndicator: PropTypes.node,
    tagId: PropTypes.node,
    summary: PropTypes.node,
    author: PropTypes.node,
    completed: PropTypes.node,
    actions: PropTypes.array,
};

export default class TagRow extends Component {
    // The number of hardcoded actions not provided by extensions
    static actionItemsCount = 2;

    openRunDetails = newUrl => {
        const { router, location } = this.context;

        location.pathname = newUrl;
        router.push(location);
    };

    render() {
        const { tg, t, locale, pipeline: contextPipeline, columns } = this.props;

        if (!tg || !tg.tag || !contextPipeline) {
            return null;
        }

        const { latestRun, tag, name } = tg;

        if (!latestRun) {
            return noRun(tg, this.openRunDetails, t, columns);
        }

        const result = latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
        const { fullName, organization } = contextPipeline;
        const runDetailsUrl = UrlBuilder.buildRunUrl(organization, fullName, decodeURIComponent(latestRun.pipeline), latestRun.id, 'pipeline');

        const statusIndicator = (
            <LiveStatusIndicator
                durationInMillis={latestRun.durationInMillis}
                result={result}
                startTime={latestRun.startTime}
                estimatedDuration={latestRun.estimatedDurationInMillis}
            />
        );

        const completed = (
            <ReadableDate
                date={latestRun.endTime}
                liveUpdate
                locale={locale}
                shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
            />
        );

        const actions = (
            <div className="actions-container">
                <RunHistoryButton pipeline={contextPipeline} branchName={tg.name} t={t} />
                <RunButton className="icon-button" runnable={tg} latestRun={tg.latestRun} onNavigation={this.openRunDetails} />
                <Extensions.Renderer extensionPoint="jenkins.pipeline.tags.list.action" t={t} />
            </div>
        );

        return (
            <TagRowRenderer
                columns={columns}
                runDetailsUrl={runDetailsUrl}
                pipelineName={name}
                statusIndicator={statusIndicator}
                tagId={tag.id}
                summary={tag.title}
                author={tag.author}
                completed={completed}
                actions={actions}
            />
        );
    }
}

TagRow.propTypes = {
    tg: PropTypes.object,
    locale: PropTypes.string,
    t: PropTypes.func,
    pipeline: PropTypes.object,
    columns: PropTypes.array,
};

TagRow.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object,
};

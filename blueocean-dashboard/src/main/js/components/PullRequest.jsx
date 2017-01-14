import React, { Component, PropTypes } from 'react';
import { LiveStatusIndicator, ReadableDate } from '@jenkins-cd/design-language';
import { RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { CellRow, CellLink } from './CellLink';

import { buildRunDetailsUrl } from '../util/UrlUtils';

export default class PullRequest extends Component {
    render() {
        const { pr, t, locale, pipeline: contextPipeline } = this.props;
        if (!pr || !pr.pullRequest || !pr.latestRun || !contextPipeline) {
            return null;
        }
        const {
            latestRun: {
                result: resultString,
                id,
                startTime,
                pipeline,
                endTime,
                estimatedDurationInMillis,
                state,
            },
            pullRequest: {
                id: prId,
                title,
                author,
            },
            name,
        } = pr;
        const result = resultString === 'UNKNOWN' ? state : resultString;
        const {
            router,
            location,
        } = this.context;
        const { fullName, organization } = contextPipeline;
        const runDetailsUrl = buildRunDetailsUrl(organization, fullName, decodeURIComponent(pipeline), id, 'pipeline');

        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };
        return (
            <CellRow linkUrl={runDetailsUrl} id={`${name}-${id}`}>
                <CellLink>
                    <LiveStatusIndicator result={result} startTime={startTime}
                      estimatedDuration={estimatedDurationInMillis}
                    />
                </CellLink>
                <CellLink>{prId}</CellLink>
                <CellLink>{title || '-'}</CellLink>
                <CellLink>{author || '-'}</CellLink>
                <CellLink>
                    <ReadableDate
                      date={endTime}
                      liveUpdate
                      locale={locale}
                      shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                      longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                    />
                </CellLink>
                <td className="actions">
                    <RunButton
                      className="icon-button"
                      runnable={pr}
                      latestRun={pr.latestRun}
                      onNavigation={openRunDetails}
                    />
                    <Extensions.Renderer extensionPoint="jenkins.pipeline.pullrequests.list.action" {...t} />
                </td>
            </CellRow>
        );
    }
}

const { func, object, string } = PropTypes;

PullRequest.propTypes = {
    pr: object,
    locale: string,
    t: func,
    pipeline: object,
};

PullRequest.contextTypes = {
    router: object.isRequired, // From react-router
    location: object,
};

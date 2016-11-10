import React, { Component, PropTypes } from 'react';
import { LiveStatusIndicator, ReadableDate } from '@jenkins-cd/design-language';
import { RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

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
        const open = () => {
            location.pathname = buildRunDetailsUrl(organization, fullName, decodeURIComponent(pipeline), id, 'pipeline');
            router.push(location);
        };

        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        return (<tr key={id} onClick={open} id={`${name}-${id}`} >
            <td>
                <LiveStatusIndicator result={result} startTime={startTime}
                  estimatedDuration={estimatedDurationInMillis}
                />
            </td>
            <td>{id}</td>
            <td>{title || '-'}</td>
            <td>{author || '-'}</td>
            <td>
                <ReadableDate
                  date={endTime}
                  liveUpdate
                  locale={locale}
                  shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                  longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                />
            </td>
            <td>
                <RunButton
                  className="icon-button"
                  runnable={pr}
                  latestRun={pr.latestRun}
                  onNavigation={openRunDetails}
                />
                <Extensions.Renderer extensionPoint="jenkins.pipeline.pullrequests.list.action" />
            </td>
        </tr>);
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

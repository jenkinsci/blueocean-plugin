import React, { Component, PropTypes } from 'react';
import { LiveStatusIndicator, ReadableDate } from '@jenkins-cd/design-language';
import { RunButton, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { buildRunDetailsUrl } from '../util/UrlUtils';

const { object } = PropTypes;

export default class PullRequest extends Component {
    render() {
        const { pr, pipeline: contextPipeline } = this.props;
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
        const url = buildRunDetailsUrl(organization, fullName, decodeURIComponent(pipeline), id, 'pipeline');
          
        const open = (event) => {
            if (event) {
                event.preventDefault();
            }
            location.pathname = url;
            router.push(location);
        };
        const PRCol = (props) => <td className="tableRowLink"><a onClick={open} href={`${UrlConfig.getJenkinsRootURL()}/blue${url}`}>{props.children}</a></td>;
      
        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        return (<tr key={id} onClick={open} id={`${name}-${id}`} >
            <PRCol>
                <LiveStatusIndicator result={result} startTime={startTime}
                  estimatedDuration={estimatedDurationInMillis}
                />
            </PRCol>
            <PRCol>{id}</PRCol>
            <PRCol>{title || '-'}</PRCol>
            <PRCol>{author || '-'}</PRCol>
            <PRCol><ReadableDate date={endTime} liveUpdate /></PRCol>
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

PullRequest.propTypes = {
    pr: object,
    pipeline: object,
};

PullRequest.contextTypes = {
    router: object.isRequired, // From react-router
    location: object,
};

import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';
import { RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { buildRunDetailsUrl } from '../util/UrlUtils';

const stopProp = (event) => event.stopPropagation();

export default class Branches extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        const { data, t, locale, pipeline } = this.props;
        // early out
        if (!data || !pipeline) {
            return null;
        }
        const {
            router,
            location,
        } = this.context;

        const {
            latestRun: { id, result, startTime, endTime, changeSet, state, commitId, estimatedDurationInMillis },
            weatherScore,
            name: branchName,
        } = data;
        const { fullName, organization } = pipeline;

        const cleanBranchName = decodeURIComponent(branchName);
        const url = buildRunDetailsUrl(organization, fullName, cleanBranchName, id, 'pipeline');

        const open = () => {
            location.pathname = url;
            router.push(location);
        };

        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        const { msg } = changeSet[0] || {};

        return (
            <tr key={cleanBranchName} onClick={open} id={`${cleanBranchName}-${id}`} >
                <td><WeatherIcon score={weatherScore} /></td>
                <td onClick={open}>
                    <LiveStatusIndicator result={result === 'UNKNOWN' ? state : result}
                      startTime={startTime} estimatedDuration={estimatedDurationInMillis}
                    />
                </td>
                <td>{cleanBranchName}</td>
                <td><CommitHash commitId={commitId} /></td>
                <td>{msg || '-'}</td>
                <td>
                  <ReadableDate
                    date={endTime}
                    liveUpdate
                    locale={locale}
                    shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                    longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                  />
                </td>
                { /* suppress all click events from extension points */ }
                <td className="actions" onClick={(event) => stopProp(event)}>
                    <RunButton
                      className="icon-button"
                      runnable={data}
                      latestRun={data.latestRun}
                      onNavigation={openRunDetails}
                    />
                    <Extensions.Renderer
                      extensionPoint="jenkins.pipeline.branches.list.action"
                      pipeline={data}
                      store={this.context.store}
                    />
                </td>
            </tr>
        );
    }
}

const { func, object, string } = PropTypes;
Branches.propTypes = {
    data: object.isRequired,
    t: func,
    locale: string,
    pipeline: object,
};

Branches.contextTypes = {
    store: object,
    router: object.isRequired, // From react-router
    location: object,
};

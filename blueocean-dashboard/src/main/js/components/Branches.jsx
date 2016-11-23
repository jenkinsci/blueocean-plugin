import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';
import { RunButton, UrlConfig } from '@jenkins-cd/blueocean-core-js';
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

        const open = (event) => {
            if (event) {
                event.preventDefault();
            }
            location.pathname = url;
            router.push(location);
        };

        const BranchCol = (props) => <td className="tableRowLink">
            <a onClick={open} href={`${UrlConfig.getJenkinsRootURL()}/blue${url}`}>{props.children}</a>
        </td>;
       
        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        const { msg } = changeSet[0] || {};

        return (
            <tr key={cleanBranchName} onClick={open} id={`${cleanBranchName}-${id}`} >
                <BranchCol><WeatherIcon score={weatherScore} /></BranchCol>
                <BranchCol onClick={open}>
                    <LiveStatusIndicator result={result === 'UNKNOWN' ? state : result}
                      startTime={startTime} estimatedDuration={estimatedDurationInMillis}
                    />
                </BranchCol>
                <BranchCol>{cleanBranchName}</BranchCol>
                <BranchCol><CommitHash commitId={commitId} /></BranchCol>
                <BranchCol>{msg || '-'}</BranchCol>
                <BranchCol>
                    <ReadableDate
                      date={endTime}
                      liveUpdate
                      locale={locale}
                      shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                      longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                    />
                </BranchCol>
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
                      {...t}
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

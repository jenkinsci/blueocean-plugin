import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';
import { locationService, RunButton, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { buildRunDetailsUrl } from '../util/UrlUtils';
import { observer } from 'mobx-react';


const stopProp = (event) => event.stopPropagation();

@observer
export default class Branches extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        const current = locationService.previous;
        console.log(current, 'current');
        const { data: branch, pipeline, t, locale } = this.props;
        // early out
        if (!branch || !pipeline) {
            return null;
        }

        const { router, location } = this.context;
        const latestRun = branch.latestRun;
        
        const cleanBranchName = decodeURIComponent(branch.name);
        const url = buildRunDetailsUrl(branch.organization, pipeline.fullName, cleanBranchName, latestRun.id, 'pipeline');

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

        const { msg } = (branch.changeSet && branch.changeSet.length > 0) ? (branch.changeSet[0] || {}) : {};
        return (
            <tr key={cleanBranchName} onClick={open} id={`${cleanBranchName}-${latestRun.id}`} >
                <BranchCol><WeatherIcon score={branch.weatherScore} /></BranchCol>
                <BranchCol onClick={open}>
                    <LiveStatusIndicator result={latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result}
                      startTime={latestRun.startTime} estimatedDuration={latestRun.estimatedDurationInMillis}
                    />
                </BranchCol>
                <BranchCol>{cleanBranchName}</BranchCol>
                <BranchCol><CommitHash commitId={latestRun.commitId} /></BranchCol>
                <BranchCol>{msg || '-'}</BranchCol>
                <BranchCol>
                    <ReadableDate
                      date={latestRun.endTime}
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
                      runnable={branch}
                      latestRun={branch.latestRun}
                      onNavigation={openRunDetails}
                    />
                    <Extensions.Renderer
                      extensionPoint="jenkins.pipeline.branches.list.action"
                      pipeline={branch }
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

import React, { Component, PropTypes } from 'react';
import {
    CommitHash,
    ReadableDate,
    LiveStatusIndicator,
    WeatherIcon,
} from '@jenkins-cd/design-language';
import { RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';
import { observer } from 'mobx-react';
import { CellRow, CellLink } from './CellLink';

import { buildRunDetailsUrl } from '../util/UrlUtils';

@observer
export default class Branches extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        const { data: branch, pipeline, t, locale } = this.props;
        // early out
        if (!branch || !pipeline) {
            return null;
        }

        const { router, location } = this.context;
        const latestRun = branch.latestRun;

        const cleanBranchName = decodeURIComponent(branch.name);
        const runDetailsUrl = buildRunDetailsUrl(branch.organization, pipeline.fullName, cleanBranchName, latestRun.id, 'pipeline');

        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        const { msg } = (latestRun.changeSet && latestRun.changeSet.length > 0) ? (latestRun.changeSet[latestRun.changeSet.length - 1] || {}) : {};
        return (
            <CellRow linkUrl={runDetailsUrl} id={`${cleanBranchName}-${latestRun.id}`}>
                <CellLink disableDefaultPadding>
                    <WeatherIcon score={branch.weatherScore} />
                </CellLink>
                <CellLink>
                    <LiveStatusIndicator result={latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result}
                      startTime={latestRun.startTime} estimatedDuration={latestRun.estimatedDurationInMillis}
                    />
                </CellLink>
                <CellLink>{cleanBranchName}</CellLink>
                <CellLink><CommitHash commitId={latestRun.commitId} /></CellLink>
                <CellLink>{msg || '-'}</CellLink>
                <CellLink>
                    <ReadableDate
                      date={latestRun.endTime}
                      liveUpdate
                      locale={locale}
                      shortFormat={t('common.date.readable.short', { defaultValue: 'MMM DD h:mma Z' })}
                      longFormat={t('common.date.readable.long', { defaultValue: 'MMM DD YYYY h:mma Z' })}
                    />
                </CellLink>
                <td className="actions">
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
            </CellRow>
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

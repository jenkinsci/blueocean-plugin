import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';
import { RunButton } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { buildRunDetailsUrl } from '../util/UrlUtils';
import { observer } from 'mobx-react';
const { object } = PropTypes;

const stopProp = (event) => event.stopPropagation();

@observer
export default class Branches extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        const { data: branch, pipeline } = this.props;
        // early out
        if (!branch || !pipeline) {
            return null;
        }
        const { router, location } = this.context;
        const latestRun = branch.latestRun;
        
        const cleanBranchName = decodeURIComponent(branch.name);
        const url = buildRunDetailsUrl(branch.organization, branch.fullName, cleanBranchName, latestRun.id, 'pipeline');

        const open = () => {
            location.pathname = url;
            router.push(location);
        };

        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        const { msg } = (branch.changeSet && branch.changeSet.length > 0) ? (branch.changeSet[0] || {}) : {};
        return (
            <tr key={cleanBranchName} onClick={open} id={`${cleanBranchName}-${latestRun.id}`} >
                <td><WeatherIcon score={branch.weatherScore} /></td>
                <td onClick={open}>
                    <LiveStatusIndicator result={latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result}
                      startTime={latestRun.startTime} estimatedDuration={latestRun.estimatedDurationInMillis}
                    />
                </td>
                <td>{cleanBranchName}</td>
                <td><CommitHash commitId={latestRun.commitId} /></td>
                <td>{msg || '-'}</td>
                <td><ReadableDate date={latestRun.endTime} liveUpdate /></td>
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
                    />
                </td>
            </tr>
        );
    }
}

Branches.propTypes = {
    data: object.isRequired,
    pipeline: object,
};

Branches.contextTypes = {
    store: object,
    router: object.isRequired, // From react-router
    location: object,
};

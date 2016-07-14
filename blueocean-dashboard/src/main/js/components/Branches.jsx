import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';
import RunPipeline from './RunPipeline.jsx';
import { buildRunDetailsUrl } from '../util/UrlUtils';

const { object } = PropTypes;

export default class Branches extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        const { data } = this.props;
        // early out
        if (!data || !this.context.pipeline) {
            return null;
        }
        const {
            context: {
                router,
                location,
                pipeline: {
                    fullName,
                    organization,
                    },
                },
            } = this;
        const {
            latestRun: { id, result, startTime, endTime, changeSet, state, commitId, estimatedDurationInMillis },
            weatherScore,
            name: branchName,
        } = data;

        
        const cleanBranchName = decodeURIComponent(branchName);
        const url = buildRunDetailsUrl(organization, fullName, cleanBranchName, id, 'pipeline');
        
        const open = () => {
            location.pathname = url;
            router.push(location);
        };
        const { msg } = changeSet[0] || {};

        return (<tr key={cleanBranchName} onClick={open} id={`${cleanBranchName}-${id}`} >
            <td><WeatherIcon score={weatherScore} /></td>
            <td onClick={open}>
                <LiveStatusIndicator result={result === 'UNKNOWN' ? state : result}
                  startTime={startTime} estimatedDuration={estimatedDurationInMillis}
                />
            </td>
            <td>{cleanBranchName}</td>
            <td><CommitHash commitId={commitId} /></td>
            <td>{msg || '-'}</td>
            <td><ReadableDate date={endTime} liveUpdate /></td>
            <td><RunPipeline organization={organization} pipeline={fullName} branch={encodeURIComponent(branchName)} /></td>
        </tr>);
    }
}


Branches.propTypes = {
    data: object.isRequired,
};


Branches.contextTypes = {
    pipeline: object,
    router: object.isRequired, // From react-router
    location: object,
};

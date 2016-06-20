import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';
import RunPipeline from './RunPipeline.jsx';

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
                    name: pipelineName,
                    organization,
                    },
                },
            } = this;
        const {
            latestRun: { id, result, startTime, endTime, changeSet, state, commitId, estimatedDurationInMillis },
            weatherScore,
            name,
        } = data;
        const url = `/organizations/${organization}/${pipelineName}/detail/${name}/${id}/pipeline`;
        const open = () => {
            location.pathname = url;
            router.push(location);
        };
        const { msg } = changeSet[0] || {};

        return (<tr key={name} id={`${name}-${id}`} >
            <td onClick={open}><WeatherIcon score={weatherScore} /></td>
            <td onClick={open}>
                <LiveStatusIndicator result={result === 'UNKNOWN' ? state : result}
                  startTime={startTime} estimatedDuration={estimatedDurationInMillis}
                />
            </td>
            <td onClick={open}>{decodeURIComponent(name)}</td>
            <td onClick={open}><CommitHash commitId={commitId} /></td>
            <td onClick={open}>{msg || '-'}</td>
            <td onClick={open}><ReadableDate date={endTime || ''} /></td>
            <td><RunPipeline organization={organization} pipeline={pipelineName} branch={name} /></td>
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

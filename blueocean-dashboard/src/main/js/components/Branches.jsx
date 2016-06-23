import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator, WeatherIcon } from '@jenkins-cd/design-language';

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

        return (<tr key={name} onClick={open} id={`${name}-${id}`} >
            <td><WeatherIcon score={weatherScore} /></td>
            <td>
                <LiveStatusIndicator result={result === 'UNKNOWN' ? state : result}
                  startTime={startTime} estimatedDuration={estimatedDurationInMillis}
                />
            </td>
            <td>{decodeURIComponent(name)}</td>
            <td><CommitHash commitId={commitId} /></td>
            <td>{msg || '-'}</td>
            <td><ReadableDate date={endTime} liveUpdate /></td>
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

import React, { Component, PropTypes } from 'react';
import { CommitHash, ReadableDate } from '@jenkins-cd/design-language';
import { WeatherIcon, StatusIndicator } from '@jenkins-cd/design-language';

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
                    },
                },
            } = this;
        const {
            latestRun: { id, result, endTime, changeSet, state, commitId },
            weatherScore,
            name,
        } = data;
        const url = `/pipelines/${pipelineName}/detail/${name}/${id}/pipeline`;
        const open = () => {
            location.pathname = url;
            router.push(location);
        };
        const { msg } = changeSet[0] || {};
        return (<tr key={name} onClick={open} id={`${name}-${id}`} >
            <td><WeatherIcon score={weatherScore} /></td>
            <td>
                <StatusIndicator result={result === 'UNKNOWN' ? state : result} />
            </td>
            <td>{decodeURIComponent(name)}</td>
            <td><CommitHash commitId={commitId} /></td>
            <td>{msg || '-'}</td>
            <td><ReadableDate date={endTime || ''} /></td>
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

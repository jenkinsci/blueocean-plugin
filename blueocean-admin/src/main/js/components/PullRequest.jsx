import React, { Component, PropTypes } from 'react';
import { ReadableDate, StatusIndicator } from '@jenkins-cd/design-language';

const { object } = PropTypes;

export default class PullRequest extends Component {
    render() {
        const { pr } = this.props;
        if (!pr || !pr.pullRequest || !pr.latestRun || !this.context.pipeline) {
            return null;
        }
        const {
            latestRun: {
                result: resultString,
                id,
                endTime,
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
            context: {
                router,
                location,
                pipeline: {
                    name: pipelineName,
            },
                },
        } = this;
        const url = `/pipelines/${pipelineName}/detail/${name}/${id}/pipeline`;
        const open = () => {
            location.pathname = url;
            router.push(location);
        };
        return (<tr key={id} onClick={open} id={`${name}-${id}`} >
            <td><StatusIndicator result={result} /></td>
            <td>{id}</td>
            <td>{title || '-'}</td>
            <td>{author || '-'}</td>
            <td><ReadableDate date={endTime} /></td>
        </tr>);
    }
}

PullRequest.propTypes = {
    pr: object,
};

PullRequest.contextTypes = {
    pipeline: object,
    router: object.isRequired, // From react-router
    location: object,
};

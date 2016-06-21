import React, { Component, PropTypes } from 'react';
import { LiveStatusIndicator, ReadableDate } from '@jenkins-cd/design-language';
import RunPipeline from './RunPipeline.jsx';

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
                startTime,
                endTime,
                estimatedDurationInMillis,
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
                    organization,
            },
                },
        } = this;
        const url = `organizations/${organization}/${pipelineName}/detail/${name}/${id}/pipeline`;
        const open = () => {
            location.pathname = url;
            router.push(location);
        };

        return (<tr key={id} id={`${name}-${id}`} >
            <td onClick={open}>
                <LiveStatusIndicator result={result} startTime={startTime}
                  estimatedDuration={estimatedDurationInMillis}
                />
            </td>
            <td onClick={open}>{id}</td>
            <td onClick={open}>{title || '-'}</td>
            <td onClick={open}>{author || '-'}</td>
            <td onClick={open}><ReadableDate date={endTime} /></td>
            <td><RunPipeline organization={organization} pipeline={pipelineName} branch={name} /></td>
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

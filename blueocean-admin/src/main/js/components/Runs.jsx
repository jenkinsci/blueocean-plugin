import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { StatusIndicator, CommitHash, ReadableDate } from '@jenkins-cd/design-language';
const { object, string, any } = PropTypes;

require('moment-duration-format');

/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/runs
 */
export default class Runs extends Component {
    constructor(props) {
        super(props);
        this.state = { isVisible: false };
    }
    render() {
        // early out
        if (!this.props.result || !this.context.pipeline) {
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
            props: {
                result: {
                    durationInMillis,
                    pipeline,
                    id,
                    result,
                    state,
                    endTime,
                    commitId,
                },
                changeset,
            },
        } = this;

        const duration = moment.duration(durationInMillis).humanize();
        const resultRun = result === 'UNKNOWN' ? state : result;

        const url = `/pipelines/${pipelineName}/detail/${pipeline}/${id}/pipeline`;
        const open = () => {
            location.pathname = url;
            router.push(location);
        };

        return (<tr key={id} onClick={open} id={`${pipeline}-${id}`} >
            <td>
                <StatusIndicator result={resultRun} />
            </td>
            <td>
                {id}
            </td>
            <td><CommitHash commitId={commitId} /></td>
            <td>{decodeURIComponent(pipeline)}</td>
            <td>{changeset && changeset.comment || '-'}</td>
            <td>{duration}</td>
            <td><ReadableDate date={endTime} /></td>
        </tr>);
    }
}

Runs.propTypes = {
    result: any.isRequired, // FIXME: create a shape
    data: string,
    changeset: object.isRequired,
};
Runs.contextTypes = {
    pipeline: object,
    router: object.isRequired, // From react-router
    location: object,
};

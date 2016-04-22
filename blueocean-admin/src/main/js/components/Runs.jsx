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
                result,
                changeset,
            },
        } = this;

        const duration = moment.duration(result.durationInMillis).humanize();
        const name = decodeURIComponent(result.pipeline);
        const resultRun = result.result === 'UNKNOWN' ? result.state : result.result;

        const url = `/pipelines/${pipelineName}/detail/${name}/${result.id}`;
        const open = () => {
            location.pathname = url;
            router.push(location);
        };

        return (<tr key={result.id} onClick={open} >
            <td>
                <StatusIndicator result={resultRun} />
            </td>
            <td>
                {result.id}
            </td>
            <td><CommitHash commitId={changeset.commitId} /></td>
            <td>{name}</td>
            <td>{changeset && changeset.comment || '-'}</td>
            <td>{duration}</td>
            <td><ReadableDate date={result.endTime} /></td>
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

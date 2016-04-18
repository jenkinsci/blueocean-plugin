import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import { StatusIndicator } from '@jenkins-cd/design-language';

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
        const name = decodeURIComponent(result.pipeline);
        const url = `/pipelines/${pipelineName}/detail/${name}/${result.id}`;
        let
            duration = moment.duration(
                Number(result.durationInMillis), 'milliseconds').format('hh:mm:ss');

        const durationArray = duration.split(':');

        if (durationArray.length === 1) {
            duration = `00:${duration}`;
        }

        const resultRun = result.result === 'UNKNOWN' ? result.state : result.result;

        const open = () => {
            location.pathname = url;
            router.replace(location);
        };

        return (<tr key={result.id}>
            <td>
                <a onClick={open}>
                    <StatusIndicator result={resultRun} />
                </a>
            </td>
            <td>{result.id}</td>
            <td>{changeset && changeset.commitId && changeset.commitId.substring(0, 8) || '-'}</td>
            <td>{name}</td>
            <td>{changeset && changeset.comment || '-'}</td>
            <td>
                {duration} minutes
            </td>
            <td>{moment(result.endTime).fromNow()}</td>
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

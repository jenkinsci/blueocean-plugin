import React, { Component, PropTypes } from 'react';
import moment from 'moment';
require('moment-duration-format');

/*
 http://localhost:8080/jenkins/blue/rest/organizations/jenkins/pipelines/PR-demo/runs
 */
export default class Runs extends Component {
    render() {
        const { data, changeset } = this.props;
        // early out
        if (!data && data.toJS) {
            return null;
        }
        let
            duration = moment.duration(
                Number(data.durationInMillis), 'milliseconds').format('hh:mm:ss');

        const
            durationArray = duration.split(':');

        if (durationArray.length === 1) {
            duration = `00:${duration}`;
        }

        return (<tr key={data.id}>
            <td>{data.result}</td>
            <td>{data.id}</td>
            <td>{changeset && changeset.commitId && changeset.commitId.substring(0, 8)}</td>
            <td>{decodeURIComponent(data.pipeline)}</td>
            <td>{changeset && changeset.comment}</td>
            <td>
                {duration} minutes
            </td>
            <td>{moment(data.endTime).fromNow()}</td>
        </tr>);
    }
}

Runs.propTypes = {
    data: PropTypes.object.isRequired,
    changeset: PropTypes.object.isRequired,
};

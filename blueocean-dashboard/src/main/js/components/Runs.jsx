import React, { Component, PropTypes } from 'react';
import {
    CommitHash, ReadableDate, LiveStatusIndicator, TimeDuration,
}
    from '@jenkins-cd/design-language';
import { removeLastUrlSegment } from '../util/UrlUtils';
import moment from 'moment';

const { object, string, any } = PropTypes;

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

        const resultRun = result === 'UNKNOWN' ? state : result;
        const estimatedDuration = this.context.pipeline.estimatedDurationInMillis;
        const startTime = moment.parseZone(this.props.result.startTime).valueOf();

        const baseUrl = removeLastUrlSegment(this.context.location.pathname);
        const url = `${baseUrl}/detail/${pipeline}/${id}/pipeline`;
        const open = () => {
            location.pathname = url;
            router.push(location);
        };

        return (<tr key={id} onClick={open} id={`${pipeline}-${id}`} >
            <td>
                <LiveStatusIndicator result={resultRun} startTime={startTime} estimatedDuration={estimatedDuration} />
            </td>
            <td>
                {id}
            </td>
            <td><CommitHash commitId={commitId} /></td>
            <td>{decodeURIComponent(pipeline)}</td>
            <td>{changeset && changeset.comment || '-'}</td>
            <td><TimeDuration millis={durationInMillis} /></td>
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

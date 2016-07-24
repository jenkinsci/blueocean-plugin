import React, { Component, PropTypes } from 'react';
import {
    CommitHash, ReadableDate, LiveStatusIndicator, TimeDuration,
}
    from '@jenkins-cd/design-language';

import { MULTIBRANCH_PIPELINE } from '../Capabilities';

import Extensions from '@jenkins-cd/js-extensions';
import moment from 'moment';
import { buildRunDetailsUrl } from '../util/UrlUtils';
import IfCapability from './IfCapability';

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
                pipeline: {
                    _class: pipelineClass,
                    fullName,
                    organization,
                },
            },
            props: {
                result: {
                    durationInMillis,
                    estimatedDurationInMillis,
                    pipeline,
                    id,
                    result,
                    state,
                    startTime,
                    endTime,
                    commitId,
                },
                changeset,
            },
        } = this;

        const resultRun = result === 'UNKNOWN' ? state : result;
        const running = resultRun === 'RUNNING';
        const durationMillis = !running ?
            durationInMillis :
            moment().diff(moment(startTime));

        const open = () => {
            const pipelineName = decodeURIComponent(pipeline);
            location.pathname = buildRunDetailsUrl(organization, fullName, pipelineName, id, 'pipeline');
            router.push(location);
        };

        return (<tr key={id} onClick={open} id={`${pipeline}-${id}`} >
            <td>
                <LiveStatusIndicator result={resultRun} startTime={startTime}
                  estimatedDuration={estimatedDurationInMillis}
                />
            </td>
            <td>
                {id}
            </td>
            <td><CommitHash commitId={commitId} /></td>
            <IfCapability _class={pipelineClass} capability={MULTIBRANCH_PIPELINE} >
                <td>{decodeURIComponent(pipeline)}</td>
            </IfCapability>
            <td>{changeset && changeset.comment || '-'}</td>
            <td><TimeDuration millis={durationMillis} liveUpdate={running} /></td>
            <td><ReadableDate date={endTime} liveUpdate /></td>
            <td>
                <Extensions.Renderer extensionPoint="jenkins.pipeline.activity.list.action" />
            </td>
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

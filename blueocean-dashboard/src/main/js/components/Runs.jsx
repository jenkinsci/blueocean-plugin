import React, { Component, PropTypes } from 'react';
import {
    CommitHash, ReadableDate, LiveStatusIndicator, TimeDuration,
}
    from '@jenkins-cd/design-language';

import { MULTIBRANCH_PIPELINE } from '../Capabilities';

import Extensions from '@jenkins-cd/js-extensions';
import moment from 'moment';
import { getLocation } from '../util/UrlUtils';
import Pipeline from '../api/Pipeline';
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

    openRunDetails() {
        const location = getLocation({
            ...this.context,
            branch: (Pipeline.isMultibranch(this.context.pipeline) && this.props.result.pipeline),
            runId: this.props.result.id,
        });
        this.context.router.push(location);
    }

    render() {
        // early out
        if (!this.props.result || !this.context.pipeline) {
            return null;
        }
        const {
            context: {
                pipeline: {
                    _class: pipelineClass,
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

        return (<tr key={id} onClick={() => this.openRunDetails()} id={`${pipeline}-${id}`} >
            <td>
                <LiveStatusIndicator result={resultRun} startTime={startTime}
                  estimatedDuration={estimatedDurationInMillis}
                />
            </td>
            <td>
                {id}
            </td>
            <td><CommitHash commitId={commitId} /></td>
            <IfCapability className={pipelineClass} capability={MULTIBRANCH_PIPELINE} >
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
    basePage: string,
};
Runs.contextTypes = {
    pipeline: object,
    router: object.isRequired, // From react-router
    location: object,
};

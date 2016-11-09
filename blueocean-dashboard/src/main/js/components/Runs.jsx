import React, { Component, PropTypes } from 'react';
import {
    CommitHash, ReadableDate, LiveStatusIndicator, TimeDuration,
}
    from '@jenkins-cd/design-language';
import { ReplayButton, RunButton } from '@jenkins-cd/blueocean-core-js';

import { MULTIBRANCH_PIPELINE, SIMPLE_PIPELINE } from '../Capabilities';

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
        if (!this.props.run || !this.context.pipeline) {
            return null;
        }
        const { router, location, pipeline } = this.context;
      
        const { run, changeset } = this.props;
          
        const resultRun = run.result === 'UNKNOWN' ? run.state : run.result;
        const running = resultRun === 'RUNNING';
        const durationMillis = !running ?
            run.durationInMillis :
            moment().diff(moment(run.startTime));

        const open = () => {
            const pipelineName = decodeURIComponent(pipeline.name);
            console.log('pipelineName',run.id);
            location.pathname = buildRunDetailsUrl(pipeline.organization, pipeline.fullName, pipelineName, run.id, 'pipeline');
            
            router.push(location);
        };

        const openRunDetails = (newUrl) => {
            location.pathname = newUrl;
            router.push(location);
        };

        return (<tr key={run.id} onClick={open} id={`${pipeline}-${run.id}`} >
            <td>
                <LiveStatusIndicator result={resultRun} startTime={run.startTime}
                  estimatedDuration={run.estimatedDurationInMillis}
                />
            </td>
            <td>{run.id}</td>
            <td><CommitHash commitId={run.commitId} /></td>
            <IfCapability className={pipeline._class} capability={MULTIBRANCH_PIPELINE} >
                <td>{decodeURIComponent(pipeline.name)}</td>
            </IfCapability>
            <td>{changeset && changeset.msg || '-'}</td>
            <td><TimeDuration millis={durationMillis} liveUpdate={running} /></td>
            <td><ReadableDate date={run.endTime} liveUpdate /></td>
            <td>
                <Extensions.Renderer extensionPoint="jenkins.pipeline.activity.list.action" />
                <RunButton className="icon-button" runnable={this.props.pipeline} latestRun={this.props.run} buttonType="stop-only" />
                { /* TODO: check can probably removed and folded into ReplayButton once JENKINS-37519 is done */ }
                <IfCapability className={pipeline._class} capability={[MULTIBRANCH_PIPELINE, SIMPLE_PIPELINE]}>
                    <ReplayButton className="icon-button" runnable={pipeline} latestRun={run} onNavigation={openRunDetails} />
                </IfCapability>
            </td>
        </tr>);
    }
}

Runs.propTypes = {
    run: PropTypes.object,
    pipeline: PropTypes.object,
    result: any.isRequired, // FIXME: create a shape
    data: string,
    changeset: object.isRequired,
};
Runs.contextTypes = {
    pipeline: object,
    router: object.isRequired, // From react-router
    location: object,
};

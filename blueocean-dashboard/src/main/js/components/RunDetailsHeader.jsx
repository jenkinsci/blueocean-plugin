// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';
import { ReadableDate } from '@jenkins-cd/design-language';
import { LiveStatusIndicator } from '@jenkins-cd/design-language';
import { TimeDuration } from '@jenkins-cd/design-language';
import moment from 'moment';

const { object, func } = PropTypes;

class RunDetailsHeader extends Component {
    handleAuthorsClick() {
        if (this.props.onAuthorsClick) {
            this.props.onAuthorsClick();
        }
    }

    handleOrganizationClick() {
        if (this.props.onOrganizationClick) {
            this.props.onOrganizationClick();
        }
    }

    handleNameClick() {
        if (this.props.onNameClick) {
            this.props.onNameClick();
        }
    }

    render() {
        const { data: run } = this.props;
        // Grab author from each change, run through a set for uniqueness
        // FIXME-FLOW: Remove the ":any" cast after completion of https://github.com/facebook/flow/issues/1059
        const authors = [...(new Set(run.changeSet.map(change => change.author.fullName)):any)];
        const status = run.getComputedResult();
        const durationMillis = run.isRunning() ?
            moment().diff(moment(run.startTime)) : run.durationInMillis;
        return (
        <div className="pipeline-result">
            <section className="status inverse">
                <LiveStatusIndicator result={status} startTime={run.startTime}
                  estimatedDuration={run.estimatedDurationInMillis}
                  noBackground
                />
            </section>
            <section className="table">
                <h4>
                    <a onClick={() => this.handleOrganizationClick()}>{run.organization}</a>
                    &nbsp;/&nbsp;
                    <a onClick={() => this.handleNameClick()}>{run.pipeline}</a>
                    &nbsp;
                    #{run.id}
                </h4>

                <div className="row">
                    <div className="commons">
                        <div>
                            <label>Branch</label>
                            <span>{decodeURIComponent(run.pipeline)}</span>
                        </div>
                        { run.commitId ?
                        <div>
                            <label>Commit</label>
                            <span className="commit">
                                #{run.commitId.substring(0, 8)}
                            </span>
                        </div>
                        : null }
                        <div>
                       { authors.length > 0 ?
                                   <a className="authors" onClick={() => this.handleAuthorsClick()}>
                                        Changes by {authors.map(
                                        author => ` ${author}`)}
                                   </a>
                       : 'No changes' }
                        </div>
                    </div>
                    <div className="times">
                        <div>
                            <Icon {...{
                                size: 20,
                                icon: 'timelapse',
                                style: { fill: '#fff' },
                            }} />
                            <TimeDuration millis={durationMillis} liveUpdate={run.isRunning()} />
                        </div>
                        <div>
                            <Icon {...{
                                size: 20,
                                icon: 'access_time',
                                style: { fill: '#fff' },
                            }} />
                            <ReadableDate date={run.endTime} liveUpdate />
                        </div>
                    </div>
                </div>
            </section>
        </div>);
    }
}

RunDetailsHeader.propTypes = {
    data: object.isRequired,
    colors: object,
    onOrganizationClick: func,
    onNameClick: func,
    onAuthorsClick: func,
};

export { RunDetailsHeader };

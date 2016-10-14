// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';
import { ExpandablePath, ReadableDate, LiveStatusIndicator, TimeDuration } from '@jenkins-cd/design-language';
import ChangeSetToAuthors from './ChangeSetToAuthors';
import moment from 'moment';

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
        const { data: run, pipeline } = this.props;
        // pipeline name
        const displayName = decodeURIComponent(run.pipeline);
        const fullDisplayName = pipeline.fullDisplayName;

        // Grab author from each change, run through a set for uniqueness
        // FIXME-FLOW: Remove the ":any" cast after completion of https://github.com/facebook/flow/issues/1059
        const changeSet = run.changeSet;
        const status = run.getComputedResult();
        const durationMillis = run.isRunning() ?
            moment().diff(moment(run.startTime)) : run.durationInMillis;
        const onAuthorsClick = () => this.handleAuthorsClick();
        return (
        <div className="pipeline-result run-details-header">
            <section className="status inverse">
                <LiveStatusIndicator result={status} startTime={run.startTime}
                  estimatedDuration={run.estimatedDurationInMillis}
                  noBackground
                />
            </section>
            <section className="table">
                <h4>
                    <a onClick={() => this.handleOrganizationClick()}>{run.organization}</a>
                    <span>&nbsp;/&nbsp;</span>
                    <a className="path-link" onClick={() => this.handleNameClick()}>
                        <ExpandablePath path={fullDisplayName} iconSize={20} hideFirst />
                    </a>
                    <span>&nbsp;#{run.id}</span>
                </h4>

                <div className="row">
                    <div className="commons">
                        <div>
                            <label>Branch</label>
                            <span>{displayName}</span>
                        </div>
                        { run.commitId ?
                        <div>
                            <label>Commit</label>
                            <span className="commit">
                                {run.commitId.substring(0, 7)}
                            </span>
                        </div>
                        : null }
                        <ChangeSetToAuthors {...{ changeSet, onAuthorsClick }} />
                    </div>
                    <div className="times">
                        <div>
                            <Icon {...{
                                size: 20,
                                icon: 'timelapse',
                                style: { fill: '#fff' },
                            }}
                            />
                            <TimeDuration
                              millis={durationMillis}
                              liveUpdate={run.isRunning()}
                              updatePeriod={1000}
                            />
                        </div>
                        <div>
                            <Icon {...{
                                size: 20,
                                icon: 'access_time',
                                style: { fill: '#fff' },
                            }}
                            />
                            <ReadableDate date={run.endTime} liveUpdate />
                        </div>
                    </div>
                </div>
            </section>
        </div>);
    }
}

const { object, func } = PropTypes;

RunDetailsHeader.propTypes = {
    data: object.isRequired,
    pipeline: object,
    colors: object,
    onOrganizationClick: func,
    onNameClick: func,
    onAuthorsClick: func,
};

export { RunDetailsHeader };

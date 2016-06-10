// @flow

import React, { Component, PropTypes } from 'react';
import {Icon} from 'react-material-icons-blue';
import { ReadableDate } from '../ReadableDate';

import moment from 'moment';

const { object, func } = PropTypes;

class PipelineResult extends Component {
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
        const {
            data: {
                id,
                name,
                organization,
                pipeline,
                changeSet,
                result,
                durationInMillis,
                endTime,
                commitId,

            },
        } = this.props;

        let
            duration = moment.duration(
                Number(durationInMillis), 'milliseconds').humanize();

        // Grab author from each change, run through a set for uniqueness
        // FIXME-FLOW: Remove the ":any" cast after completion of https://github.com/facebook/flow/issues/1059 
        const authors = [...(new Set(changeSet.map(change => change.author.fullName)):any)];

        return (
        <div className="pipeline-result">
            <section className="status">
                <Icon {...{
                    size: 125,
                    icon: iconFromResult(result),
                    style: { fill: "#fff" }
                }} />
            </section>
            <section className="table">
                <h4>
                    <a onClick={() => this.handleOrganizationClick()}>{organization}</a>
                    &nbsp;/&nbsp;
                    <a onClick={() => this.handleNameClick()}>{name}</a>
                    &nbsp;
                    #{id}
                </h4>

                <div className="row">
                    <div className="commons">
                        <div>
                            <label>Branch</label>
                            <span>{decodeURIComponent(pipeline)}</span>
                        </div>
                        { commitId ?
                        <div>
                            <label>Commit</label>
                            <span className="commit">
                                #{commitId.substring(0, 8)}
                            </span>
                        </div>
                        : null }
                        <div>
                       { authors.length > 0 ?
                                   <a className="authors" onClick={() => this.handleAuthorsClick()}>
                                        Changes by {authors.map(
                                        author => ' ' + author)}
                                   </a>
                       : 'No changes' }
                        </div>
                    </div>
                    <div className="times">
                        <div>
                            <Icon {...{
                                size: 25,
                                icon: 'timelapse',
                                style: { fill: "#fff" },
                            }} />
                            <span>{duration}</span>
                        </div>
                        <div>
                            <Icon {...{
                                size: 25,
                                icon: 'access_time',
                                style: { fill: "#fff" },
                            }} />
                            <ReadableDate date={endTime} />
                        </div>
                    </div>
                </div>
            </section>
        </div>);
    }
}

PipelineResult.propTypes = {
    data: object.isRequired,
    colors: object,
    onOrganizationClick: func,
    onNameClick: func,
    onAuthorsClick: func,
};

function iconFromResult(result) {
    switch(result) {
        case "SUCCESS":
        case "UNSTABLE":
            return "done";
        case "FAILURE":
        case "ABORTED":
        case "NOT_BUILT":
            return "close";
        default:
            return "close";
    }
}

export { PipelineResult };

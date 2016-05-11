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
        const authors = [...new Set(changeSet.map(change => change.author.fullName))];

        return (
        <div className="pipeline-result">
            <section className="left">
                { result === 'SUCCESS' && <Icon {...{
                    size: 100,
                    icon: 'done',
                    style: { fill: "#fff" },
                }} />}
                { result === 'FAILURE' &&  <Icon {...{
                    size: 100,
                    icon: 'close',
                    style: { fill: "#fff" },
                }} />}
            </section>
            <section className="table">
                <h4>{organization} / {name} #{id}</h4>

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
                                size: 15,
                                icon: 'timelapse',
                                style: { fill: "#fff" },
                            }} />
                            <span>{duration}</span>
                        </div>
                        <div>
                            <Icon {...{
                                size: 15,
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
    onAuthorsClick: func,
};

export { PipelineResult };

import React, { Component, PropTypes } from 'react';
import {Icon} from 'react-material-icons-blue';

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

        return (<div className="result">
            <section className="left">
                { result === 'SUCCESS' && <Icon {...{
                    size: 125,
                    icon: 'done',
                    style: { fill: "#fff" },
                }} />}
                { result === 'FAILURE' &&  <Icon {...{
                    size: 125,
                    icon: 'close',
                    style: { fill: "#fff" },
                }} />}
            </section>
            <section className="table">
                <h4>{organization} / {name} #{id}</h4>

                <div className="row">
                    <div className="commons">
                        <div>Branch&nbsp;
                            <span className="value">{decodeURIComponent(pipeline)}</span>
                        </div>
                        <div>Commit&nbsp;
                            <span className="value">
                                #{commitId && commitId.substring(0, 8) || '-'}
                            </span>
                        </div>
                        <div>
                           {
                               authors.length > 0 ?
                                   <a className="authors" onClick={() => this.handleAuthorsClick()}>
                                        Changes by {authors.map(
                                        author => ' ' + author)}
                                   </a>
                                   : 'No changes'
                            }
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
                            {moment(endTime).fromNow()}
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

import React, { Component, PropTypes } from 'react';
import {Icons} from 'react-material-icons-blue';

import moment from 'moment';

const { object } = PropTypes;

class PipelineResult extends Component {
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
                { result === 'SUCCESS' && <Icons
                  size={125}
                  icon="done"// Icons in the field transformation
                  style={{ fill: "#fff" }} // Styles prop for icon (svg)
                />}
                { result === 'FAILURE' &&  <Icons
                  size={125}
                  icon="close"// Icons in the field transformation
                  style={{ fill: "#fff" }} // Styles prop for icon (svg)
                />}
            </section>
            <section className="table">
                <h4>{organization} / {name} #{id}</h4>

                <div className="row">
                    <div className="commons">
                        <div>Branch&nbsp;
                            <span className="value">{pipeline}</span>
                        </div>
                        <div>Commit&nbsp;
                            <span className="value">
                                #{commitId && commitId.substring(0, 8) || '-'}
                            </span>
                        </div>
                        <div>
                           {
                               authors.length > 0 ? `Changes by ${authors.map(
                                 author => ' ' + author)}` : 'No changes'
                            }
                        </div>
                    </div>
                    <div className="times">
                        <div>
                            <Icons
                              size={15}
                              icon="timelapse"// Icons in the field transformation
                              style={{ fill: "#fff" }} // Styles prop for icon (svg)
                            />
                            <span>{duration}</span>
                        </div>
                        <div>
                            <Icons
                              size={15}
                              icon="access_time"// Icons in the field transformation
                              style={{ fill: "#fff" }} // Styles prop for icon (svg)
                            />
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
};

export { PipelineResult };

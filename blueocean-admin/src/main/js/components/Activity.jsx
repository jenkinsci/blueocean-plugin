import React, { Component, PropTypes } from 'react';
import ajaxHoc from '../AjaxHoc';
import Table from './Table';
import Runs from './Runs';
import { Link } from 'react-router';
import { urlPrefix } from '../config';
import { ActivityRecord, ChangeSetRecord } from './records';

const { object, array } = PropTypes;

export class Activity extends Component {
    render() {
        const { pipeline, data } = this.props;
        // early out
        if (!data || !pipeline) {
            return null;
        }
        const headers = [
            'Status',
            'Build',
            'Commit',
            { label: 'Branch', className: 'branch' },
            { label: 'Message', className: 'message' },
            { label: 'Duration', className: 'duration' },
            { label: 'Completed', className: 'completed' },
        ];

        let latestRecord = {};
        return (<main>
            <article>
                <Table className="activity-table" headers={headers}>
                    { data.map((run, index) => {
                        let
                        changeset = run.changeSet;
                        if (changeset && changeset.size > 0) {
                            changeset = changeset.toJS();
                            latestRecord = new ChangeSetRecord(changeset[
                                Object.keys(changeset)[0]
                            ]);
                        }
                        const props = {
                            ...pipeline,
                            key: index,
                            changeset: latestRecord,
                            result: new ActivityRecord(run),
                        };
                        return (<Runs {...props} />);
                    })}

                    <tr>
                        <td colSpan={headers.length}>
                            <Link className="btn" to={urlPrefix}>Dashboard</Link>
                        </td>
                    </tr>
                </Table>
            </article>
        </main>);
    }
}

Activity.propTypes = {
    pipeline: object,
    data: array,
};

// Decorated for ajax as well as getting pipeline from context
export default ajaxHoc(Activity, ({ pipeline }, config) => {
    if (!pipeline) return null;
    const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${pipeline.name}`;
    return `${baseUrl}/runs`;
});

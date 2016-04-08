import React, { Component, PropTypes } from 'react';
import ajaxHoc from '../AjaxHoc';
import Table from './Table';
import Runs from './Runs';
import { Link } from 'react-router';
import { urlPrefix } from '../config';
import { ActivityRecord, ChangeSetRecord } from './records';

let baseUrl;
let multiBranch;

export class Activity extends Component {
    render() {
        const { pipeline, data } = this.props;

        if (!data || !pipeline) {
            return null;
        }
        const headers = ['Status', 'Build', 'Commit', 'Branch', 'Message', 'Duration', 'Completed'];

        let latestRecord = {};
        return (<main>
            <article>
                <Table headers={headers}>
                    { data.map((run, index) => {
                        let
                        changeset = run.changeSet;
                        if (changeset && changeset.size > 0) {
                            changeset = changeset.toJS();
                            latestRecord = new ChangeSetRecord(
                                changeset[Object.keys(changeset)[0]]);
                        }
                        return (<Runs
                          baseUrl={baseUrl}
                          multiBranch={multiBranch}
                          key={index}
                          changeset={latestRecord}
                          result={new ActivityRecord(run)}
                        />);
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
    pipeline: PropTypes.object,
    data: PropTypes.array,
};

// Decorated for ajax as well as getting pipeline from context
export default ajaxHoc(Activity, (props, config) => {
    if (!props.pipeline) return null;
    multiBranch = !!pipeline.branchNames;
    baseUrl =`${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${pipeline.name}`;
    return `${baseUrl}/runs`;
});

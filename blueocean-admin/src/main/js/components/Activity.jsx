import React, { Component, PropTypes } from 'react';
import Table from './Table';
import Runs from './Runs';
import { ActivityRecord, ChangeSetRecord } from './records';
import { actions, currentRuns as runsSelector, ACTION_TYPES, createSelector, connect } from '../redux';

const { object, array, func } = PropTypes;

export class Activity extends Component {
    componentWillMount() {
        if (this.context.params) {
            const pipeId = this.context.params.pipeline;
            const baseUrl = `${this.context.config.getAppURLBase()}/rest/organizations/jenkins` +
            `/pipelines/${this.context.params.pipeline}/runs`;
            this.props.generateData(baseUrl, ACTION_TYPES.SET_RUNS_DATA, { id: pipeId });
        }
    }
    render() {
        const { runs } = this.props;
        console.log(runs)
        // early out
        if (!runs) {
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
                    { runs.map((run, index) => {
                        const changeset = run.changeSet;
                        if (changeset && changeset.length > 0) {
                            latestRecord = new ChangeSetRecord(changeset[
                                Object.keys(changeset)[0]
                            ]);
                        }
                        const props = {
                            key: index,
                            changeset: latestRecord,
                            result: new ActivityRecord(run),
                        };
                        return (<Runs {...props} />);
                    })}
                </Table>
            </article>
        </main>);
    }
}

Activity.contextTypes = {
    params: object.isRequired,
    config: object.isRequired,
};

Activity.propTypes = {
    runs: array,
    generateData: func,
};
/*
const selectors = createSelector([runsSelector], (runs) => ({ runs }));

export default connect(selectors, actions)(Activity);
*/
export default Activity;

// Decorated for ajax as well as getting pipeline from context
/*
export default fetch(Activity, ({ pipeline }, config) => {
    if (!pipeline) return null;
    const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${pipeline.name}`;
    return `${baseUrl}/runs`;
});
*/

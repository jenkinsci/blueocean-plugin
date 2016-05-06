import React, { Component, PropTypes } from 'react';
import Table from './Table';
import Runs from './Runs';
import { ActivityRecord, ChangeSetRecord } from './records';
import {
    actions,
    currentRuns as runsSelector,
    createSelector,
    connect,
} from '../redux';

const { object, array, func } = PropTypes;

export class Activity extends Component {
    componentWillMount() {
        if (this.context.config && this.context.params) {
            const {
                params: {
                    pipeline,
                },
                config = {},
            } = this.context;
            config.pipeline = pipeline;
            this.props.fetchRunsIfNeeded(config);
        }
    }
    render() {
        const { runs } = this.props;
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
    fetchRunsIfNeeded: func,
};

const selectors = createSelector([runsSelector], (runs) => ({ runs }));

export default connect(selectors, actions)(Activity);

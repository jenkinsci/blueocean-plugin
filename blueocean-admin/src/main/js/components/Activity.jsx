import React, { Component, PropTypes } from 'react';
import { EmptyStateView, fetch } from '@jenkins-cd/design-language';
import Table from './Table';
import Runs from './Runs';
import { ActivityRecord, ChangeSetRecord } from './records';

const { object, array } = PropTypes;

export class Activity extends Component {

    renderEmptyState(repoName) {
        return (
            <EmptyStateView iconName="shoes">
                <h1>Ready, get set...</h1>

                <p>
                    Hmm, looks like there are no runs in this pipeline’s history.
                </p>

                <p>
                    Commit to the repository <em>{repoName}</em> or run the pipeline manually.
                </p>

                <button>Run Now</button>
            </EmptyStateView>
        );
    }

    render() {
        const { pipeline, data } = this.props;

        // render empty view while data loads
        if (!data || !pipeline) {
            return null;
        }

        if (!data.length) {
            return this.renderEmptyState(pipeline.name);
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
                        const changeset = run.changeSet;
                        if (changeset && changeset.length > 0) {
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
export default fetch(Activity, ({ pipeline }, config) => {
    if (!pipeline) return null;
    const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${pipeline.name}`;
    return `${baseUrl}/runs`;
});

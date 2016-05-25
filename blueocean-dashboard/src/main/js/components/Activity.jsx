import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import Table from './Table';
import Runs from './Runs';
import { scrollToHash } from './ScrollToHash';
import { ActivityRecord, ChangeSetRecord } from './records';
import {
    actions,
    currentRuns as runsSelector,
    createSelector,
    connect,
} from '../redux';

const { object, array, func, string } = PropTypes;

const EmptyState = ({ repoName }) => (
    <main>
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
    </main>
);

EmptyState.propTypes = {
    repoName: string,
};

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

        if (!runs.length) {
            return (<EmptyState repoName={this.context.params.pipeline} />);
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
    location: object.isRequired,
    config: object.isRequired,
};

Activity.propTypes = {
    runs: array,
    fetchRunsIfNeeded: func,
};

const selectors = createSelector([runsSelector], (runs) => ({ runs }));

export default connect(selectors, actions)(scrollToHash(Activity));

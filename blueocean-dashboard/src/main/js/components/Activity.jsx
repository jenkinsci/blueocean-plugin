import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import Runs from './Runs';
import Pipeline from '../api/Pipeline';
import { ActivityRecord, ChangeSetRecord } from './records';
import RunPipeline from './RunPipeline.jsx';
import {
    actions,
    currentRuns as runsSelector,
    createSelector,
    connect,
} from '../redux';

const { object, array, func, string, bool } = PropTypes;

const EmptyState = ({ repoName, pipeline, showRunButton }) => (
    <main>
        <EmptyStateView iconName="shoes">
            <h1>Ready, get set...</h1>

            <p>
                Hmm, looks like there are no runs in this pipeline’s history.
            </p>

            <p>
                Commit to the repository <em>{repoName}</em> or run the pipeline manually.
            </p>

            {showRunButton && <RunNonMultiBranchPipeline pipeline={pipeline} buttonText="Run Now" />}
        </EmptyStateView>
    </main>
);

EmptyState.propTypes = {
    repoName: string,
    pipeline: object,
    showRunButton: bool,
};

const RunNonMultiBranchPipeline = ({ pipeline, buttonText }) => (
    <RunPipeline organization={pipeline.organization} pipeline={pipeline.fullName} buttonClass="btn-primary inverse non-multi-branch" buttonText={buttonText} />
);

RunNonMultiBranchPipeline.propTypes = {
    pipeline: object,
    buttonText: string,
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
        const { runs, pipeline } = this.props;
        // early out
        if (!runs) {
            return null;
        }

        // Only show the Run button for non multi-branch pipelines.
        // Multi-branch pipelines have the Run/play button beside them on
        // the Branches/PRs tab.
        const showRunButton = (pipeline && !Pipeline.isMultibranch(pipeline));


        if (!runs.length) {
            return (<EmptyState repoName={this.context.params.pipeline} showRunButton={showRunButton} pipeline={pipeline} />);
        }

        // We only want to show branch column for multibranch
        const showBranchCol = (pipeline && Pipeline.isMultibranch(pipeline));
        const headers = showBranchCol ? [
            'Status',
            'Build',
            'Commit',
            { label: 'Branch', className: 'branch' },
            { label: 'Message', className: 'message' },
            { label: 'Duration', className: 'duration' },
            { label: 'Completed', className: 'completed' },
            { label: '', className: 'actions' },
        ] : [
            'Status',
            'Build',
            'Commit',
            { label: 'Message', className: 'message' },
            { label: 'Duration', className: 'duration' },
            { label: 'Completed', className: 'completed' },
            { label: '', className: 'actions' },
        ];


        return (<main>
            <article className="activity">
                {showRunButton && <RunNonMultiBranchPipeline pipeline={pipeline} buttonText="Run" />}
                <Table className="activity-table fixed" headers={headers}>
                    { runs.map((run, index) => {
                        const changeset = run.changeSet;
                        let latestRecord = {};
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
    pipeline: object,
    fetchRunsIfNeeded: func,
};

const selectors = createSelector([runsSelector], (runs) => ({ runs }));

export default connect(selectors, actions)(Activity);

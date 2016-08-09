import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table, Progress } from '@jenkins-cd/design-language';
import Runs from './Runs';
import Pipeline from '../api/Pipeline';
import { RunRecord, ChangeSetRecord } from './records';
import RunPipeline from './RunPipeline.jsx';
import {
    actions,
    currentRuns as runsSelector,
    createSelector,
    connect,
} from '../redux';
import PageLoading from './PageLoading';

const { object, array, func, string, bool, any } = PropTypes;

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
                    organization,
                },
                config = {},
            } = this.context;

            config.pipeline = pipeline;
            config.organization = organization;
            this.props.fetchRuns(config);
        }
    }

    render() {
        const { currentRuns, pipeline } = this.props;

        if (!currentRuns || pipeline.$pending) {
            return <PageLoading />;
        }

        // Only show the Run button for non multi-branch pipelines.
        // Multi-branch pipelines have the Run/play button beside them on
        // the Branches/PRs tab.
        const showRunButton = (pipeline && !Pipeline.isMultibranch(pipeline));

        if (currentRuns.$success && !currentRuns.length) {
            return (<EmptyState repoName={this.context.params.pipeline} showRunButton={showRunButton} pipeline={pipeline} />);
        }

        const headers = [
            'Status',
            'Build',
            'Commit',
            { label: 'Branch', className: 'branch' },
            { label: 'Message', className: 'message' },
            { label: 'Duration', className: 'duration' },
            { label: 'Completed', className: 'completed' },
            { label: '', className: 'actions' },
        ];

        
        return (<main>
            {currentRuns.$pending && <Progress />}
            <article className="activity">
                {showRunButton && <RunNonMultiBranchPipeline pipeline={pipeline} buttonText="Run" />}
                <Table className="activity-table fixed" headers={headers}>
                    {
                        currentRuns.map((run, index) => {
                            const changeset = run.changeSet;
                            let latestRecord = {};
                            if (changeset && changeset.length > 0) {
                                latestRecord = new ChangeSetRecord(changeset[
                                    Object.keys(changeset)[0]
                                ]);
                            }

                            return (<Runs {...{
                                key: index,
                                changeset: latestRecord,
                                result: new RunRecord(run) }} />);
                        })
                    }
                </Table>
                {currentRuns.$pager &&
                    <button disabled={!currentRuns.$pager.hasMore} className="btn-show-more btn-secondary" onClick={() => currentRuns.$pager.fetchMore()}>
                        {currentRuns.$pending ? 'Loading...' : 'Show More'}
                    </button>
                }
            </article>
            {this.props.children}
        </main>);
    }
}

Activity.contextTypes = {
    params: object.isRequired,
    location: object.isRequired,
    pipeline: object,
    config: object.isRequired,
};

Activity.propTypes = {
    currentRuns: array,
    pipeline: object,
    fetchRuns: func,
    children: any,
};

const selectors = createSelector([runsSelector], (currentRuns) => ({ currentRuns }));

export default connect(selectors, actions)(Activity);

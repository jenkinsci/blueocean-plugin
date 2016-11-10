import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import { RunButton, activityService } from '@jenkins-cd/blueocean-core-js';
import Runs from './Runs';
import { RunRecord, ChangeSetRecord } from './records';
import {
    actions,
    currentRuns as currentRunsSelector,
    createSelector,
    connect,
} from '../redux';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { capabilityStore } from './Capability';
import { observer } from 'mobx-react';
import { paginateUrl } from '../util/UrlUtils';
import UrlConfig from '../config';


const { object, array, func, string, bool } = PropTypes;

const EmptyState = ({ repoName, pipeline, showRunButton, onNavigation }) => (
    <main>
        <EmptyStateView iconName="shoes">
            <h1>Ready, get set...</h1>

            <p>
                Hmm, looks like there are no runs in this pipeline’s history.
            </p>

            <p>
                Commit to the repository <em>{repoName}</em> or run the pipeline manually.
            </p>

        { showRunButton &&
            <RunButton
              runnable={pipeline}
              buttonType="run-only"
              runLabel="Run Now"
              onNavigation={onNavigation}
            />
        }
        </EmptyStateView>
    </main>
);

EmptyState.propTypes = {
    repoName: string,
    pipeline: object,
    showRunButton: bool,
    onNavigation: func,
};
@observer
export class Activity extends Component {

    componentWillMount() {
        if (this.context.config && this.context.params) {
            const organization = this.context.params.organization;
            const pipeline = this.context.params.pipeline;
           
            this.pager = activityService.activityPager(organization, pipeline);

        }
    }

    render() {
        const { pipeline } = this.props;
        const runs = this.pager.data;
        if (!runs || !pipeline) {
            return null;
        }
        
        const { capabilities } = this.props;
        const isMultiBranchPipeline = capabilities[pipeline._class].contains(MULTIBRANCH_PIPELINE);

        // Only show the Run button for non multi-branch pipelines.
        // Multi-branch pipelines have the Run/play button beside them on
        // the Branches/PRs tab.
        const showRunButton = !isMultiBranchPipeline;

        const onNavigation = (url) => {
            this.context.location.pathname = url;
            this.context.router.push(this.context.location);
        };

        if (!runs.length) {
            return (<EmptyState repoName={this.context.params.pipeline} showRunButton={showRunButton} pipeline={pipeline} />);
        }

        const latestRun = runs[0];

        const headers = isMultiBranchPipeline ? [
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
                { showRunButton &&
                <RunButton
                  runnable={pipeline}
                  latestRun={latestRun}
                  buttonType="run-only"
                  onNavigation={onNavigation}
                />
                }
                {runs.length > 0 &&
                <Table className="activity-table fixed" headers={headers}>
                    {
                        runs.map((run, index) => {
                            const changeset = run.changeSet;
                            let latestRecord = {};
                            if (changeset && changeset.length > 0) {
                                latestRecord = new ChangeSetRecord(changeset[
                                    Object.keys(changeset)[changeset.length - 1]
                                ]);
                            }

                            return (
                                <Runs {...{
                                    key: index,
                                    run,
                                    pipeline,
                                    changeset: latestRecord,
                                }}
                                />
                            );
                        })
                    }
                </Table>
                }
                {runs && runs.length > 0 &&
                <button disabled={this.pager.pending || !this.pager.hasMore} className="btn-show-more btn-secondary" onClick={() => this.pager.fetchNextPage()}>
                    {this.pager.pending ? 'Loading...' : 'Show More'}
                </button>
                }
            </article>
        </main>);
    }
}

Activity.contextTypes = {
    params: object.isRequired,
    location: object.isRequired,
    pipeline: object,
    config: object.isRequired,
    router: object.isRequired,
};

Activity.propTypes = {
    runs: array,
    pipeline: object,
    capabilities: object,
    fetchRuns: func,
};

const selectors = createSelector([currentRunsSelector], (runs) => ({ runs }));

export default connect(selectors, actions)(capabilityStore(props => props.pipeline._class)(Activity));

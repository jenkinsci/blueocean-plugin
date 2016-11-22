import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import { RunButton } from '@jenkins-cd/blueocean-core-js';
import Markdown from 'react-remarkable';
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

const { object, array, func, string, bool } = PropTypes;

const EmptyState = ({ repoName, pipeline, showRunButton, onNavigation, t }) =>
    (<main>
        <EmptyStateView iconName="shoes">
            <Markdown>
                {t('EmptyState.activity', {
                    0: repoName,
                    defaultValue: '# Ready, get set...\nHmm, looks like there are no runs in this pipeline\u2019s history.\n\nCommit to the repository _{0}_ or run the pipeline manually.',
                })}
            </Markdown>
            { showRunButton &&
                <RunButton
                  runnable={pipeline}
                  buttonType="run-only"
                  runLabel={ t('pipelinedetail.activity.button.run', { defaultValue: 'Run now' }) }
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
    t: func,
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
        const { runs, pipeline, t, locale } = this.props;

        if (!runs || !pipeline || pipeline.$pending) {
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

        if (runs.$success && !runs.length) {
            return (<EmptyState
              repoName={this.context.params.pipeline}
              showRunButton={showRunButton}
              pipeline={pipeline}
              t={t}
            />);
        }

        const latestRun = runs[0];
        const head = 'pipelinedetail.activity.header';

        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const build = t(`${head}.build`, { defaultValue: 'Build' });
        const commit = t(`${head}.commit`, { defaultValue: 'Commit' });
        const message = t(`${head}.message`, { defaultValue: 'Message' });
        const duration = t(`${head}.duration`, { defaultValue: 'Duration' });
        const completed = t(`${head}.completed`, { defaultValue: 'Completed' });
        const branch = t(`${head}.branch`, { defaultValue: 'Branch' });
        const headers = isMultiBranchPipeline ? [
            status,
            build,
            commit,
            { label: branch, className: 'branch' },
            { label: message, className: 'message' },
            { label: duration, className: 'duration' },
            { label: completed, className: 'completed' },
            { label: '', className: 'actions' },
        ] : [
            status,
            build,
            commit,
            { label: message, className: 'message' },
            { label: duration, className: 'duration' },
            { label: completed, className: 'completed' },
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
                <Table className="activity-table fixed" headers={headers} disableDefaultPadding>
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
                                    t,
                                    locale,
                                    run,
                                    pipeline,
                                    key: index,
                                    changeset: latestRecord,
                                    result: new RunRecord(run),
                                }}
                                />
                            );
                        })
                    }
                </Table>
                }
                {runs.$pager && runs.length > 0 &&
                <button disabled={runs.$pending || !runs.$pager.hasMore} className="btn-show-more btn-secondary" onClick={() => runs.$pager.fetchMore()}>
                    {runs.$pending ? t('common.pager.loading', { defaultValue: 'Loading...' }) : t('common.pager.more', { defaultValue: 'Show more' })}
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
    locale: string,
    t: func,
};

const selectors = createSelector([currentRunsSelector], (runs) => ({ runs }));

export default connect(selectors, actions)(capabilityStore(props => props.pipeline._class)(Activity));

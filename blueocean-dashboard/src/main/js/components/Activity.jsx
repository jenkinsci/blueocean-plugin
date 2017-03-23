import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import { capable, RunButton, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import Markdown from 'react-remarkable';
import { observer } from 'mobx-react';

import Runs from './Runs';
import { ChangeSetRecord } from './records';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { buildPipelineUrl } from '../util/UrlUtils';
import { ColumnFilter } from './ColumnFilter';
import { NoBranchesPlaceholder } from './placeholder/NoBranchesPlaceholder';
import { NoRunsPlaceholder } from './placeholder/NoRunsPlaceholder';

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


@observer
export class Activity extends Component {

    componentWillMount() {
        if (this.context.params) {
            const organization = this.context.params.organization;
            const pipeline = this.context.params.pipeline;
            const branch = this.context.params.branch;
            this.pager = this.context.activityService.activityPager(organization, pipeline, branch);
        }
    }

    componentWillReceiveProps(newProps) {
        if (this.props.params && this.props.params.branch !== newProps.params.branch) {
            const organization = newProps.params.organization;
            const pipeline = newProps.params.pipeline;
            const branch = newProps.params.branch;
            this.pager = this.context.activityService.activityPager(organization, pipeline, branch);
        }
    }

    navigateToBranch(branch) {
        const organization = this.context.params.organization;
        const pipeline = this.context.params.pipeline;
        const baseUrl = buildPipelineUrl(organization, pipeline);
        let activitiesURL = `${baseUrl}/activity`;
        if (branch) {
            activitiesURL += '/' + encodeURIComponent(branch);
        }
        this.context.router.push(activitiesURL);
    }

    render() {
        const { pipeline, t, locale } = this.props;
        const runs = this.pager.data;
        if (!pipeline) {
            return null;
        }
        const { branch } = this.context.params;
        const isMultiBranchPipeline = capable(pipeline, MULTIBRANCH_PIPELINE);
        const hasBranches = pipeline.branchNames && !!pipeline.branchNames.length;

        // Only show the Run button for non multi-branch pipelines.
        // Multi-branch pipelines have the Run/play button beside them on
        // the Branches/PRs tab.
        const showRunButton = !isMultiBranchPipeline;

        if (!this.pager.pending) {
            if (isMultiBranchPipeline && !hasBranches) {
                return <NoBranchesPlaceholder t={t} />;
            }
            if (!runs || !runs.length) {
                if (hasBranches) {
                    const { params } = this.context;
                    const branchesUrl = buildPipelineUrl(params.organization, params.pipeline, 'branches');
                    return <NoRunsPlaceholder t={t} linkUrl={branchesUrl} />;
                }
                // TOOD: what do we here?
            }
        }

        const onNavigation = (url) => {
            this.context.location.pathname = url;
            this.context.router.push(this.context.location);
        };

        const latestRun = runs[0];
        const head = 'pipelinedetail.activity.header';

        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const runHeader = t(`${head}.run`, { defaultValue: 'Run' });
        const commit = t(`${head}.commit`, { defaultValue: 'Commit' });
        const message = t(`${head}.message`, { defaultValue: 'Message' });
        const duration = t(`${head}.duration`, { defaultValue: 'Duration' });
        const completed = t(`${head}.completed`, { defaultValue: 'Completed' });
        const branchText = t(`${head}.branch`, { defaultValue: 'Branch' });

        const branchFilter = isMultiBranchPipeline && (<ColumnFilter placeholder={branchText} value={branch}
            onChange={b => this.navigateToBranch(b)}
            options={pipeline.branchNames.map(b => decodeURIComponent(b))}
        />);

        const headers = isMultiBranchPipeline ? [
            status,
            runHeader,
            commit,
            { label: branchFilter, className: 'branch' },
            { label: message, className: 'message' },
            { label: duration, className: 'duration' },
            { label: completed, className: 'completed' },
            { label: '', className: 'actions' },
        ] : [
            status,
            runHeader,
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
                      buttonType="run-only"
                      innerButtonClasses="btn-secondary"
                      runnable={pipeline}
                      latestRun={latestRun}
                      onNavigation={onNavigation}
                    />
                }
                { runs.length > 0 &&
                    <Table className="activity-table u-highlight-rows u-table-lr-indents" headers={headers} disableDefaultPadding key={branch}>
                        {
                            runs.map((run, index) => {
                                const changeset = run.changeSet;
                                let latestRecord = {};

                                if (changeset && changeset.length > 0) {
                                    latestRecord = new ChangeSetRecord(changeset[changeset.length - 1]);
                                }

                                return (
                                    <Runs {...{
                                        t,
                                        locale,
                                        run,
                                        pipeline,
                                        key: index,
                                        changeset: latestRecord,
                                    }}
                                    />
                                );
                            })
                        }
                    </Table>
                }

                { runs && runs.length > 0 &&
                  <ShowMoreButton pager={this.pager} />
                }
            </article>
        </main>);
    }
}

Activity.contextTypes = {
    params: object.isRequired,
    location: object.isRequired,
    config: object.isRequired,
    router: object.isRequired,
    activityService: object.isRequired,
};

Activity.propTypes = {
    runs: array,
    pipeline: object,
    locale: string,
    t: func,
    params: object,
};

export default Activity;

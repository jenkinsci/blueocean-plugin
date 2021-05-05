import React, { Component, PropTypes } from 'react';
import { JTable, TableRow, TableHeader, TableCell, Icon } from '@jenkins-cd/design-language';
import { capable, RunButton, ShowMoreButton, DisablePipelineButton } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { ActivityDetailsRow } from './ActivityDetailsRow';
import { ChangeSetRecord } from './records';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { UrlBuilder } from '@jenkins-cd/blueocean-core-js';
import { ColumnFilter } from './ColumnFilter';
import { NoBranchesPlaceholder } from './placeholder/NoBranchesPlaceholder';
import { NoRunsDefaultPlaceholder, NoRunsForBranchPlaceholder, NoRunsMultibranchPlaceholder } from './placeholder/NoRunsPlaceholder';

import Extensions from '@jenkins-cd/js-extensions';

const { object, array, func, string } = PropTypes;

function extractLatestRecord(run) {
    const changeset = run.changeSet;
    let latestRecord = {};

    if (changeset && changeset.length > 0) {
        latestRecord = new ChangeSetRecord(changeset[changeset.length - 1]);
    }

    return [run, latestRecord];
}

@observer
export class Activity extends Component {
    state = {
        actionExtensionCount: 0,
        pipelineDisabled: this.props.pipeline.disabled,
    };

    componentWillMount() {
        if (this.context.params) {
            const organization = this.context.params.organization;
            const pipeline = this.context.params.pipeline;
            const branch = this._branchFromProps(this.props);
            this.pager = this.context.activityService.activityPager(organization, pipeline, branch);
        }
        this._countExtensions();
    }

    componentWillReceiveProps(newProps) {
        if (this.props.params && this._branchFromProps(this.props) !== this._branchFromProps(newProps)) {
            const organization = newProps.params.organization;
            const pipeline = newProps.params.pipeline;
            const branch = this._branchFromProps(newProps);
            this.pager = this.context.activityService.activityPager(organization, pipeline, branch);
        }
    }

    // Figure out how many extensions we have for the action buttons column so we can size it appropriately
    _countExtensions() {
        Extensions.store.getExtensions('jenkins.pipeline.activity.list.action', extensions => {
            const count = extensions && typeof extensions.length === 'number' ? extensions.length : 0;
            if (count !== this.state.actionExtensionCount) {
                this.setState({ actionExtensionCount: count });
            }
        });
    }

    _branchFromProps(props) {
        return ((props.location || {}).query || {}).branch;
    }

    navigateToBranch = branch => {
        const organization = this.context.params.organization;
        const pipeline = this.context.params.pipeline;
        const baseUrl = UrlBuilder.buildPipelineUrl(organization, pipeline);
        let activitiesURL = `${baseUrl}/activity`;
        if (branch) {
            activitiesURL += '?branch=' + encodeURIComponent(branch);
        }
        this.context.router.push(activitiesURL);
    };

    createPipeline() {
        const { organization, pipeline } = this.context.params;
        const url = `/organizations/${organization}/pipeline-editor/${pipeline}/`;
        this.context.router.push(url);
    }

    render() {
        const { pipeline, t, locale } = this.props;
        const { actionExtensionCount } = this.state;
        const actionsInRowCount = ActivityDetailsRow.actionItemsCount; // Non-extension actions

        if (!pipeline) {
            return null;
        }

        const runs = this.pager.data;
        const isLoading = this.pager.pending;
        const branch = this._branchFromProps(this.props);

        const isMultiBranchPipeline = capable(pipeline, MULTIBRANCH_PIPELINE);
        const hasBranches = pipeline.branchNames && !!pipeline.branchNames.length;

        const disableablePipeline = pipeline.disabled !== undefined && pipeline.disabled !== null ? true : false;

        const onNavigation = url => {
            this.context.location.pathname = url;
            this.context.router.push(this.context.location);
        };

        const onChangeDisableState = newDisableState => {
            this.setState({ pipelineDisabled: newDisableState });
        };

        const latestRun = runs && runs[0];

        // Only show the Run button for non multi-branch pipelines.
        // Multi-branch pipelines have the Run/play button beside them on
        // the Branches/PRs tab.
        let runButton = null;

        if (!isMultiBranchPipeline) {
            if (this.state.pipelineDisabled) {
                runButton = (
                    <span className="pipeline-disabled-label">
                        <Icon size={24} icon="ActionInfoOutline" style={{ marginRight: '5px' }} />
                        <span>{t('pipelinedetail.activity.header.disabled.pipeline', { defaultValue: 'This Pipeline is currently disabled' })}</span>
                    </span>
                );
            } else {
                runButton = (
                    <RunButton buttonType="run-only" innerButtonClasses="btn-secondary" runnable={pipeline} latestRun={latestRun} onNavigation={onNavigation} />
                );
            }
        }

        if (!isLoading) {
            if (isMultiBranchPipeline && !hasBranches) {
                return (
                    <NoBranchesPlaceholder
                        t={t}
                        primaryAction={
                            <button className="btn btn-primary" onClick={() => this.createPipeline()}>
                                {t('creation.git.step1.create_button')}
                            </button>
                        }
                    />
                );
            }
            if (!runs || !runs.length) {
                if (!isMultiBranchPipeline) {
                    return <NoRunsDefaultPlaceholder t={t} runButton={runButton} />;
                } else if (!branch) {
                    const { params } = this.context;
                    const branchesUrl = UrlBuilder.buildPipelineUrl(params.organization, params.pipeline, 'branches');
                    return <NoRunsMultibranchPlaceholder t={t} branchName={branch} branchesUrl={branchesUrl} />;
                }
            }
        }

        const showTable = branch || (runs && runs.length > 0);
        const head = 'pipelinedetail.activity.header';

        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const runHeader = t(`${head}.run`, { defaultValue: 'Run' });
        const commit = t(`${head}.commit`, { defaultValue: 'Commit' });
        const message = t(`${head}.message`, { defaultValue: 'Message' });
        const duration = t(`${head}.duration`, { defaultValue: 'Duration' });
        const completed = t(`${head}.completed`, { defaultValue: 'Completed' });
        const branchText = t(`${head}.branch`, { defaultValue: 'Branch' });
        const decodedBranchName = branch ? decodeURIComponent(branch) : branch;

        const branchFilter = isMultiBranchPipeline && (
            <ColumnFilter
                placeholder={branchText}
                value={decodedBranchName}
                onChange={this.navigateToBranch}
                options={pipeline.branchNames.map(b => decodeURIComponent(b))}
            />
        );

        // Build up our column metadata

        const columns = [JTable.column(60, status, false), JTable.column(60, runHeader, false), JTable.column(60, commit, false)];

        if (isMultiBranchPipeline) {
            columns.push(JTable.column(160, 'branches', false));
        }

        columns.push(
            JTable.column(480, message, true),
            JTable.column(100, duration, false),
            JTable.column(100, completed, false),
            JTable.column((actionExtensionCount + actionsInRowCount) * 24, '', false)
        );

        // Build main display table

        const runsTable = showTable && (
            <JTable columns={columns} className="activity-table">
                <TableRow>
                    <TableHeader>{status}</TableHeader>
                    <TableHeader>{runHeader}</TableHeader>
                    <TableHeader>{commit}</TableHeader>
                    {isMultiBranchPipeline && <TableCell>{branchFilter}</TableCell>}
                    <TableHeader>{message}</TableHeader>
                    <TableHeader>{duration}</TableHeader>
                    <TableHeader>{completed}</TableHeader>
                    <TableHeader />
                </TableRow>
                {runs
                    .map(extractLatestRecord)
                    .map(([run, changeset], index) => (
                        <ActivityDetailsRow
                            t={t}
                            locale={locale}
                            run={run}
                            pipeline={pipeline}
                            key={index}
                            changeset={changeset}
                            isMultibranch={isMultiBranchPipeline}
                        />
                    ))}
            </JTable>
        );

        return (
            <main>
                <article className="activity">
                    <div className="activity-actions-container">
                        {runButton}
                        {disableablePipeline && (
                            <DisablePipelineButton t={t} innerButtonClasses="btn-secondary" pipeline={pipeline} onChangeDisableState={onChangeDisableState} />
                        )}
                    </div>
                    {runsTable}
                    {!isLoading && !runs.length && branch && <NoRunsForBranchPlaceholder t={t} branchName={branch} />}
                    {runs && runs.length > 0 && <ShowMoreButton pager={this.pager} />}
                </article>
            </main>
        );
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
    location: object.isRequired,
};

export default Activity;

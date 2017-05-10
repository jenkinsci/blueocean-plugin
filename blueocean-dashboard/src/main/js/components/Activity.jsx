import React, { Component, PropTypes } from 'react';
import { Table } from '@jenkins-cd/design-language';
import { capable, RunButton, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';

import Runs from './Runs';
import { ChangeSetRecord } from './records';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { buildPipelineUrl } from '../util/UrlUtils';
import { ColumnFilter } from './ColumnFilter';
import { NoBranchesPlaceholder } from './placeholder/NoBranchesPlaceholder';
import {
    NoRunsDefaultPlaceholder,
    NoRunsForBranchPlaceholder,
    NoRunsMultibranchPlaceholder,
} from './placeholder/NoRunsPlaceholder';


const { object, array, func, string } = PropTypes;


@observer
export class Activity extends Component {
    componentWillMount() {
        if (this.context.params) {
            const organization = this.context.params.organization;
            const pipeline = this.context.params.pipeline;
            const branch = this._branchFromProps(this.props);
            this.pager = this.context.activityService.activityPager(organization, pipeline, branch);
        }
    }

    componentWillReceiveProps(newProps) {
        if (this.props.params && this._branchFromProps(this.props) !== this._branchFromProps(newProps)) {
            const organization = newProps.params.organization;
            const pipeline = newProps.params.pipeline;
            const branch = this._branchFromProps(newProps);
            this.pager = this.context.activityService.activityPager(organization, pipeline, branch);
        }
    }

    _branchFromProps(props) {
        return ((props.location || {}).query || {}).branch;
    }

    navigateToBranch(branch) {
        const organization = this.context.params.organization;
        const pipeline = this.context.params.pipeline;
        const baseUrl = buildPipelineUrl(organization, pipeline);
        let activitiesURL = `${baseUrl}/activity`;
        if (branch) {
            activitiesURL += '?branch=' + encodeURIComponent(branch);
        }
        this.context.router.push(activitiesURL);
    }

    render() {
        const { pipeline, t, locale } = this.props;
        const runs = this.pager.data;
        const isLoading = this.pager.pending;

        if (!pipeline) {
            return null;
        }
        const branch = this._branchFromProps(this.props);

        const isMultiBranchPipeline = capable(pipeline, MULTIBRANCH_PIPELINE);
        const hasBranches = pipeline.branchNames && !!pipeline.branchNames.length;

        const onNavigation = (url) => {
            this.context.location.pathname = url;
            this.context.router.push(this.context.location);
        };

        const latestRun = runs && runs[0];
        // Only show the Run button for non multi-branch pipelines.
        // Multi-branch pipelines have the Run/play button beside them on
        // the Branches/PRs tab.
        const runButton = !isMultiBranchPipeline && (
            <RunButton
                buttonType="run-only"
                innerButtonClasses="btn-secondary"
                runnable={pipeline}
                latestRun={latestRun}
                onNavigation={onNavigation}
            />
        );

        if (!isLoading) {
            if (isMultiBranchPipeline && !hasBranches) {
                return <NoBranchesPlaceholder t={t} />;
            }
            if (!runs || !runs.length) {
                if (!isMultiBranchPipeline) {
                    return <NoRunsDefaultPlaceholder t={t} runButton={runButton} />;
                } else if (!branch) {
                    const { params } = this.context;
                    const branchesUrl = buildPipelineUrl(params.organization, params.pipeline, 'branches');
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
                { runButton }
                { showTable &&
                <Table className="activity-table u-highlight-rows u-table-lr-indents" headers={headers} disableDefaultPadding key={branch}>
                    {
                        runs.length > 0 && runs.map((run, index) => {
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
                { !isLoading && !runs.length && branch &&
                    <NoRunsForBranchPlaceholder t={t} branchName={branch} />
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
    location: object.isRequired,
};

export default Activity;

import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import Markdown from 'react-remarkable';
import Branches from './Branches';

import PageLoading from './PageLoading';
import { pipelineBranchesUnsupported } from './PipelinePage';
import { capable } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';

const { object, string, any, func } = PropTypes;

const EmptyState = ({ repoName, t }) => (
    <main>
        <EmptyStateView iconName="branch">
            <Markdown>
                {t('EmptyState.branches', {
                    0: repoName,
                    defaultValue: '# Branch out\nCreate a branch in the repository _{0}_ and Jenkins will start testing your changes.\n\nGive it a try and become a hero to your team.',
                })}
            </Markdown>
            <button>{t('Enable', { defaultValue: 'Enable' })}</button>
        </EmptyStateView>
    </main>
);

const NotSupported = ({ t }) => (
    <main>
        <EmptyStateView>
            <Markdown>
                {t('EmptyState.branches.notSupported', { defaultValue: '# Branches are unsupported\nBranch builds only work with the _Multibranch Pipeline_ job type. This is just one of the many reasons to switch to Jenkins Pipeline.\n\n[Learn more](https://jenkins.io/doc/book/pipeline-as-code/)' })}
            </Markdown>
        </EmptyStateView>
    </main>
);

EmptyState.propTypes = {
    repoName: string,
    t: func,
};

NotSupported.propTypes = {
    t: func,
};

@observer
export class MultiBranch extends Component {
    componentWillMount() {
        if (this.props.pipeline && this.context.params && !pipelineBranchesUnsupported(this.props.pipeline)) {
            const { organization, pipeline } = this.context.params;
            this.pager = this.context.pipelineService.branchPager(organization, pipeline);
        }
    }

    render() {
        const { t, locale, pipeline } = this.props;

        if (!capable(pipeline, MULTIBRANCH_PIPELINE)) {
            return (<NotSupported t={t} />);
        }

        const branches = this.pager.data;

        if (!this.pager.pending && !branches.length) {
            return (<EmptyState repoName={this.context.params.pipeline} />);
        }

        const head = 'pipelinedetail.branches.header';

        const statusHeader = t(`${head}.status`, { defaultValue: 'Status' });
        const healthHeader = t(`${head}.health`, { defaultValue: 'Health' });
        const commitHeader = t(`${head}.commit`, { defaultValue: 'Commit' });
        const branchHeader = t(`${head}.branch`, { defaultValue: 'Branch' });
        const messageHeader = t(`${head}.message`, { defaultValue: 'Message' });
        const completedHeader = t(`${head}.completed`, { defaultValue: 'Completed' });

        const headers = [
            healthHeader,
            statusHeader,
            { label: branchHeader, className: 'branch' },
            { label: commitHeader, className: 'lastcommit' },
            { label: messageHeader, className: 'message' },
            { label: completedHeader, className: 'completed' },
            { label: '', className: 'run' },
        ];

        return (
            <main>
                <article>
                    {branches.$pending && <PageLoading />}

                    <Table className="multibranch-table u-highlight-rows u-table-lr-indents" headers={headers} disableDefaultPadding>
                        {branches.length > 0 && branches.map((branch, index) => <Branches pipeline={pipeline} key={index} data={branch} t={t} locale={locale} />)}
                    </Table>
                    {this.pager.pending &&
                        <button disabled={this.pager.pending || !this.pager.hasMore} className="btn-show-more btn-secondary" onClick={() => this.pager.fetchNextPage()}>
                             {this.pager.pending ? t('common.pager.loading', { defaultValue: 'Loading...' }) : t('common.pager.more', { defaultValue: 'Show more' })}
                        </button>
                    }
                </article>
                {this.props.children}
            </main>
        );
    }
}

MultiBranch.contextTypes = {
    config: object.isRequired,
    params: object.isRequired,
    pipelineService: object.isRequired,
};

MultiBranch.propTypes = {
    children: any,
    t: func,
    locale: string,
    pipeline: object,
};

export default MultiBranch;

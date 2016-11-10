import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import Markdown from 'react-remarkable';
import Branches from './Branches';
import { RunsRecord } from './records';
import {
    actions,
    currentBranches as branchSelector,
    createSelector,
    connect,
} from '../redux';
import PageLoading from './PageLoading';
import { pipelineBranchesUnsupported } from './PipelinePage';

const { object, array, func, string, any } = PropTypes;

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

export class MultiBranch extends Component {
    componentWillMount() {
        if (this.props.pipeline && this.context.params && !pipelineBranchesUnsupported(this.props.pipeline)) {
            this.props.fetchBranches({
                organizationName: this.context.params.organization,
                pipelineName: this.context.params.pipeline,
            });
        }
    }

    componentWillUnmount() {
        this.props.clearBranchData();
    }


    render() {
        const { branches, t, locale, pipeline } = this.props;

        if (!branches || (!branches.$pending && pipelineBranchesUnsupported(pipeline))) {
            return (<NotSupported t={t} />);
        }

        if (branches.$failed) {
            return <div>ERROR: {branches.$failed}</div>;
        }

        if (!branches.$pending && !branches.length) {
            return (<EmptyState t={t} repoName={this.context.params.pipeline} />);
        }

        const head = 'pipelinedetail.branches.header';

        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const health = t(`${head}.health`, { defaultValue: 'health' });
        const commit = t(`${head}.commit`, { defaultValue: 'Commit' });
        const branch = t(`${head}.branch`, { defaultValue: 'Branch' });
        const message = t(`${head}.message`, { defaultValue: 'Message' });
        const completed = t(`${head}.completed`, { defaultValue: 'Completed' });
        const headers = [
            health,
            status,
            { label: branch, className: 'branch' },
            { label: commit, className: 'lastcommit' },
            { label: message, className: 'message' },
            { label: completed, className: 'completed' },
            { label: '', className: 'run' },
        ];

        return (
            <main>
                <article>
                    {branches.$pending && <PageLoading />}
                    <Table className="multibranch-table fixed"
                      headers={headers}
                    >
                        {branches.length > 0 && branches.map((run, index) => {
                            const result = new RunsRecord(run);
                            return (<Branches
                              pipeline={pipeline}
                              key={index}
                              data={result}
                              t={t}
                              locale={locale}
                            />);
                        })
                        }
                    </Table>
                    {branches.$pager &&
                        <button disabled={branches.$pending || !branches.$pager.hasMore} className="btn-show-more btn-secondary" onClick={() => branches.$pager.fetchMore()}>
                             {branches.$pending ? t('common.pager.loading', { defaultValue: 'Loading...' }) : t('common.pager.more', { defaultValue: 'Show more' })}
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
};

MultiBranch.propTypes = {
    branches: array,
    fetchBranches: func,
    clearBranchData: func,
    children: any,
    t: func,
    locale: string,
    pipeline: object,
};

const selectors = createSelector([branchSelector], (branches) => ({ branches }));

export default connect(selectors, actions)(MultiBranch);

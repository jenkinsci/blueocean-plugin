import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import PullRequest from './PullRequest';
import Markdown from 'react-remarkable';
import { RunsRecord } from './records';
import {
    actions,
    pullRequests as pullRequestSelector,
    createSelector,
    connect,
} from '../redux';
import PageLoading from './PageLoading';
import { pipelineBranchesUnsupported } from './PipelinePage';

const { func, object, array, string } = PropTypes;

const EmptyState = ({ repoName, t }) => (
    <main>
        <EmptyStateView iconName="goat">
            <Markdown>
                {t('EmptyState.pr', {
                    0: repoName,
                    defaultValue: '# Push me, pull you\nWhen a Pull Request is opened on the repository _{0}_, Jenkins will test it and report the status of your changes back to the pull request on Github.',
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
                {t('EmptyState.pr.notSupported', {
                    defaultValue: '# Pull Requests are unsupported\nValidated pull request builds only work with the _Multibranch Pipeline_ job type. This is just one of the many reasons to switch to Jenkins Pipeline.\n\n[Learn more](https://jenkins.io/doc/book/pipeline-as-code/)',
                })}
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

export class PullRequests extends Component {
    componentWillMount() {
        if (this.props.pipeline && this.context.params && !pipelineBranchesUnsupported(this.props.pipeline)) {
            this.props.fetchPullRequests({
                organizationName: this.context.params.organization,
                pipelineName: this.context.params.pipeline,
            });
        }
    }

    componentWillUnmount() {
        this.props.clearPRData();
    }

    render() {
        const { pullRequests, t, locale, pipeline } = this.props;

        if (!pullRequests || (!pullRequests.$pending && pipelineBranchesUnsupported(pipeline))) {
            return (<NotSupported t={t} />);
        }

        if (pullRequests.$pending && !pullRequests.length) {
            return <PageLoading />;
        }


        if (pullRequests.$failed) {
            return <div>Error: {pullRequests.$failed}</div>;
        }

        if (!pullRequests.$pending && !pullRequests.length) {
            return (<EmptyState t={t} repoName={this.context.params.pipeline} />);
        }

        const head = 'pipelinedetail.pullrequests.header';
        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const build = t(`${head}.build`, { defaultValue: 'Build' });
        const author = t(`${head}.author`, { defaultValue: 'Author' });
        const summary = t(`${head}.summary`, { defaultValue: 'Summary' });
        const completed = t(`${head}.completed`, { defaultValue: 'Completed' });

        const headers = [
            status,
            { label: build, className: 'build' },
            { label: summary, className: 'summary' },
            author,
            { label: completed, className: 'completed' },
            { label: '', className: 'run' },
        ];

        return (
            <main>
                <article>
                    {pullRequests.$pending && <PageLoading />}
                    <Table className="pr-table fixed" headers={headers}>
                        {pullRequests.map((run, index) => {
                            const result = new RunsRecord(run);
                            return (<PullRequest
                              t={t}
                              locale={locale}
                              pipeline={pipeline}
                              key={index}
                              pr={result}
                            />);
                        })}
                    </Table>
                    {pullRequests.$pager &&
                        <button disabled={pullRequests.$pending || !pullRequests.$pager.hasMore} className="btn-show-more btn-secondary" onClick={() => pullRequests.$pager.fetchMore()}>
                            {pullRequests.$pending ? 'Loading...' : 'Show More'}
                        </button>
                    }
                </article>
            </main>
        );
    }
}

PullRequests.contextTypes = {
    config: object.isRequired,
    params: object.isRequired,
};

PullRequests.propTypes = {
    pullRequests: array,
    clearPRData: func,
    locale: string,
    fetchPullRequests: func,
    t: func,
    pipeline: object,
};

const selectors = createSelector([pullRequestSelector], (pullRequests) => ({ pullRequests }));

export default connect(selectors, actions)(PullRequests);

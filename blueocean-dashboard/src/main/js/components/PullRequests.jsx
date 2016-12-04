import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import PullRequest from './PullRequest';
import Markdown from 'react-remarkable';
import { RunsRecord } from './records';
import PageLoading from './PageLoading';
import { capable } from '@jenkins-cd/blueocean-core-js';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { observer } from 'mobx-react';
const { object, string, func } = PropTypes;

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

@observer
export class PullRequests extends Component {
    componentWillMount() {
        if (this.props.pipeline && this.props.params && capable(this.props.pipeline, MULTIBRANCH_PIPELINE)) {
            this.pager = this.context.pipelineService.prPager(this.props.params.organization, this.props.params.pipeline);
        }
    }


    render() {
        const { t, locale, pipeline } = this.props;

        if (!capable(pipeline, MULTIBRANCH_PIPELINE)) {
            return (<NotSupported t={t} />);
        }
        const pullRequests = this.pager.data;

        if (this.pager.pending) {
            return <PageLoading />;
        }

        if (!this.pager.pending && !this.pager.data.length) {
            return (<EmptyState t={t} repoName={this.context.params.pipeline} />);
        }

        const head = 'pipelinedetail.pullrequests.header';
        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const build = t(`${head}.build`, { defaultValue: 'PR' });
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
                    {this.pager.pending && <PageLoading />}
                    <Table className="pr-table u-highlight-rows u-table-lr-indents" headers={headers} disableDefaultPadding>
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
                    {this.pager &&
                        <button disabled={this.pager.pending || !this.pager.hasMore} className="btn-show-more btn-secondary" onClick={() => this.pager.fetchNextPage()}>
                            {this.pager.pending ? 'Loading...' : 'Show More'}
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
    pipelineService: object.isRequired,
};

PullRequests.propTypes = {
    locale: string,
    t: func,
    pipeline: object,
    params: object,
};

export default PullRequests;

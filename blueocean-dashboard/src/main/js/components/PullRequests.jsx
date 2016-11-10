import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import PullRequest from './PullRequest';
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

const EmptyState = ({ repoName }) => (
    <main>
        <EmptyStateView iconName="goat">
            <h1>Push me, pull you</h1>

            <p>
                When a Pull Request is opened on the repository <em>{repoName}</em>,
                Jenkins will test it and report the status of
                your changes back to the pull request on Github.
            </p>

            <button>Enable</button>
        </EmptyStateView>
    </main>
);

const NotSupported = () => (
    <main>
        <EmptyStateView>
            <h1>Pull Requests are unsupported</h1>
            <p>
            Validated pull request builds only work with the <i>Multibranch Pipeline</i> job type.
            This is just one of the many reasons to switch to Jenkins Pipeline.
            </p>
            <a href="https://jenkins.io/doc/book/pipeline-as-code/" target="_blank">Learn more</a>
        </EmptyStateView>
    </main>
);

EmptyState.propTypes = {
    repoName: string,
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
        const { pullRequests, pipeline } = this.props;

        if (!pullRequests || (!pullRequests.$pending && pipelineBranchesUnsupported(pipeline))) {
            return (<NotSupported />);
        }

        if (pullRequests.$pending && !pullRequests.length) {
            return <PageLoading />;
        }


        if (pullRequests.$failed) {
            return <div>Error: {pullRequests.$failed}</div>;
        }

        if (!pullRequests.$pending && !pullRequests.length) {
            return (<EmptyState repoName={this.context.params.pipeline} />);
        }

        const headers = [
            'Status',
            { label: 'Latest Build', className: 'build' },
            { label: 'Summary', className: 'summary' },
            'Author',
            { label: 'Completed', className: 'completed' },
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
    fetchPullRequests: func,
    pipeline: object,
};

const selectors = createSelector([pullRequestSelector], (pullRequests) => ({ pullRequests }));

export default connect(selectors, actions)(PullRequests);

import React, { Component, PropTypes } from 'react';
import { EmptyStateView } from '@jenkins-cd/design-language';
import Table from './Table';
import PullRequest from './PullRequest';
import { scrollToHash } from './ScrollToHash';
import { RunsRecord } from './records';
import {
    actions,
    currentBranches as branchSelector,
    createSelector,
    connect,
} from '../redux';

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

EmptyState.propTypes = {
    repoName: string,
};

export class PullRequests extends Component {
    componentWillMount() {
        if (this.context.config && this.context.params) {
            const {
                params: {
                    pipeline,
                },
                config = {},
            } = this.context;
            config.pipeline = pipeline;
            this.props.fetchBranchesIfNeeded(config);
        }
    }


    render() {
        const { branches } = this.props;
        // early out
        if (!branches) {
            return null;
        }

        const pullRequests = branches.filter((run) => run.pullRequest);

        if (!pullRequests.length) {
            return (<EmptyState repoName={this.context.params.pipeline} />);
        }

        const headers = [
            'Status',
            { label: 'Latest Build', className: 'build' },
            { label: 'Summary', className: 'summary' },
            'Author',
            { label: 'Completed', className: 'completed' },
        ];

        return (
            <main>
                <article>
                    <Table className="pr-table" headers={headers}>
                        { pullRequests.map((run, index) => {
                            const result = new RunsRecord(run);
                            return (<PullRequest
                              key={index}
                              pr={result}
                            />);
                        })}
                    </Table>
                </article>
            </main>
        );
    }
}

PullRequests.contextTypes = {
    params: object.isRequired,
    config: object.isRequired,
};

PullRequests.propTypes = {
    branches: array,
    fetchBranchesIfNeeded: func,
};

const selectors = createSelector([branchSelector], (branches) => ({ branches }));

export default connect(selectors, actions)(scrollToHash(PullRequests));

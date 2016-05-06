import React, { Component, PropTypes } from 'react';
import Table from './Table';
import PullRequest from './PullRequest';
import { RunsRecord } from './records';
import {
    actions,
    currentBranches as branchSelector,
    createSelector,
    connect,
} from '../redux';

const { func, object, array } = PropTypes;

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
                        { branches.filter((run) => run.pullRequest).map((run, index) => {
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

export default connect(selectors, actions)(PullRequests);

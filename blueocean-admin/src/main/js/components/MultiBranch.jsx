import React, { Component, PropTypes } from 'react';
import Table from './Table';
import { EmptyStateView } from '@jenkins-cd/design-language';
import Branches from './Branches';
import { RunsRecord } from './records';
import { scrollToHash } from './ScrollToHash';
import {
    actions,
    currentBranches as branchSelector,
    createSelector,
    connect,
} from '../redux';

const { object, array, func, string } = PropTypes;

const EmptyState = ({ repoName }) => (
    <main>
        <EmptyStateView iconName="branch">
            <h1>Branch out</h1>

            <p>
                Create a branch in the repository <em>{repoName}</em> and
                Jenkins will start testing your changes.
            </p>

            <p>
                Give it a try and become a hero to your team.
            </p>

            <button>Enable</button>
        </EmptyStateView>
    </main>
);

EmptyState.propTypes = {
    repoName: string,
};

export class MultiBranch extends Component {
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

        if (!branches.length) {
            return (<EmptyState repoName={this.context.params.pipeline} />);
        }

        const headers = [
            'Health',
            'Status',
            { label: 'Branch', className: 'branch' },
            { label: 'Last commit', className: 'lastcommit' },
            { label: 'Latest message', className: 'message' },
            { label: 'Completed', className: 'completed' },
        ];

        return (
            <main>
                <article>
                    <Table className="multibranch-table"
                      headers={headers}
                    >
                        {branches.map((run, index) => {
                            const result = new RunsRecord(run);
                            return (<Branches
                              key={index}
                              data={result}
                            />);
                        })
                        }
                    </Table>
                </article>
            </main>
        );
    }
}

MultiBranch.contextTypes = {
    params: object.isRequired,
    config: object.isRequired,
};

MultiBranch.propTypes = {
    branches: array,
    fetchBranchesIfNeeded: func,
};

const selectors = createSelector([branchSelector], (branches) => ({ branches }));

export default connect(selectors, actions)(scrollToHash(MultiBranch));

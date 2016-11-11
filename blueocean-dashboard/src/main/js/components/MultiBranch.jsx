import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import Branches from './Branches';

import PageLoading from './PageLoading';
import { pipelineBranchesUnsupported } from './PipelinePage';
import { branchService, capable } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';

const { object, array, func, string, any } = PropTypes;

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

const NotSupported = () => (
    <main>
        <EmptyStateView>
            <h1>Branches are unsupported</h1>
            <p>
            Branch builds only work with the <i>Multibranch Pipeline</i> job type.
            This is just one of the many reasons to switch to Jenkins Pipeline.
            </p>
            <a href="https://jenkins.io/doc/book/pipeline-as-code/" target="_blank">Learn more</a>
        </EmptyStateView>
    </main>
);

EmptyState.propTypes = {
    repoName: string,
};

@observer
export class MultiBranch extends Component {
    componentWillMount() {
        if (this.props.pipeline && this.context.params && !pipelineBranchesUnsupported(this.props.pipeline)) {
            const { organization, pipeline } = this.context.params;
            this.pager = branchService.branchPager(organization, pipeline);
        }
    }

    render() {
        const { pipeline } = this.props;
        const branches = this.pager.data;
        if (!capable(pipeline, MULTIBRANCH_PIPELINE)) {
            return (<NotSupported />);
        }

        if (!this.pager.pending && !branches.length) {
            return (<EmptyState repoName={this.context.params.pipeline} />);
        }

        const headers = [
            'Health',
            'Status',
            { label: 'Branch', className: 'branch' },
            { label: 'Last commit', className: 'lastcommit' },
            { label: 'Latest message', className: 'message' },
            { label: 'Completed', className: 'completed' },
            { label: '', className: 'run' },
        ];

        return (
            <main>
                <article>
                    {branches.$pending && <PageLoading />}
                    <Table className="multibranch-table fixed"
                      headers={headers}
                    >
                        {branches.length > 0 && branches.map((branch, index) => {
                            return (<Branches
                              pipeline={pipeline}
                              key={index}
                              data={branch}
                            />);
                        })
                        }
                    </Table>
                    {this.pager.pending &&
                        <button disabled={this.pager.pending || !this.pager.hasMore} className="btn-show-more btn-secondary" onClick={() => this.pager.fetchNextPage()}>
                             {this.pager.pending ? 'Loading...' : 'Show More'}
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
    children: any,
    pipeline: object,
};

export default MultiBranch;

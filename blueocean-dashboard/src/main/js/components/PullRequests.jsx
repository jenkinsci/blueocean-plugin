import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import PullRequest from './PullRequest';
import { RunsRecord } from './records';
import PageLoading from './PageLoading';
import { pipelineService, capable } from '@jenkins-cd/blueocean-core-js';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { observer } from 'mobx-react';
const { object, string } = PropTypes;

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

@observer
export class PullRequests extends Component {
    componentWillMount() {
        if (this.props.pipeline && this.props.params && capable(this.props.pipeline, MULTIBRANCH_PIPELINE)) {
            this.pager = this.context.pipelineService.prPager(this.props.params.organization, this.props.params.pipeline);
        }
    }


    render() {
        if (!capable(this.props.pipeline, MULTIBRANCH_PIPELINE)) {
            return (<NotSupported />);
        }
        const pullRequests = this.pager.data;
        const { pipeline } = this.props;


        if (this.pager.pending) {
            return <PageLoading />;
        }

        if (!this.pager.pending && !this.pager.data.length) {
            return (<EmptyState repoName={this.context.params.pipeline} />);
        }

        const headers = [
            'Status',
            { label: 'PR', className: 'build' },
            { label: 'Subject', className: 'summary' },
            'Author',
            { label: 'Completed', className: 'completed' },
            { label: '', className: 'run' },
        ];

        return (
            <main>
                <article>
                    {this.pager.pending && <PageLoading />}
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
    pipeline: object,
    params: object,
};

export default PullRequests;

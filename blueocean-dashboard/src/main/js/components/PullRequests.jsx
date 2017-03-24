import React, { Component, PropTypes } from 'react';
import { EmptyStateView, Table } from '@jenkins-cd/design-language';
import { capable, ShowMoreButton } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';

import PullRequest from './PullRequest';
import { RunsRecord } from './records';
import { MULTIBRANCH_PIPELINE } from '../Capabilities';
import { NoPullRequestsPlaceholder } from './placeholder/NoPullRequestsPlaceholder';
import { UnsupportedPlaceholder } from './placeholder/UnsupportedPlaceholder';


const { object, string, func } = PropTypes;


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
            return (<UnsupportedPlaceholder t={t} />);
        }
        const pullRequests = this.pager.data;

        if (this.pager.pending) {
            return null;
        }

        if (!this.pager.pending && !this.pager.data.length) {
            return <NoPullRequestsPlaceholder t={t} />;
        }

        const head = 'pipelinedetail.pullrequests.header';
        const status = t(`${head}.status`, { defaultValue: 'Status' });
        const runHeader = t(`${head}.run`, { defaultValue: 'PR' });
        const author = t(`${head}.author`, { defaultValue: 'Author' });
        const summary = t(`${head}.summary`, { defaultValue: 'Summary' });
        const completed = t(`${head}.completed`, { defaultValue: 'Completed' });

        const headers = [
            status,
            { label: runHeader, className: 'run' },
            { label: summary, className: 'summary' },
            author,
            { label: completed, className: 'completed' },
            { label: '', className: 'run' },
        ];

        return (
            <main>
                <article>
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
                    <ShowMoreButton pager={this.pager} />
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

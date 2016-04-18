import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import ajaxHoc from '../AjaxHoc';
import Table from './Table';
import PullRequest from './PullRequest';
import { RunsRecord } from './records';
import { urlPrefix } from '../config';

const { object, array } = PropTypes;

export class PullRequests extends Component {
    render() {
        const { pipeline, data } = this.props;

        if (!data || !pipeline) {
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
                        { data.filter((run) => run.pullRequest).map((run, index) => {
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

PullRequests.propTypes = {
    pipeline: object,
    data: array,
};

// Decorated for ajax as well as getting pipeline from context
export default ajaxHoc(PullRequests, (props, config) => {
    if (!props.pipeline) return null;
    return `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${props.pipeline.name}/branches`;
});

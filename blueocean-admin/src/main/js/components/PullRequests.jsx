import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import ajaxHoc from '../AjaxHoc';
import Table from './Table';
import PullRequest from './PullRequest';
import { RunsRecord } from './records';
import { urlPrefix } from '../config';
import pipelinePropProvider from './pipelinePropProvider';

import { Page, PageHeader, Title, WeatherIcon } from '@jenkins-cd/design-language';

export class PullRequests extends Component {
    render() {
        const { pipeline, data } = this.props;

        if (!data || !pipeline) {
            return null;
        }
        const
            {
                name,
                weatherScore,
                } = pipeline;

        const headers = ['Status', 'Latest Build', 'Summary', 'Author', 'Completed'];

        return (<Page>

            <PageHeader>
                <Title><WeatherIcon score={weatherScore} /> <h1>CloudBees / {name}</h1></Title>
            </PageHeader>

            <main>
                <article>
                    <Table headers={headers}>
                        { data.filter((run) => run.pullRequest).map((run, index) => {
                            const result = new RunsRecord(run);
                            return (<PullRequest
                              key={index}
                              pr={result}
                            />);
                        })}

                        <tr>
                            <td colSpan={headers.length}>
                                <Link className="btn" to={urlPrefix}>Dashboard</Link>
                            </td>
                        </tr>
                    </Table>
                </article>
            </main>
        </Page>);
    }
}

PullRequests.propTypes = {
    pipeline: PropTypes.object,
    data: PropTypes.array,
};

// Decorated for ajax as well as getting pipeline from context
export default pipelinePropProvider(ajaxHoc(PullRequests, (props, config) => {
    if (!props.pipeline) return null;
    return `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${props.pipeline.name}/branches`;
}));

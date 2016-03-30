import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import Table from './Table';
import AjaxHoc from '../AjaxHoc';
import Branches from './Branches';
import { components } from '@jenkins-cd/design-language';
const { WeatherIcon, Page, PageHeader, Title } = components;
import { RunsRecord } from './records';
import { urlPrefix } from '../config';
import pipelinePropProvider from './pipelinePropProvider';

export class MultiBranch extends Component {
    render() {
        const { pipeline, data } = this.props;
        // early out
        if (!data || !pipeline) {
            return null;
        }

        const {
            name,
            weatherScore,
            } = pipeline;

        const headers =
            ['Health', 'Status', 'Branch', 'Last commit', 'Latest message', 'Completed'];

        return (
            <Page>
                <PageHeader>
                    <Title><WeatherIcon score={weatherScore} /> <h1>CloudBees / {name}</h1></Title>
                </PageHeader>
                <main>
                    <article>
                        <Table className="multiBranch"
                          headers={headers}
                        >
                            {data.map((run, index) => {
                                    const result = new RunsRecord(run.toJS());
                                    return <Branches key={index} data={result} />;
                                })
                            }
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

MultiBranch.propTypes = {
    pipeline: PropTypes.object,
    data: PropTypes.object,
};

// Decorated for ajax as well as getting pipeline from context
export default pipelinePropProvider(AjaxHoc(MultiBranch, (props, config) => {
    if (!props.pipeline) return null;
    return `${config.getAppURLBase()}/rest/organizations/jenkins` +
        `/pipelines/${props.pipeline.name}/branches`;
}));

import React, { Component, PropTypes } from 'react';
import Table from './Table';
import AjaxHoc from '../AjaxHoc';
import Branches from './Branches';
import { components } from '@jenkins-cd/design-language';
import { RunsRecord } from './records';
const { WeatherIcon, Page, PageHeader, Title } = components;

export class MultiBranch extends Component {
    render() {
        const { pipeline, data, back } = this.props;
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
                    <Title><WeatherIcon score={weatherScore} /> CloudBees / {name}</Title>
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
                                    <button className="btn" onClick={back}>Dashboard</button>
                                </td>
                            </tr>
                        </Table>
                    </article>
                </main>
            </Page>);
    }
}

MultiBranch.propTypes = {
    pipeline: PropTypes.object.isRequired,
    back: PropTypes.func.isRequired,
    data: PropTypes.object,
};

const baseUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines/';

export default AjaxHoc(MultiBranch, props => ({
    url: `${baseUrl}${props.pipeline.name}/branches`,
}));

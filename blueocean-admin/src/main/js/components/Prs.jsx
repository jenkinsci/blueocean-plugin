import React, { Component, PropTypes } from 'react';
import AjaxHoc from '../AjaxHoc';
import Table from './Table';
import Pr from './Pr';

import { runsRecords } from './records';

import { components } from '@jenkins-cd/design-language';
const { Page, PageHeader, Title, WeatherIcon } = components;

export class Prs extends Component {
    render() {
        const { pipeline, data, back } = this.props;

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
                <Title><WeatherIcon score={weatherScore} /> CloudBees / {name}</Title>
            </PageHeader>

            <main>
                <article>
                    <Table headers={headers}>
                        { data.filter((run) => run.get('pullRequest')).map((run, index) => {
                            const result = new runsRecords(run.toJS());
                            return (<Pr
                                key={index}
                                pr={result}
                            />);
                        })}

                        <tr>
                            <td colSpan={headers.length}>
                                <button onClick={back}>Dashboard</button>
                            </td>
                        </tr>
                    </Table>
                </article>
            </main>
          </Page>);
    }
}

Prs.propTypes = {
    pipeline: PropTypes.object.isRequired,
    back: PropTypes.func.isRequired,
    data: PropTypes.object,
};

const baseUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines/';

export default AjaxHoc(Prs, props => ({
    url: `${baseUrl}${props.pipeline.name}/branches`,
}));

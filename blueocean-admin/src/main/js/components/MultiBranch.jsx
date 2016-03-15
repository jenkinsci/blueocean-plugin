import React, { Component, PropTypes } from 'react';
import Table from './Table';
import Branches from './Branches';
import { components } from '@jenkins-cd/design-language';
const { WeatherIcon, Page, PageHeader, Title } = components;

export default class MultiBranch extends Component {
    render() {
        const { pipeline, back } = this.props;
        // early out
        if (!pipeline) {
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
                            {pipeline.branchNames.map((branch, index) =>
                                <Branches key={index} branch={branch} pipeline={pipeline} />)}
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
};

import React, { Component, PropTypes } from 'react';
import Pipeline from './Pipeline';
import { pipelineRecord } from './records';
import Table from './Table';

import { components } from '@jenkins-cd/design-language';
const { Page, PageHeader, Title } = components;

export default class Pipelines extends Component {

    render() {
        const { pipelines, hack, link } = this.props;
        // Early out
        if (!pipelines) {
            return null;
        }

        const multiBranch = pipelines.filter(pipeline =>
            !!new pipelineRecord(pipeline).branchNames);
        const noMultiBranch = pipelines.filter(pipeline =>
            !new pipelineRecord(pipeline).branchNames);

        return (
            <Page>
                <PageHeader>
                    <Title>CloudBees {link}</Title>
                </PageHeader>
                <main>
                    <article>
                        <Table
                          className="multiBranch"
                          headers={['Name', 'Status', 'Branches', 'Pull Requests', '']}
                        >
                            { multiBranch.map(
                                (pipeline, index) => <Pipeline
                                  key={index}
                                  hack={hack}
                                  pipeline={new pipelineRecord(pipeline)}
                                />
                            )}
                            { noMultiBranch.map(
                                (pipeline, index) => <Pipeline
                                  key={index}
                                  hack={hack}
                                  simple
                                  pipeline={new pipelineRecord(pipeline)}
                                />)}
                        </Table>
                    </article>
                </main>
            </Page>);
    }
}

Pipelines.propTypes = {
    pipelines: PropTypes.object.isRequired,
    link: PropTypes.object.isRequired,
    hack: PropTypes.object.isRequired,
};

import React, { Component, PropTypes } from 'react';
import PipelineRowItem from './PipelineRowItem';
import { PipelineRecord } from './records';
import Table from './Table';

import { Page, PageHeader, Title } from '@jenkins-cd/design-language';
import { ExtensionPoint } from '@jenkins-cd/js-extensions';

const { array } = PropTypes;

export default class Pipelines extends Component {

    render() {
        const { pipelines, config } = this.context;

        // Early out
        if (!pipelines) {
            return <div>No pipelines found.</div>;
        }

        const pipelineRecords = pipelines
            .map(data => new PipelineRecord(data))
            .sort(pipeline => !!pipeline.branchNames);

        const headers = [
            { label: 'Name', className: 'name' },
            'Health',
            'Branches',
            'Pull Requests',
            { label: '', className: 'favorite' },
        ];

        const baseUrl = config.getRootURL();
        const newJobUrl = `${baseUrl}view/All/newJob`;

        return (
            <Page>
                <PageHeader>
                    <Title>
                        <h1>Dashboard</h1>
                        <a target="_blank" className="btn-inverse" href={newJobUrl}>
                            New Pipeline
                        </a>
                    </Title>
                </PageHeader>
                <main>
                    <article>
                        <ExtensionPoint name="jenkins.pipeline.list.top" />
                        <Table
                          className="pipelines-table"
                          headers={headers}
                        >
                            { pipelineRecords
                                .map(pipeline => <PipelineRowItem
                                  key={pipeline.name} pipeline={pipeline}
                                />)
                            }
                        </Table>
                    </article>
                </main>
            </Page>);
    }
}

Pipelines.contextTypes = {
    pipelines: array,
    config: PropTypes.object,
};

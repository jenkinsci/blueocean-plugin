import React, { Component, PropTypes } from 'react';
import Pipeline from './Pipeline';
import { PipelineRecord } from './records';
import Table from './Table';

import { components } from '@jenkins-cd/design-language';
const { Page, PageHeader, Title } = components;

export default class Pipelines extends Component {

    render() {
        const { pipelines } = this.context;

        // Early out
        if (!pipelines) {
            return <div>No pipelines found.</div>;
        }

        const pipelineRecords = pipelines
            .map(data => new PipelineRecord(data))
            .sort(pipeline => !!pipeline.branchNames);

        return (
            <Page>
                <PageHeader>
                    <Title>
                        <h1>CloudBees</h1>
                        <a target="_blank" className="btn-primary" href="/jenkins/view/All/newJob">New Pipeline</a>
                    </Title>
                </PageHeader>
                <main>
                    <article>
                        <Table className="multiBranch" headers={['Name', 'Status', 'Branches', 'Pull Requests', '']}>
                            { pipelineRecords
                                .map(pipeline => <Pipeline key={pipeline.name} pipeline={pipeline}/>)
                                .toArray() }
                        </Table>
                    </article>
                </main>
            </Page>);
    }
}

Pipelines.contextTypes = {
    pipelines: PropTypes.object
};
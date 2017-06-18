/* eslint-disable */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import PipelineRowItem from '../PipelineRowItem.jsx';
import { PipelineRecord } from '../records.jsx';
import { JTable, TableHeaderRow } from '@jenkins-cd/design-language';

/*
First example of using storybook
 */
storiesOf('pipelines', module)
    .add('with a pipeline', pipelines)
    .add('no pipeline should return null', () => <PipelineRowItem />)
;

function pipelines() {

    const columns = [
        JTable.column(640, 'Name', true),
        JTable.column(70, 'Status'),
        JTable.column(70, 'Branches'),
        JTable.column(70, 'Pull Requests'),
        JTable.column(24, ''),
    ];

    let p1 = new PipelineRecord({
        displayName: 'moreBeersSuccess',
        name: 'morebeersSuccess',
        organization: 'jenkins',
        weatherScore: 0,
        branchNames: ['master'],
        numberOfFailingBranches: 0,
        numberOfFailingPullRequests: 0,
        numberOfSuccessfulBranches: 3,
        numberOfSuccessfulPullRequests: 3,
        totalNumberOfBranches: 3,
        totalNumberOfPullRequests: 3,
    });

    let p2 = new PipelineRecord({
        displayName: 'moreBeers',
        name: 'morebeers',
        organization: 'jenkins',
        weatherScore: 0,
        branchNames: ['master'],
        numberOfFailingBranches: 1,
        numberOfFailingPullRequests: 0,
        numberOfSuccessfulBranches: 0,
        numberOfSuccessfulPullRequests: 0,
        totalNumberOfBranches: 1,
        totalNumberOfPullRequests: 0,
    });

    return (
        <JTable columns={columns} style={{ margin: '1em' }}>
            <TableHeaderRow />
            <PipelineRowItem t={(key) => key} pipeline={p1} />
            <PipelineRowItem t={(key) => key} pipeline={p2} />
        </JTable>
    );
}

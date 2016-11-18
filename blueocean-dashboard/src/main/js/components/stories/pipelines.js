import React from 'react';
import { storiesOf } from '@kadira/storybook';
import PipelineRowItem from '../PipelineRowItem.jsx';
import { PipelineRecord } from '../records.jsx';
import { Table } from '@jenkins-cd/design-language';

/*
First example of using storybook
 */
storiesOf('pipelines', module)
    .add('with a pipeline', () => (
        <Table
          className="multiBranch"
          headers={['Name', 'Status', 'Branches', 'Pull Requests', '']}
        >
            <PipelineRowItem
              t={(key) => key}
              pipeline={new PipelineRecord({
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
              })}
            />
            <PipelineRowItem
              t={(key) => key}
              pipeline={new PipelineRecord({
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
              })}
            />
        </Table>
    ))
.add('no pipeline should return null', () => (
     <PipelineRowItem />
))
;

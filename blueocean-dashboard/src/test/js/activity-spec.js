import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { Activity } from '../../main/js/components/Activity.jsx';
import { CapabilityRecord } from '../../main/js/components/Capability.jsx';

import { pipelines } from './data/pipelines/pipelinesSingle';
import { mockExtensionsForI18n } from './mock-extensions-i18n';


const
  data = [
      {
      "changeSet": [],
      "durationInMillis": 64617,
      "enQueueTime": "2016-03-04T13:59:53.272+0100",
      "endTime": "2016-03-04T14:00:57.991+0100",
      "id": "3",
      "organization": "jenkins",
      "pipeline": "master",
      "result": "FAILURE",
      "runSummary": "broken since build #2",
      "startTime": "2016-03-04T13:59:53.374+0100",
      "state": "FINISHED",
      "type": "WorkflowRun",
      "commitId": "444196ac6afbd3e417f1d46ebfb3c4f0aac0c165",
      "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
    },
    {
      "changeSet": [],
      "durationInMillis": 664124,
      "enQueueTime": "2016-03-04T13:59:54.132+0100",
      "endTime": "2016-03-04T14:10:58.262+0100",
      "id": "1",
      "organization": "jenkins",
      "pipeline": "patch-1",
      "result": "SUCCESS",
      "runSummary": "stable",
      "startTime": "2016-03-04T13:59:54.138+0100",
      "state": "FINISHED",
      "type": "WorkflowRun",
      "commitId": "f8eeb35c03e52c17c27824fa77fa6b0f03a93625",
      "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
    },
    {
      "changeSet": [
        {
          "author": {
            "email": "tscherler@cloudbees.com",
            "fullName": "tscherler",
            "id": "tscherler"
          },
          "affectedPaths": [
            "Jenkinsfile"
          ],
          "commitId": "746cf27525b7b1d615de408ca86786613ccf7548",
          "comment": "Update Jenkinsfile\n",
          "date": "2016-03-04 14:14:48 +0100",
          "id": "746cf27525b7b1d615de408ca86786613ccf7548",
          "msg": "Update Jenkinsfile",
          "paths": [
            {
              "editType": "edit",
              "file": "Jenkinsfile"
            }
          ],
          "timestamp": "2016-03-04T14:14:48.000+0100"
        }
      ],
      "durationInMillis": 69102,
      "enQueueTime": "2016-03-04T14:18:55.455+0100",
      "endTime": "2016-03-04T14:20:04.592+0100",
      "id": "4",
      "organization": "jenkins",
      "pipeline": "master",
      "result": "SUCCESS",
      "runSummary": "back to normal",
      "startTime": "2016-03-04T14:18:55.490+0100",
      "state": "FINISHED",
      "type": "WorkflowRun",
      "commitId": "746cf27525b7b1d615de408ca86786613ccf7548",
      "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
    },
    {
      "changeSet": [
        {
          "author": {
            "email": "scherler@gmail.com",
            "fullName": "scherler",
            "id": "scherler"
          },
          "affectedPaths": [
            "Jenkinsfile"
          ],
          "commitId": "7d2ad24151dcd4be26d13b6116794691e8bb004f",
          "comment": "Update Jenkinsfile\n",
          "date": "2016-03-04 14:24:01 +0100",
          "id": "7d2ad24151dcd4be26d13b6116794691e8bb004f",
          "msg": "Update Jenkinsfile",
          "paths": [
            {
              "editType": "edit",
              "file": "Jenkinsfile"
            }
          ],
          "timestamp": "2016-03-04T14:24:01.000+0100"
        }
      ],
      "durationInMillis": 48626,
      "enQueueTime": "2016-03-04T14:28:09.321+0100",
      "endTime": "2016-03-04T14:28:57.948+0100",
      "id": "5",
      "organization": "jenkins",
      "pipeline": "master",
      "result": "FAILURE",
      "runSummary": "broken since this build",
      "startTime": "2016-03-04T14:28:09.322+0100",
      "state": "FINISHED",
      "type": "WorkflowRun",
      "commitId": "a2f0801fec8bad98663f0df5e9110261820e8c4e",
      "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
    },
    {
      "changeSet": [],
      "durationInMillis": 111377,
      "enQueueTime": "2016-03-09T16:01:23.707+0100",
      "endTime": "2016-03-09T16:03:15.144+0100",
      "id": "6",
      "organization": "jenkins",
      "pipeline": "master",
      "result": "FAILURE",
      "runSummary": "broken since build #5",
      "startTime": "2016-03-09T16:01:23.767+0100",
      "state": "FINISHED",
      "type": "WorkflowRun",
      "commitId": "a2f0801fec8bad98663f0df5e9110261820e8c4e",
      "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
    }
  ];

const context = {
    params: {},
    config: {
        getServerBrowserTimeSkewMillis: () => 0,
    },
    activityService: {
        activityPager() {
            return { data: data };
        },
    },
};

const contextNoData = {
    params: {},
    config: {
        getServerBrowserTimeSkewMillis: () => 0,
    },
    activityService: {
        activityPager() {
            return {
                data: [],
                pending: true,
            };
        },
    },
};
const pipeline = pipelines[0];

const capabilities = {
    'some.class': new CapabilityRecord({}),
};
data.$success = true; // fetch flag

const t = () => {};

describe('Activity', () => {
    beforeAll(() => mockExtensionsForI18n());

    it('render the Activity with data', () => {
        const wrapper = shallow(<Activity t={t} runs={data} pipeline={pipeline} capabilities={capabilities} />, { context });

        // does data renders?
        assert.isNotNull(wrapper);
        assert.equal(wrapper.find('NewComponent').length, data.length);
    });

    it('does not render without data', () => {
        const wrapper = shallow(<Activity pipeline={pipeline} t={ () => {} } capabilities={capabilities} />, { context: contextNoData });
        assert.equal(wrapper.find('NewComponent').length, 0);
    });
});

describe('Pipeline -> Activity List', () => {
    beforeAll(() => mockExtensionsForI18n());

    it('should contain cause', () => {
        const wrapper = shallow(<Activity t={t} runs={data} pipeline={pipeline} capabilities={capabilities} />, { context });
        assert.isNotNull(wrapper);
        const runs = wrapper.find('NewComponent');
        assert.isNotNull(runs);
        const run1 = runs.at(0).html(); // has cause
        const run3 = runs.at(2).html(); // has changeset

        assert(run1.indexOf('Branch indexing') >= 0, 'should have cause message');
        assert(run3.indexOf('Update Jenkinsfile') >= 0, 'should have changset message');
    });

    it('should not duplicate changeset messages', () => {
        const wrapper = shallow(<Activity t={t} runs={data} pipeline={pipeline} capabilities={capabilities} />, { context });
        assert.isNotNull(wrapper);
        const runs = wrapper.find('NewComponent');
        assert.isNotNull(runs);

        const run4 = runs.at(3);
        const run5 = runs.at(4);
        assert.notEqual(
            run4.props().changeset,
            run5.props().changeset
        );
    });
});

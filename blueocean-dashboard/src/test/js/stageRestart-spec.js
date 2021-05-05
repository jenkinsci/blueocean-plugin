import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import StageRestartLink from '../../main/js/components/StageRestartLink';
import { pipelines } from './data/pipelines/pipelinesSingle';

const
  run = {
        "restartable": true,
        "changeSet": [],
        "durationInMillis": 64617,
        "enQueueTime": "2016-03-04T13:59:53.272+0100",
        "endTime": "2016-03-04T14:00:57.991+0100",
        "id": "1",
        "organization": "jenkins",
        "pipeline": "master",
        "result": "FAILURE",
        "runSummary": "broken since build #2",
        "startTime": "2016-03-04T13:59:53.374+0100",
        "state": "FINISHED",
        "type": "WorkflowRun",
        "commitId": "444196ac6afbd3e417f1d46ebfb3c4f0aac0c165",
        "causes": [{"_class": "jenkins.branch.BranchIndexingCause", "shortDescription": "Branch indexing"}],
  };
const pipeline = pipelines[0];
const t = (varName, title) => {return 'Restart ' + title[0]};

const switchRunDetails = () => {};

describe('Stage Restart', () => {
    it('render stage restart link', () => {
        const wrapper = shallow(<StageRestartLink t={t} title={'Stage Title'} run={run} pipeline={pipeline} onNavigation={switchRunDetails} />);
        
        // does data render?
        assert.isNotNull(wrapper);

        //find the restartable stage link
        assert.equal(wrapper.find('a.restart-stage').length, 1);

        //check that the link text gets rendered in the expected markup
        assert.equal(wrapper.find('a.restart-stage span').text(), 'Restart Stage Title');
    });

    it("don't render stage restart link when user doesn't have permission", () => {
        const { permissions } = pipeline;
        permissions.start = false;

        const wrapper = shallow(<StageRestartLink t={t} title={'Stage Title'} run={run} pipeline={pipeline} onNavigation={switchRunDetails} />);

        //make sure that the link is not rendered
        assert.equal(wrapper.find('a.restart-stage').length, 0);
    });
});

import { assert} from 'chai';
import * as pushEventUtil from '../../main/js/util/push-event-util';

describe("event enrichment", () => {

  it("freestyle job queued event", () => {
      const inputEvent = {
          jenkins_object_url: 'job/Myfree/',
          job_name: 'Myfree'
      };

      const outputEvent = pushEventUtil.enrichJobEvent(inputEvent);

      assert.equal(outputEvent.jenkins_object_url, 'job/Myfree/');
      assert.equal(outputEvent.job_name, 'Myfree');

      assert.equal(outputEvent.blueocean_job_name, 'Myfree');
      assert.equal(outputEvent.blueocean_branch_name, undefined);
      assert.equal(outputEvent.blueocean_is_multi_branch, false);
      assert.equal(outputEvent.blueocean_is_for_current_job, false);
  });

  it("freestyle job run event", () => {
      // The run number on the end should not cause a problem
      // Should be ignored.
      const inputEvent = {
          jenkins_object_url: 'job/Myfree/17/',
          job_name: 'Myfree'
      };

      const outputEvent = pushEventUtil.enrichJobEvent(inputEvent, 'Myfree');

      assert.equal(outputEvent.jenkins_object_url, 'job/Myfree/17/');
      assert.equal(outputEvent.job_name, 'Myfree');

      assert.equal(outputEvent.blueocean_job_name, 'Myfree');
      assert.equal(outputEvent.blueocean_branch_name, undefined);
      assert.equal(outputEvent.blueocean_is_multi_branch, false);
      assert.equal(outputEvent.blueocean_is_for_current_job, true);
  });

  it("multi-branch job queued event", () => {
      const inputEvent = {
          jenkins_object_url: 'job/CloudBeers/job/PR-demo/branch/quicker/',
          job_name: 'CloudBeers/PR-demo/quicker'
      };

      const outputEvent = pushEventUtil.enrichJobEvent(inputEvent);

      assert.equal(outputEvent.jenkins_object_url, 'job/CloudBeers/job/PR-demo/branch/quicker/');
      assert.equal(outputEvent.job_name, 'CloudBeers/PR-demo/quicker');

      assert.equal(outputEvent.blueocean_job_name, 'PR-demo');
      assert.equal(outputEvent.blueocean_branch_name, 'quicker');
      assert.equal(outputEvent.blueocean_is_multi_branch, true);
  });

  it("multi-branch job run event", () => {
      // The run number on the end should not cause a problem
      // Should be ignored.
      const inputEvent = {
          jenkins_object_url: 'job/CloudBeers/job/PR-demo/branch/quicker/46/',
          job_name: 'CloudBeers/PR-demo/quicker'
      };

      const outputEvent = pushEventUtil.enrichJobEvent(inputEvent);

      assert.equal(outputEvent.jenkins_object_url, 'job/CloudBeers/job/PR-demo/branch/quicker/46/');
      assert.equal(outputEvent.job_name, 'CloudBeers/PR-demo/quicker');

      assert.equal(outputEvent.blueocean_job_name, 'PR-demo');
      assert.equal(outputEvent.blueocean_branch_name, 'quicker');
      assert.equal(outputEvent.blueocean_is_multi_branch, true);
  });

});

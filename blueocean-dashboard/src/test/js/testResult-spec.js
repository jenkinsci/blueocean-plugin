import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import TestResults from '../../main/js/components/testing/TestResults.jsx';

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-dashboard');

describe("TestResults", () => {
  const testResults1 = {
        "_class":"hudson.tasks.junit.TestResult",
        "duration":0.008,
        "empty":false,
        "failCount":4,
        "passCount":9,
        "skipCount":3,
        "suites":[
            {"cases":[
                    {"age":0,"className":"failure.TestThisWillFailAbunch","duration":0.002,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest3","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                    {"age":0,"className":"failure.TestThisWillFailAbunch","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest4","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                    {"age":4,"className":"failure.TestThisWillFailAbunch","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":7,"name":"aFailingTest2","skipped":true,"skippedMessage":null,"status":"SKIPPED","stderr":null,"stdout":null},
                    {"age":4,"className":"failure.TestThisWillFailAbunch","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":7,"name":"aFailingTest3","skipped":true,"skippedMessage":null,"status":"SKIPPED","stderr":null,"stdout":null},
                    {"age":0,"className":"failure.TestThisWillFailAbunch","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aFailingTest4","skipped":false,"skippedMessage":null,"status":"FIXED","stderr":null,"stdout":null},
                    {"age":1,"className":"failure.TestThisWillFailAbunch","duration":0.003,"errorDetails":"<some exception here>","failedSince":7,"name":"aFailingTest","skipped":false,"skippedMessage":null,"status":"FAILED","stderr":null,"stdout":null},
                    {"age":4,"className":"failure.TestThisWillFailAbunch","duration":0.001,"errorDetails":null,"errorStackTrace":null,"failedSince":7,"name":"aNewFailingTest31","skipped":true,"skippedMessage":null,"status":"SKIPPED","stderr":null,"stdout":null}
                ],
                "duration":0.008,
                "id":null,
                "name":"failure.TestThisWillFailAbunch",
                "stderr":null,
                "stdout":null,
                "timestamp":null
            },
            {"cases":[
                    {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest2","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                    {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest3","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                    {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest4","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                    {"age":4,"className":"failure.TestThisWillFailAbunch","duration":0.003,"errorDetails":"<some exception here>","failedSince":7,"name":"aFailingTest","skipped":false,"skippedMessage":null,"status":"FAILED","stderr":null,"stdout":null},
                    {"age":4,"className":"failure.TestThisWillFailAbunch","duration":0.003,"errorDetails":"<some exception here>","failedSince":7,"name":"aFailingTest","skipped":false,"skippedMessage":null,"status":"FAILED","stderr":null,"stdout":null},
                ],
                "duration":0,
                "id":null,
                "name":"failure.TestThisWontFail",
                "stderr":null,
                "stdout":null,
                "timestamp":null
            }
        ]
    };

  it("Test fixed included", () => {
      let wrapper = shallow(<TestResults t={t} testResults={testResults1} />);

      const fixed = wrapper.find('.new-passed .count').text();
      assert.equal(fixed, 1);

      const failed = wrapper.find('.failed .count').text();
      assert.equal(failed, 4);

      const skipped = wrapper.find('.skipped .count').text();
      assert.equal(skipped, 3);

      const newFailed = wrapper.find('.new-failed .count').text();
      assert.equal(newFailed, 1);
  });

  it("Handles REGRESSION case", () => {
      var failures = {
              "_class":"hudson.tasks.junit.TestResult",
              "duration":0.008, "empty":false, "failCount":3, "passCount":0, "skipCount":0, "suites":[
                { "duration":0, "id":null, "name":"failure.TestThisWontFail", "stderr":null, "stdout":null, "timestamp":null, "cases": [
                {"age":5,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest2","skipped":false,"skippedMessage":null,"status":"FAILED","stderr":null,"stdout":null},
                {"age":2,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest3","skipped":false,"skippedMessage":null,"status":"REGRESSION","stderr":null,"stdout":null},
                {"age":1,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest4","skipped":false,"skippedMessage":null,"status":"FAILED","stderr":null,"stdout":null},
                ],
            }]};

      let wrapper = shallow(<TestResults t={t} testResults={failures} />);
      const newFailed = wrapper.find('.new-failure-block h4').text();
      assert.equal(newFailed, 'New failing - 2');

      const failed = wrapper.find('.existing-failure-block h4').text();
      assert.equal(failed, 'Existing failures - 1');
  });

  it("All passing shown", () => {
      let wrapper = shallow(<TestResults t={t} testResults={testResults1} />);
      let isDone = wrapper.html().indexOf('done_all') > 0;
      assert(!isDone, "Done all found, when shouldn't have been");

      var success = {
              "_class":"hudson.tasks.junit.TestResult",
              "duration":0.008, "empty":false, "failCount":0, "passCount":3, "skipCount":0, "suites":[
                { "duration":0, "id":null, "name":"failure.TestThisWontFail", "stderr":null, "stdout":null, "timestamp":null, "cases": [
                {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest2","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest3","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest4","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                ],
            }]};

      wrapper = shallow(<TestResults t={t} testResults={success} />);
      let html = wrapper.html();
      assert(html.indexOf('done_all') > 0, "Done all not found, when should be");
      assert(html.indexOf('fixed-block') < 0, "No fixed tests!");
  });

  it("All passing and fixed shown", () => {
      var successWithFixed = {
              "_class":"hudson.tasks.junit.TestResult",
              "duration":0.008, "empty":false, "failCount":0, "passCount":3, "skipCount":0, "suites":[
                { "duration":0, "id":null, "name":"failure.TestThisWontFail", "stderr":null, "stdout":null, "timestamp":null, "cases": [
                {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest2","skipped":false,"skippedMessage":null,"status":"FIXED","stderr":null,"stdout":null},
                {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest3","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                {"age":0,"className":"failure.TestThisWontFail","duration":0,"errorDetails":null,"errorStackTrace":null,"failedSince":0,"name":"aPassingTest4","skipped":false,"skippedMessage":null,"status":"PASSED","stderr":null,"stdout":null},
                ],
            }]};

      let wrapper = shallow(<TestResults t={t} testResults={successWithFixed} />);
      let html = wrapper.html();
      assert(html.indexOf('done_all') > 0, "Done all not found, when should be");
      assert(html.indexOf('fixed-block') > 0, "Should have fixed tests!");
  });
});

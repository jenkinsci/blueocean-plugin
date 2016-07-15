import React from 'react';
import {createRenderer} from 'react-addons-test-utils';
import { assert} from 'chai';
import { shallow } from 'enzyme';

import TestResults from '../../main/js/components/testing/TestResults.jsx';

describe("TestResults", () => {
  let wrapper;
  const
    renderer = createRenderer();

  beforeEach(() => {
    const testResults = {
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
    wrapper = shallow(<TestResults testResults={testResults} />);
  });

  it("Test fixed included", () => {
      const fixed = wrapper.find('.new-passed .count').text();
      assert.equal(fixed, 1);

      const failed = wrapper.find('.failed .count').text();
      assert.equal(failed, 4);

      const skipped = wrapper.find('.skipped .count').text();
      assert.equal(skipped, 3);

      const newFailed = wrapper.find('.new-failed .count').text();
      assert.equal(newFailed, 1);
  });
});

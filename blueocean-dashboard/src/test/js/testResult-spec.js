import { prepareMount } from './util/EnzymeUtils';
prepareMount();

import React from 'react';
import { assert } from 'chai';
import { shallow, mount } from 'enzyme';

import TestResults from '../../main/js/components/testing/TestResults.jsx';

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-dashboard');

describe("TestResults", () => {
    const testResults1 = {
        "_class": "hudson.tasks.junit.TestResult",
        "duration": 0.008,
        "empty": false,
        "failCount": 4,
        "passCount": 9,
        "skipCount": 3,
        "suites": [
            {
                "cases": [
                    { "age": 0, "className": "failure.TestThisWillFailAbunch", "duration": 0.002, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest3", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    { "age": 0, "className": "failure.TestThisWillFailAbunch", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest4", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    { "age": 4, "className": "failure.TestThisWillFailAbunch", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 7, "name": "aFailingTest2", "skipped": true, "skippedMessage": null, "status": "SKIPPED", "stderr": null, "stdout": null },
                    { "age": 4, "className": "failure.TestThisWillFailAbunch", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 7, "name": "aFailingTest3", "skipped": true, "skippedMessage": null, "status": "SKIPPED", "stderr": null, "stdout": null },
                    { "age": 0, "className": "failure.TestThisWillFailAbunch", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aFailingTest4", "skipped": false, "skippedMessage": null, "status": "FIXED", "stderr": null, "stdout": null },
                    { "age": 1, "className": "failure.TestThisWillFailAbunch", "duration": 0.003, "errorDetails": "<some exception here>", "failedSince": 7, "name": "aFailingTest", "skipped": false, "skippedMessage": null, "status": "FAILED", "stderr": null, "stdout": null },
                    { "age": 4, "className": "failure.TestThisWillFailAbunch", "duration": 0.001, "errorDetails": null, "errorStackTrace": null, "failedSince": 7, "name": "aNewFailingTest31", "skipped": true, "skippedMessage": null, "status": "SKIPPED", "stderr": null, "stdout": null }
                ],
                "duration": 0.008,
                "id": null,
                "name": "failure.TestThisWillFailAbunch",
                "stderr": null,
                "stdout": null,
                "timestamp": null
            },
            {
                "cases": [
                    { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest2", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest3", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest4", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    { "age": 4, "className": "failure.TestThisWillFailAbunch", "duration": 0.003, "errorDetails": "<some exception here>", "failedSince": 7, "name": "aFailingTest", "skipped": false, "skippedMessage": null, "status": "FAILED", "stderr": null, "stdout": null },
                    { "age": 4, "className": "failure.TestThisWillFailAbunch", "duration": 0.003, "errorDetails": "<some exception here>", "failedSince": 7, "name": "aFailingTest", "skipped": false, "skippedMessage": null, "status": "FAILED", "stderr": null, "stdout": null },
                ],
                "duration": 0,
                "id": null,
                "name": "failure.TestThisWontFail",
                "stderr": null,
                "stdout": null,
                "timestamp": null
            }
        ]
    };

    it("Test fixed included", () => {
        const wrapper = shallow(<TestResults t={t} testResults={testResults1} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.failing_title') >= 0, 'should show failing title');
        assert(output.indexOf('new-failure-block') >= 0, 'should find new failure block');
        assert(output.indexOf('existing-failure-block') >= 0, 'should find existing failure block');
        assert(output.indexOf('fixed-block') >= 0, 'should find a fixed block');
        assert(output.indexOf('skipped-block') >= 0, 'should find a skipped blocks');
    });

    it("Handles REGRESSION case", () => {
        var failures = {
            "_class": "hudson.tasks.junit.TestResult",
            "duration": 0.008, "empty": false, "failCount": 3, "passCount": 0, "skipCount": 0, "suites": [
                {
                    "duration": 0, "id": null, "name": "failure.TestThisWontFail", "stderr": null, "stdout": null, "timestamp": null, "cases": [
                        { "age": 5, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest2", "skipped": false, "skippedMessage": null, "status": "FAILED", "stderr": null, "stdout": null },
                        { "age": 2, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest3", "skipped": false, "skippedMessage": null, "status": "REGRESSION", "stderr": null, "stdout": null },
                        { "age": 1, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest4", "skipped": false, "skippedMessage": null, "status": "FAILED", "stderr": null, "stdout": null },
                    ],
                }]
        };

        let wrapper = shallow(<TestResults t={t} testResults={failures} />);
        const newFailed = wrapper.find('.new-failure-block h4').text();
        assert.equal(newFailed, 'New failing - 2');

        const failed = wrapper.find('.existing-failure-block h4').text();
        assert.equal(failed, 'Existing failures - 1');
    });

    it("All passing shown", () => {
        var success = {
            "_class": "hudson.tasks.junit.TestResult",
            "duration": 0.008, "empty": false, "failCount": 0, "passCount": 3, "skipCount": 0, "suites": [
                {
                    "duration": 0, "id": null, "name": "failure.TestThisWontFail", "stderr": null, "stdout": null, "timestamp": null, "cases": [
                    { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest2", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest3", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest4", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                ],
                }]
        };

        const wrapper = shallow(<TestResults t={t} testResults={success} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.passing_title') >= 0, 'should show all passing title');
        assert(output.indexOf('-failure-block') < 0, 'should not find any failure blocks');
        assert(output.indexOf('fixed-block') < 0, 'should not find a fixed block');
        assert(output.indexOf('skipped-block') < 0, 'should not find a skipped blocks');
    });

    it("All passing and fixed shown", () => {
        var successWithFixed = {
            "_class": "hudson.tasks.junit.TestResult",
            "duration": 0.008, "empty": false, "failCount": 0, "passCount": 3, "skipCount": 0, "suites": [
                {
                    "duration": 0, "id": null, "name": "failure.TestThisWontFail", "stderr": null, "stdout": null, "timestamp": null, "cases": [
                        { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest2", "skipped": false, "skippedMessage": null, "status": "FIXED", "stderr": null, "stdout": null },
                        { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest3", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                        { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": null, "failedSince": 0, "name": "aPassingTest4", "skipped": false, "skippedMessage": null, "status": "PASSED", "stderr": null, "stdout": null },
                    ],
                }]
        };

        const wrapper = shallow(<TestResults t={t} testResults={successWithFixed} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.passing_after_fixes_title') >= 0, 'should show passing with fixes title');
        assert(output.indexOf('-failure-block') < 0, 'should not find any failure blocks');
        assert(output.indexOf('fixed-block') >= 0, 'should find a fixed block');
        assert(output.indexOf('skipped-block') < 0, 'should not find a skipped blocks');
    });

    it("unstable renders with no error message", () => {
        var unstableWithNoMsg = {
            "_class": "hudson.tasks.junit.TestResult",
            "duration": 0.008, "empty": false, "failCount": 1, "passCount": 3, "skipCount": 0, "suites": [
                {
                    "duration": 0, "id": null, "name": "failure.TestThisWontFail", "stderr": null, "stdout": null, "timestamp": null, "cases": [
                        { "age": 0, "className": "failure.TestThisWontFail", "duration": 0, "errorDetails": null, "errorStackTrace": 'aa', "failedSince": 0, "name": "aPassingTest2", "skipped": false, "skippedMessage": null, "status": "FAILED", "stderr": null, "stdout": null },
                    ],
                }]
        };
        // Lets mount it to that it renders children.
        let wrapper = mount(<TestResults t={t} testResults={unstableWithNoMsg} />);

        // Expend the test result
        wrapper.find('.result-item-head').simulate('click');

        // Assert that it expanded and just shows the stacktrace.
        assert.equal(wrapper.find('.test-console h4').length, 1);
    });
});

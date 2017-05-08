import {prepareMount} from "./util/EnzymeUtils";
import React from "react";
import {assert} from "chai";
import {mount, shallow} from "enzyme";

import TestResults from "../../main/js/components/testing/TestResults.jsx";

import { mockExtensionsForI18n } from './mock-extensions-i18n';
mockExtensionsForI18n();

import {i18nTranslator} from "@jenkins-cd/blueocean-core-js";
prepareMount();

const t = i18nTranslator('blueocean-dashboard');

describe('TestResults', () => {

    const bigRun = {
        testSummary: {
            existingFailed: 3,
            failed: 3,
            fixed: 0,
            passed: 10,
            regressions: 0,
            skipped: 3,
            total: 16,
        },
    };

    const bigTests = [
        { age: 0, name: 'failure.TestThisWillFailAbunch', duration: 0.002, status: 'PASSED' },
        { age: 0, name: 'failure.TestThisWillFailAbunch', duration: 0, status: 'PASSED' },
        { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0, status: 'SKIPPED' },
        { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0, status: 'SKIPPED' },
        { age: 0, name: 'failure.TestThisWillFailAbunch', duration: 0, status: 'PASSED', state: 'FIXED' },
        { age: 1, name: 'failure.TestThisWillFailAbunch', duration: 0.003, errorDetails: '<some exception here>', status: 'FAILED' },
        { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0.001, status: 'SKIPPED' },
        { age: 0, name: 'failure.TestThisWontFail', duration: 0, status: 'PASSED' },
        { age: 0, name: 'failure.TestThisWontFail', duration: 0, status: 'PASSED' },
        { age: 0, name: 'failure.TestThisWontFail', duration: 0, status: 'PASSED' },
        { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0.003, errorDetails: '<some exception here>', status: 'FAILED' },
        { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0.003, errorDetails: '<some exception here>', status: 'FAILED' },
    ];

    it('Test fixed included', () => {
        const wrapper = shallow(<TestResults t={t} run={bigRun} tests={bigTests} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.failing_title') >= 0, 'should show failing title');
        assert(output.indexOf('new-failure-block') >= 0, 'should find new failure block');
        assert(output.indexOf('existing-failure-block') >= 0, 'should find existing failure block');
        assert(output.indexOf('fixed-block') >= 0, 'should find a fixed block');
        assert(output.indexOf('skipped-block') >= 0, 'should find a skipped blocks');
    });

    it('Handles REGRESSION case', () => {
        const failures = [
            { age: 5, name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, status: 'FAILED', state: 'UNKNOWN' },
            { age: 2, name: 'failure.TestThisWontFail - aPassingTest3', duration: 0, status: 'FAILED', state: 'REGRESSION' },
            { age: 1, name: 'failure.TestThisWontFail - aPassingTest4', duration: 0, status: 'FAILED', state: 'UNKNOWN' },
        ];
        const run = {
            testSummary: {
                existingFailed: 1,
                failed: 3,
                fixed: 0,
                passed: 0,
                regressions: 2,
                skipped: 0,
                total: 3,
            },
        };

        const wrapper = shallow(<TestResults t={t} run={run} tests={failures} />);
        const newFailed = wrapper.find('.new-failure-block h4').text();
        assert.equal(newFailed, 'New failing - 2');

        const failed = wrapper.find('.existing-failure-block h4').text();
        assert.equal(failed, 'Existing failures - 1');
    });

    it('All passing shown', () => {
        const success = [
            { age: 0, name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, status: 'PASSED' },
            { age: 0, name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, status: 'PASSED' },
            { age: 0, name: 'failure.TestThisWontFail - aPassingTest4', duration: 0, status: 'PASSED' },
        ];
        const run = {
            testSummary: {
                existingFailed: 0,
                failed: 0,
                fixed: 0,
                passed: 3,
                regressions: 0,
                skipped: 0,
                total: 3,
            },
        };

        const wrapper = shallow(<TestResults t={t} run={run} tests={success} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.passing_title') >= 0, 'should show all passing title');
        assert(output.indexOf('-failure-block') < 0, 'should not find any failure blocks');
        assert(output.indexOf('fixed-block') < 0, 'should not find a fixed block');
        assert(output.indexOf('skipped-block') < 0, 'should not find a skipped blocks');
    });

    it('All passing and fixed shown', () => {
        const successWithFixed = [
            { name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, age: 0, status: 'PASSED', state: 'FIXED' },
            { name: 'failure.TestThisWontFail - aPassingTest3', duration: 0, age: 0, status: 'PASSED', state: 'UNKNOWN' },
            { name: 'failure.TestThisWontFail - aPassingTest4', duration: 0, age: 0, status: 'PASSED', state: 'UNKNOWN' },
        ];
        const run = {
            testSummary: {
                existingFailed: 0,
                failed: 0,
                fixed: 1,
                passed: 3,
                regressions: 0,
                skipped: 0,
                total: 3,
            },
        };

        const wrapper = shallow(<TestResults t={t} run={run} tests={successWithFixed} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.passing_after_fixes_title') >= 0, 'should show passing with fixes title');
        assert(output.indexOf('-failure-block') < 0, 'should not find any failure blocks');
        assert(output.indexOf('fixed-block') >= 0, 'should find a fixed block');
        assert(output.indexOf('skipped-block') < 0, 'should not find a skipped blocks');
    });

    it('unstable renders with no error message', () => {
        const unstableWithNoMsg = [
            { age: 0, name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, errorDetails: null, errorStackTrace: 'aa', status: 'FAILED' },
        ];
        const run = {
            testSummary: {
                existingFailed: 0,
                failed: 1,
                fixed: 0,
                passed: 0,
                regressions: 0,
                skipped: 0,
                total: 1,
            },
        };
        // Lets mount it to that it renders children.
        const wrapper = mount(<TestResults t={t} run={run} tests={unstableWithNoMsg} />);

        // Expend the test result
        wrapper.find('.result-item-head').simulate('click');

        // Assert that it expanded and just shows the stacktrace.
        assert.equal(wrapper.find('.test-console h4').length, 1);
    });
});

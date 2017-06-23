import { prepareMount } from './util/EnzymeUtils';
import React from 'react';
import { assert } from 'chai';
import { mount } from 'enzyme';

import TestResults from '../../main/js/components/testing/TestResults.jsx';

import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import { mockExtensionsForI18n } from './mock-extensions-i18n';

mockExtensionsForI18n();
prepareMount();

const t = i18nTranslator('blueocean-dashboard');

describe('TestResults', () => {

    const pipeline = {
        organization: 'myteam'
    };

    class MockTestService {

        constructor({regressions, existingFailed, skipped, fixed}) {
            this.regressions = regressions || [];
            this.existingfailed = existingFailed || [];
            this.skipped = skipped || [];
            this.fixed = fixed || [];
        }

        newRegressionsPager(pipeline, run) {
            return {
                data: this.regressions
            };
        }

        newExistingFailedPager(pipeline, run) {
            return {
                data: this.existingfailed
            };
        }

        newSkippedPager(pipeline, run) {
            return {
                data: this.skipped
            };
        }

        newFixedPager(pipeline, run) {
            return {
                data: this.fixed
            };
        }

        testLogs() {
            return {
                loadStdOut: () => {},
                loadStdErr: () => {},
                getStdOut: () => { return null; },
                getStdErr: () => { return null; },
            }
        }
    }

    it('Test fixed included', () => {
        const skipped = [
            { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0.001, status: 'SKIPPED' },
            { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0, status: 'SKIPPED' },
            { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0, status: 'SKIPPED' },
        ];
        const fixed = [
            { age: 0, name: 'failure.TestThisWillFailAbunch', duration: 0, status: 'PASSED', state: 'FIXED' },
        ];
        const existingFailed = [
            { age: 1, name: 'failure.TestThisWillFailAbunch', duration: 0.003, errorDetails: '<some exception here>', status: 'FAILED' },
            { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0.003, errorDetails: '<some exception here>', status: 'FAILED' },
            { age: 4, name: 'failure.TestThisWillFailAbunch', duration: 0.003, errorDetails: '<some exception here>', status: 'FAILED' },
        ];

        const run = {
            testSummary: {
                existingFailed: 3,
                failed: 0,
                fixed: 1,
                passed: 10,
                regressions: 0,
                skipped: 3,
                total: 16,
            },
            '_links': {
                tests: {
                    href: '/pipeline/runs/1/tests'
                }
            }
        };

        const testService = new MockTestService({fixed: fixed, skipped: skipped, existingFailed: existingFailed});

        const wrapper = mount(<TestResults t={t} run={run} testService={testService} pipeline={pipeline} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.failing_title') >= 0, 'should show failing title');
        assert(output.indexOf('new-failure-block') === -1, 'should not have failure block');
        assert(output.indexOf('existing-failure-block') >= 0, 'should find existing failure block');
        assert(output.indexOf('fixed-block') >= 0, 'should find a fixed block');
        assert(output.indexOf('skipped-block') >= 0, 'should find a skipped blocks');
    });

    it('Handles REGRESSION case', () => {
        const existingFailed = [
            { age: 5, name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, status: 'FAILED', state: 'UNKNOWN' },
            { age: 1, name: 'failure.TestThisWontFail - aPassingTest4', duration: 0, status: 'FAILED', state: 'UNKNOWN' },
        ];
        const regressions = [
            { age: 2, name: 'failure.TestThisWontFail - aPassingTest3', duration: 0, status: 'FAILED', state: 'REGRESSION' }
        ];

        const testService = new MockTestService({regressions: regressions, existingFailed: existingFailed});

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
            '_links': {
                tests: {
                    href: '/pipeline/runs/1/tests'
                }
            }
        };

        const wrapper = mount(<TestResults t={t} run={run} testService={testService} pipeline={pipeline} />);
        const newFailed = wrapper.find('.new-failure-block h4').text();
        assert.equal(newFailed, 'rundetail.tests.results.errors.new.count');

        const failed = wrapper.find('.existing-failure-block h4').text();
        assert.equal(failed, 'rundetail.tests.results.errors.existing.count');
    });

    it('All passing shown', () => {
        const testService = new MockTestService({});

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
            '_links': {
                tests: {
                    href: '/pipeline/runs/1/tests'
                }
            }
        };

        const wrapper = mount(<TestResults t={t} run={run} testService={testService} pipeline={pipeline} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.passing_title') >= 0, 'should show all passing title');
        assert(output.indexOf('-failure-block') < 0, 'should not find any failure blocks');
        assert(output.indexOf('fixed-block') < 0, 'should not find a fixed block');
        assert(output.indexOf('skipped-block') < 0, 'should not find a skipped blocks');
    });

    it('All passing and fixed shown', () => {
        const fixed = [
            {name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, age: 0, status: 'PASSED', state: 'FIXED' },
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
        const testService = new MockTestService({passed: fixed});

        const wrapper = mount(<TestResults t={t} run={run} testService={testService} pipeline={pipeline} />);
        const output = wrapper.html();
        assert(output.indexOf('summary.passing_after_fixes_title') >= 0, 'should show passing with fixes title');
        assert(output.indexOf('-failure-block') < 0, 'should not find any failure blocks');
        assert(output.indexOf('fixed-block') >= 0, 'should find a fixed block');
        assert(output.indexOf('skipped-block') < 0, 'should not find a skipped blocks');
    });

    it('unstable renders with no error message', () => {
        const existingFailed = [
            { age: 0, name: 'failure.TestThisWontFail - aPassingTest2', duration: 0, errorDetails: null, errorStackTrace: 'aa', status: 'FAILED' },
        ];
        const run = {
            testSummary: {
                existingFailed: 1,
                failed: 0,
                fixed: 0,
                passed: 0,
                regressions: 0,
                skipped: 0,
                total: 1,
            },
            '_links': {
                tests: {
                    href: '/pipeline/runs/1/tests'
                }
            }
        };
        const testService = new MockTestService({existingFailed: existingFailed});
        // Lets mount it to that it renders children.
        const wrapper = mount(<TestResults t={t} run={run} testService={testService} pipeline={pipeline} />);

        // Expend the test result
        wrapper.find('.result-item-head').simulate('click');

        // Assert that it expanded and just shows the stacktrace.
        assert.equal(wrapper.find('.test-console h4').length, 1);
    });
});

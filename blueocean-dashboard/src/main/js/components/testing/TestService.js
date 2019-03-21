import { BunkerService, Pager } from '@jenkins-cd/blueocean-core-js';
import TestLogService from './TestLogService';

const PAGE_SIZE = 100;

// Source: https://stackoverflow.com/questions/286141/remove-blank-attributes-from-an-object-in-javascript/38340730#38340730
const stripNullAndUndefined = obj =>
    Object.keys(obj)
        .filter(k => obj[k] !== null && obj[k] !== undefined) // Remove undef. and null.
        .reduce(
            (newObj, k) =>
                typeof obj[k] === 'object'
                    ? Object.assign(newObj, { [k]: stripNullAndUndefined(obj[k]) }) // Recurse.
                    : Object.assign(newObj, { [k]: obj[k] }), // Copy value.
            {}
        );

export default class TestService extends BunkerService {
    constructor(pagerService) {
        super(pagerService);
        this._logs = new TestLogService(pagerService);
    }

    newRegressionsPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/regressions/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: null, state: 'REGRESSION' }), PAGE_SIZE, this),
        });
    }

    newExistingFailedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/existingFailures/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: 'FAILED', state: '!REGRESSION' }), PAGE_SIZE, this),
        });
    }

    newSkippedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/skipped/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: 'SKIPPED', state: null }), PAGE_SIZE, this),
        });
    }

    newFixedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/fixed/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: null, state: 'FIXED' }), PAGE_SIZE, this),
        });
    }

    newPassedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/passed/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: 'PASSED', state: null }), PAGE_SIZE, this),
        });
    }
    testLogs() {
        return this._logs;
    }

    static createURL({ run, status, state, age }) {
        const testBaseUrl = run._links.tests.href;
        const params = new URLSearchParams(stripNullAndUndefined({ status, state, age }));
        if (!params) {
            throw new Error('must provide either stage, status or age');
        }
        return `${testBaseUrl}?${params}`;
    }
}

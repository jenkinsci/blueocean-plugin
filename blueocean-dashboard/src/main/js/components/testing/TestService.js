import { BunkerService, Pager } from '@jenkins-cd/blueocean-core-js';
import TestLogService from './TestLogService';

const PAGE_SIZE = 100;

export default class TestService extends BunkerService {
    constructor(pagerService) {
        super(pagerService);
        this._logs = new TestLogService(pagerService);
    }

    newRegressionsPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/regressions/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL(run, { state: 'REGRESSION' }), PAGE_SIZE, this),
        });
    }

    newExistingFailedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/existingFailures/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL(run, { status: 'FAILED', state: '!REGRESSION' }), PAGE_SIZE, this),
        });
    }

    newSkippedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/skipped/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL(run, { status: 'SKIPPED' }), PAGE_SIZE, this),
        });
    }

    newFixedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/fixed/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL(run, { state: 'FIXED' }), PAGE_SIZE, this),
        });
    }

    newPassedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/passed/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL(run, { status: 'PASSED' }), PAGE_SIZE, this),
        });
    }
    testLogs() {
        return this._logs;
    }

    static createURL(run, searchParams) {
        const testBaseUrl = run._links.tests.href;
        const params = new URLSearchParams(searchParams);
        return `${testBaseUrl}?${new URLSearchParams(params)}`;
    }
}

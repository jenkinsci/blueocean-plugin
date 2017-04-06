import { BunkerService } from '../../../../../../blueocean-core-js/src/js/services/BunkerService';
import { Pager } from '@jenkins-cd/blueocean-core-js';
import TestLogService from './TestLogService';

export default class TestService extends BunkerService {

    constructor(pagerService) {
        super(pagerService);
        this._logs = new TestLogService(pagerService);
    }

    newRegressionsPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/regressions/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: null, state: 'REGRESSION' }), 100, this),
        });
    }

    newExistingFailedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/existingFailures/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: 'FAILED', state: null }), 100, this),
        });
    }

    newSkippedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/skipped/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: 'SKIPPED', state: null }), 100, this),
        });
    }

    newFixedPager(pipeline, run) {
        return this.pagerService.getPager({
            key: `tests/fixed/${pipeline.organization}-${pipeline.name}-${run.id}/`,
            lazyPager: () => new Pager(TestService.createURL({ run, status: null, state: 'FIXED' }), 100, this),
        });
    }

    testLogs() {
        return this._logs;
    }

    static createURL({ run, status, state }) {
        if (status && state) {
            throw new Error('status and state are exclusive');
        }
        const testBaseUrl = run._links.tests.href;
        if (status) {
            return `${testBaseUrl}?status=${status}`;
        }
        if (state) {
            return `${testBaseUrl}?state=${state}`;
        }
        throw new Error('must provide either status or state');
    }
}

import { assert } from 'chai';

import DelayUtils from '../../../main/js/util/PromiseDelayUtils';


const { delayResolve, delayReject, delayBoth } = DelayUtils;
const DELAY_LESS = 10;
const DELAY_ACTUAL = 50;
const DELAY_MORE = 100;

const mockAsync = {
    getResult: (delay = DELAY_ACTUAL) => (
        new Promise(resolve => {
            setTimeout(() => resolve('result'), delay);
        })
    ),
    getReject: (delay = DELAY_ACTUAL) => (
        new Promise((resolve, reject) => {
            setTimeout(() => reject(new Error('reject')), delay);
        })
    ),
};


// NOTE: these don't actually assert on timing because it'd be too flaky
// timings can be examined in Jest output however (either ~50ms or ~100ms)
describe('PromiseDelayUtils', () => {
    describe('delayResolve', () => {
        it('handle delay duration less than resolve', (done) => {
            mockAsync.getResult()
                .then(
                    delayResolve(DELAY_LESS)
                )
                .then(
                    result => {
                        assert.equal(result, 'result');
                        done();
                    }
                );
        });
        it('handle delay duration more than resolve', (done) => {
            mockAsync.getResult()
                .then(
                    delayResolve(DELAY_MORE)
                )
                .then(
                    result => {
                        assert.equal(result, 'result');
                        done();
                    }
                );
        });
    });

    describe('delayReject', () => {
        it('handle delay duration less than reject', (done) => {
            mockAsync.getReject()
                .then(
                    result => result,
                    delayReject(DELAY_LESS)
                )
                .then(
                    result => result,
                    error => {
                        assert.equal(error.message, 'reject');
                        done();
                    }
                );
        });
        it('handle delay duration more than reject', (done) => {
            mockAsync.getReject()
                .then(
                    result => result,
                    delayReject(DELAY_MORE)
                )
                .then(
                    result => result,
                    error => {
                        assert.equal(error.message, 'reject');
                        done();
                    }
                );
        });
    });
    describe('delayBoth', () => {
        it('handle delay duration less than resolve', (done) => {
            mockAsync.getResult()
                .then(...delayBoth(DELAY_LESS))
                .then(
                    result => {
                        assert.equal(result, 'result');
                        done();
                    }
                );
        });
        it('handle delay duration more than resolve', (done) => {
            mockAsync.getResult()
                .then(...delayBoth(DELAY_MORE))
                .then(
                    result => {
                        assert.equal(result, 'result');
                        done();
                    }
                );
        });
        it('handle delay duration less than reject', (done) => {
            mockAsync.getReject()
                .then(...delayBoth(DELAY_LESS))
                .then(
                    result => result,
                    error => {
                        assert.equal(error.message, 'reject');
                        done();
                    }
                );
        });
        it('handle delay duration more than reject', (done) => {
            mockAsync.getReject()
                .then(...delayBoth(DELAY_MORE))
                .then(
                    result => result,
                    error => {
                        assert.equal(error.message, 'reject');
                        done();
                    }
                );
        });
    });
});

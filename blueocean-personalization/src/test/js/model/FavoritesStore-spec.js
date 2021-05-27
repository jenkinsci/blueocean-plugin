/**
 * Created by cmeyers on 7/11/16.
 */
import {assert} from 'chai';
import {Fetch} from '@jenkins-cd/blueocean-core-js';

import store from '../../../main/js/model/FavoriteStore';

const _links = {self: {href: 'dummy'}};

const fj = Fetch.fetchJSON;

function assertTransition(fullName, isPipeline, isPrimaryBranch, primaryBranchName, before, after, done) {
    const item = {fullName: fullName, _links};
    // partial mock of server response to favorite call
    const response = {item: {fullName: fullName + (isPipeline ? '/' + primaryBranchName : ''), branch: isPipeline ? undefined : {isPrimary: isPrimaryBranch}}};

    if (before) {
        store._favoritesList = [...store._favoritesList, {name: fullName + (isPipeline ? '/' + primaryBranchName : ''), primary: isPrimaryBranch}];
    }
    // ensure initial state
    assert.equal(store.isFavorite(item), before, "initial state invalid");

    Fetch.fetchJSON = () => {
        return {
            then: () => {
                return {
                    catch: () => {
                        return {
                            then: (fn) => {
                                fn(response);
                                // ensure state changed
                                assert.equal(store.isFavorite(item), after, "state not updated");
                                done();
                            }
                        }
                    }
                }
            }
        }
    };

    store.setFavorite(item, after);
}

describe('FavoriteStore', () => {
    beforeEach(() => {
        store._fetched = true;
        store._favoritesList = [{name: 'pipe/master', primary: true}, {name: 'pipe/duh', primary: false}, {name: 'other2/foo', primary: false}];
    });

    afterEach(() => {
        Fetch.fetchJSON = fj;
    });

    // branch (top level)
    it('adds a new favorite - with master as primary', (done) => {
        assertTransition('third/foo', false, false, 'master', false, true, () => {
            assert.equal(store.isFavorite({fullName: 'third'}), false, "state of pipeline not updated");
            done();
        });
    });

    it('removes an existing favorite - with master as primary', (done) => {
        assertTransition('third/foo', false, false, 'master', true, false, () => {
            assert.equal(store.isFavorite({fullName: 'third'}), false, "state of pipeline not updated");
            done();
        });
    });

    it('adds a new favorite - with main as primary', (done) => {
        assertTransition('third/foo', false, false, 'main', false, true, () => {
            assert.equal(store.isFavorite({fullName: 'third'}), false, "state of pipeline not updated");
            done();
        });
    });

    it('removes an existing favorite - with main as primary', (done) => {
        assertTransition('third/foo', false, false, 'main', true, false, () => {
            assert.equal(store.isFavorite({fullName: 'other'}), false, "state of pipeline not updated");
            done();
        });
    });

    // pipeline (top level)
    it('adds a new favorite for pipeline - with master as primary', (done) => {
        assertTransition('third', true, true, 'master', false, true, () => {
            assert.equal(store.isFavorite({fullName: 'third/master'}), true, "state of pipeline not updated");
            done();
        });
    });

    it('removes a favorite for pipeline - with master as primary', (done) => {
        assertTransition('third', true, true, 'master', true, false, () => {
            assert.equal(store.isFavorite({fullName: 'third/main'}), false, "state of pipeline not updated");
            done();
        });
    });

    it('adds a new favorite for pipeline - with main as primary', (done) => {
        assertTransition('third', true, true, 'main', false, true, () => {
            assert.equal(store.isFavorite({fullName: 'third/main'}), true, "state of pipeline not updated");
            done();
        });
        // assertTransition('third/foo', false, true, done);
    });

    it('removes a favorite for pipeline - with main as primary', (done) => {
        assertTransition('third', true, true, 'main', true, false, () => {
            assert.equal(store.isFavorite({fullName: 'third/main'}), false, "state of pipeline not updated");
            done();
        });
    });

    // pipeline (sub project)
    it('adds a new favorite for sub project - with master as primary', (done) => {
        assertTransition('top/third', true, true, 'master', false, true, done);
    });

    it('removes a favorite for sub project - with master as primary', (done) => {
        assertTransition('top/third', true, true, 'master', true, false, done);
    });

    it('adds a new favorite for sub project - with main as primary', (done) => {
        assertTransition('top/third', true, true, 'main', true, false, done);
        // assertTransition('third/foo', false, true, done);
    });

    it('removes a favorite for sub project - with main as primary', (done) => {
        assertTransition('top/third', true, true, 'main', true, false, done);
    });

    // branch (sub project)
    it('adds a new favorite for sub project branch - with master as primary', (done) => {
        assertTransition('top/sub/third', false, false, 'master', false, true, done);
    });

    it('removes a favorite for sub project branch - with master as primary', (done) => {
        assertTransition('top/sub/third', false, false, 'master', true, false, done);
    });

    it('adds a new favorite for sub project branch - with main as primary', (done) => {
        assertTransition('top/sub/third', false, false, 'main', true, false, done);
    });

    it('removes a favorite for sub project branch - with main as primary', (done) => {
        assertTransition('top/sub/third', false, false, 'main', true, false, done);
    });

    // primary branch directly (top level)
    it('adds a new favorite for primary branch directly - with master as primary', (done) => {
        assertTransition('third/master', false, true, 'master', false, true, () => {
            assert.equal(store.isFavorite({fullName: 'third'}), true, "state of pipeline not updated");
            done();
        });
    });

    it('removes a favorite for primary branch directly - with master as primary', (done) => {
        assertTransition('third/master', false, true, 'master', true, false, () => {
            assert.equal(store.isFavorite({fullName: 'third'}), false, "state of pipeline not updated");
            done();
        });
    });

    it('adds a new favorite for primary branch directly - with main as primary', (done) => {
        assertTransition('third/main', false, true, 'main', false, true, () => {
            assert.equal(store.isFavorite({fullName: 'third'}), true, "state of pipeline not updated");
            done();
        });
    });

    it('removes a favorite for primary branch directly - with main as primary', (done) => {
        assertTransition('third/main', false, true, 'main', true, false, () => {
            assert.equal(store.isFavorite({fullName: 'third'}), false, "state of pipeline not updated");
            done();
        });
    });

    // primary branch directly (sub project)
    it('adds a new favorite for primary branch directly - with master as primary', (done) => {
        assertTransition('top/third/master', false, true, 'master', false, true, done);
    });

    it('removes a favorite for primary branch directly - with master as primary', (done) => {
        assertTransition('top/third/master', false, true, 'master', true, false, done);
    });

    it('adds a new favorite for primary branch directly - with main as primary', (done) => {
        assertTransition('top/third/main', false, true, 'main', true, false, done);
    });

    it('removes a favorite for primary branch directly - with main as primary', (done) => {
        assertTransition('top/third/main', false, true, 'main', true, false, done);
    });
});

/**
 * Created by cmeyers on 7/11/16.
 */
import { assert } from 'chai';
import { Fetch } from '@jenkins-cd/blueocean-core-js';

import store from '../../../main/js/model/FavoriteStore';

const _links = { self: { href: 'asdf' } };

const fj = Fetch.fetchJSON;

describe('FavoriteStore', () => {
    beforeEach(() => {
        store._fetched = true;
        store._favoritesNames = [ 'pipe/master', 'pipe/duh', 'other/foo' ];
    });

    afterEach(() => {
        Fetch.fetchJSON = fj;
    });

    it('removes an existing favorite when toggled', (done) => {
        const item = { fullName: 'other/foo', _links };

        // ensure favorited
        assert(store.isFavorite(item));

        Fetch.fetchJSON = () => { return {
            then: () => { return {
                catch: () => { return {
                    then: (fn) => {
                        fn();
                        // ensure not favorited
                        assert(!store.isFavorite(item));
                        done();
                    }
                }}
            }}
        }};

        store.setFavorite(item, false);
    });

    it('adds a new favorite when toggled', (done) => {
        const item = { fullName: 'third/foo', _links };

        // ensure not favorited
        assert(!store.isFavorite(item));

        Fetch.fetchJSON = () => { return {
            then: () => { return {
                catch: () => { return {
                    then: (fn) => {
                        fn();
                        // ensure favorited
                        assert(store.isFavorite(item));
                        done();
                    }
                }}
            }}
        }};

        store.setFavorite(item, true);
    });
});

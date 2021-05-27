/**
 * Created by cmeyers on 8/12/16.
 */
import { SseBus as sseBus } from '@jenkins-cd/blueocean-core-js';
import favoriteStore from './FavoriteStore';

/**
 * Class that acts as a bridge between SSE and the store/actions.
 * Needs to be a "Singleton" so the subscription can be maintained across route changes.
 * TODO: should cleaner way of registering a long-lived component which can easily access stores, services, etc.
 */
class FavoritesSseListener {
    initialize(jobListener) {
        // prevent leaking by disposing of any prior listeners
        if (this.sseBus) {
            this.sseBus.dispose();
        }

        this.sseBus = sseBus;
        try {
            this.id = this.sseBus.subscribeToJob(jobListener, event => this._filterJobs(event));
        } catch (e) {
            if (!this.sseBus.connection && (!global.window || !global.window.EventSource)) {
                // This should only happen in tests i.e no browser/window, EventSource etc.
                // Maybe we could add something to the SSE gateway API that auto enables the
                // headless client in this situation.
                console.warn('SSE connection failed. Push notifications to Favorites will not work.');
            } else {
                throw e;
            }
        }
    }

    unsubscribe() {
        if (this.id) {
            this.sseBus.unsubscribe(this.id);
        }
    }

    _filterJobs(event) {
        return favoriteStore.isFavorite({fullName: event.job_name});
    }
}

export default new FavoritesSseListener();

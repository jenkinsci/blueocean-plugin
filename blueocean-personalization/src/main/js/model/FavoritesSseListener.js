/**
 * Created by cmeyers on 7/29/16.
 */

import { SseBus } from './SseBus';

import fetch from 'isomorphic-fetch';
import * as sse from '@jenkins-cd/sse-gateway';

class FavoritesSseListener {
    initialize(listener, filter) {
        if (!this.sseBus) {
            this.sseBus = new SseBus(sse, fetch);
            this.sseBus.subscribeToJob(listener, filter);
        }
    }

    dispose() {
        if (this.sseBus) {
            this.sseBus.dispose();
            this.sseBus = null;
        }
    }
}

const instance = new FavoritesSseListener();

export default instance;

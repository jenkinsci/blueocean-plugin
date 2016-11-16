/**
 * Created by cmeyers on 7/8/16.
 */

import { blueocean } from './scopes';

export class User {

    constructor(blueUser = {}) {
        this._class = blueUser._class;
        this._links = blueUser._links;
        this.email = blueUser.email;
        this.fullName = blueUser.fullName;
        this.id = blueUser.id || 'anonymous';
    }

    isAnonymous() {
        return (this.id === 'anonymous');
    }

    static current() {
        return CURRENT; // eslint-disable-line no-use-before-define
    }
}

let CURRENT = new User(blueocean.user);

/* eslint-disable */
export const TestUtil = {
    setCurrent: function (user) {
        CURRENT = new User(user);
    },
};
/* eslint-enable */

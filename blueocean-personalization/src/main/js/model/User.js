/**
 * Created by cmeyers on 7/8/16.
 */
import Immutable from 'immutable';
import { AppConfig } from '@jenkins-cd/blueocean-core-js';

/* eslint new-cap: [0] */
const { Record } = Immutable;

export class User extends Record(
    {
        _class: null,
        _links: null,
        email: null,
        fullName: null,
        id: null,
    }
) {
    isAnonymous() {
        return false;
    }

    static current() {
        return new User(AppConfig.getInitialUser());
    }
}

export class AnonUser {
    isAnonymous() {
        return true;
    }
}

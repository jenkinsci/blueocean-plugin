/**
 * Created by cmeyers on 7/8/16.
 */
import Immutable from 'immutable';

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
}

export class AnonUser {
    isAnonymous() {
        return true;
    }
}

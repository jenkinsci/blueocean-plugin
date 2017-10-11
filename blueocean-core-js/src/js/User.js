/**
 * Created by cmeyers on 7/8/16.
 */

import { blueocean } from './scopes';

class PipelinePermissions {
    constructor(blueUser = {}) {
        if (blueUser.permission && blueUser.permission.pipeline) {
            this._permissions = blueUser.permission.pipeline;
        } else {
            this._permissions = {};
        }
    }

    create() {
        return !!this._permissions.create;
    }

    read() {
        return !!this._permissions.read;
    }

    start() {
        return !!this._permissions.start;
    }

    stop() {
        return !!this._permissions.stop;
    }

    configure() {
        return !!this._permissions.configure;
    }
}

class CredentialPermissions {
    constructor(blueUser = {}) {
        if (blueUser.permission && blueUser.permission.credential) {
            this._permissions = blueUser.permission.credential;
        } else {
            this._permissions = {};
        }
    }

    create() {
        return !!this._permissions.create;
    }

    view() {
        return !!this._permissions.view;
    }

    delete() {
        return !!this._permissions.delete;
    }

    update() {
        return !!this._permissions.update;
    }

    manageDomains() {
        return !!this._permissions.manageDomains;
    }
}

export class User {
    constructor(blueUser = {}) {
        this._blueUser = blueUser;
    }

    get _class() {
        return this._blueUser._class;
    }

    get _links() {
        return this._blueUser._links;
    }

    get email() {
        return this._blueUser.email;
    }

    get fullName() {
        return this._blueUser.fullName;
    }

    get id() {
        return this._blueUser.id || 'anonymous';
    }

    isAnonymous() {
        return this.id === 'anonymous';
    }

    static current() {
        return CURRENT; // eslint-disable-line no-use-before-define
    }

    get isAdministrator() {
        const permission = this._blueUser.permission || {};
        return !!permission.administrator;
    }

    get permissions() {
        return {
            pipeline: new PipelinePermissions(this._blueUser),
            credential: new CredentialPermissions(this._blueUser),
        };
    }
}

let CURRENT = new User(blueocean.user);

/* eslint-disable */
export const TestUtil = {
    setCurrent: function(user) {
        CURRENT = new User(user);
    },
};
/* eslint-enable */

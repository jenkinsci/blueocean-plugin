/**
 * Created by cmeyers on 9/16/16.
 */

import React from 'react';
import { assert } from 'chai';

import utils from '../../src/js/utils';
import { User } from '../../src/js/User';


xdescribe('User', () => {
    describe('permissions', () => {
        it('User has pipeline permissions', () => {
            const user = new User(utils.clone(require('./data/user-1.json')));
            assert.equal(user.id, 'blah');
            assert.isTrue(user.permissions.pipeline.create());
            assert.isTrue(user.permissions.pipeline.read());
            assert.isTrue(user.permissions.pipeline.configure());
            assert.isTrue(user.permissions.pipeline.start());
            assert.isTrue(user.permissions.pipeline.stop());
        });

        it('User has credential permissions', () => {
            const user = new User(utils.clone(require('./data/user-1.json')));
            assert.equal(user.id, 'blah');
            assert.isTrue(user.permissions.credential.create());
            assert.isTrue(user.permissions.credential.delete());
            assert.isTrue(user.permissions.credential.manageDomains());
            assert.isTrue(user.permissions.credential.view());
            assert.isTrue(user.permissions.credential.update());
        });
    });
});

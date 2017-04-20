/**
 * Created by cmeyers on 9/16/16.
 */

import React from 'react';
import { assert } from 'chai';

import utils from '../../src/js/utils';
import Security from '../../src/js/security';
import config from '../../src/js/config';
import { TestUtil as UserTestUtil } from '../../src/js/User';

const { permit } = Security;

describe('Security', () => {
    describe('permit', () => {
        let pipeline;

        beforeEach(() => {
            pipeline = utils.clone(require('./data/pipeline-1.json'));
        });

        it('returns true when user has permission', () => {
            assert.isFalse(permit(pipeline).read());
            assert.isTrue(permit(pipeline).create());
            assert.isTrue(permit(pipeline).start());
            assert.isTrue(permit(pipeline).stop());
        });

        it('returns false when user does not have permission', () => {
            const { permissions: perms } = pipeline;
            perms.create = perms.read = perms.start = perms.stop = false;

            assert.isFalse(permit(pipeline).read());
            assert.isFalse(permit(pipeline).create());
            assert.isFalse(permit(pipeline).start());
            assert.isFalse(permit(pipeline).stop());
        });
    });

    describe('isSecurityEnabled', () => {
        it('returns true when active', () => {
            config._setJenkinsConfig({
                security: {
                    enabled: true,
                },
            });
            assert.isTrue(Security.isSecurityEnabled());
        });
    });

    describe('isAnonymousUser', () => {
        it('returns true when anon', () => {
            UserTestUtil.setCurrent({
                id: 'anonymous',
            });
            assert.isTrue(Security.isAnonymousUser());
        });

        it('returns false when identified', () => {
            UserTestUtil.setCurrent({
                id: 'admin',
            });
            assert.isFalse(Security.isAnonymousUser());
        });
    });
});

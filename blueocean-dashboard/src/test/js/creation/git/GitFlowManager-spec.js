import { assert } from 'chai';
import { Promise } from 'es6-promise';

import GitFlowManager from '../../../../main/js/creation/git/GitFlowManager';

import { mockExtensionsForI18n } from '../../mock-extensions-i18n';
mockExtensionsForI18n();


describe('GitFlowManager', () => {
    let manager;

    beforeEach(() => {
        manager = new GitFlowManager(
            // mock out the required api objects & methods
            {},
            { listAllCredentials: () => new Promise(() => {}) },
        );
        manager.initialize();
    });

    describe('_createNameFromRepoUrl', () => {
        it('should use last part of repository URL', () => {
            const url = 'https://github.com:axltorvalds/annoying';
            const name = manager._createNameFromRepoUrl(url);

            assert.equal(name, 'annoying');
        });

        it('should discard the extension', () => {
            const url = 'git@github.com:axltorvalds/annoying.git';
            const name = manager._createNameFromRepoUrl(url);

            assert.equal(name, 'annoying');
        });
    });
});

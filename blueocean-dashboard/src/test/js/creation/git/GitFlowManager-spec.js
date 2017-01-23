import { assert } from 'chai';

import GitFlowManager from '../../../../main/js/creation/git/GitFlowManager';

describe('GitFlowManager', () => {
    let manager;

    beforeEach(() => {
        manager = new GitFlowManager();
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

import { assert } from 'chai';

import { mockExtensionsForI18n } from '../../mock-extensions-i18n';
import { GitPWCredentialsManager, ManagerState } from '../../../../main/js/credentials/git/GitPWCredentialsManager';
import { TypedError } from '../../../../main/js/credentials/TypedError';

import { LoadError, SaveError } from '../../../../main/js/credentials/bitbucket/BbCredentialsApi';

mockExtensionsForI18n();

// Using jest expect - https://facebook.github.io/jest/docs/en/expect.html

describe('GitPWCredentialsManager', () => {

    const repoUrl = 'https://example.org/git/project.git';

    let manager;
    let apiMock;

    beforeEach(() => {
        apiMock = new GitPWCredentialsApiMock();
        manager = new GitPWCredentialsManager(apiMock);
        manager.configure(repoUrl, 'master');
    });

    describe('findExistingCredential', () => {

        it('behaves when not found', () => {
            expect.assertions(5);

            expect(manager.state).toBe(ManagerState.PENDING_LOADING_CREDS);
            expect(manager.existingCredential).not.toBeDefined();

            return manager.findExistingCredential()
                .then(credential => {
                    expect(manager.state).toBe(ManagerState.NEW_REQUIRED);
                    expect(manager.existingCredential).not.toBeDefined();
                    expect(credential).not.toBeDefined();
                });
        });

        it('behaves when found', () => {
            apiMock.findExistingCredentialShouldSucceed = true;

            expect.assertions(7);
            expect(manager.state).toBe(ManagerState.PENDING_LOADING_CREDS);
            expect(manager.existingCredential).not.toBeDefined();

            return manager.findExistingCredential()
                .then(credential => {
                    expect(manager.state).toBe(ManagerState.EXISTING_FOUND);
                    expect(credential).toBeDefined();
                    expect(credential.credentialId).toBe(apiMock.credentialId);
                    expect(manager.existingCredential).toBeDefined();
                    expect(manager.existingCredential.credentialId).toBe(apiMock.credentialId);
                });
        });
    });

    describe('createCredential', () => {

        it('updates state when create succeeds', () => {
            apiMock.findExistingCredentialShouldSucceed = true;

            expect.assertions(10);

            expect(manager.state).toBe(ManagerState.PENDING_LOADING_CREDS);
            expect(manager.existingCredential).not.toBeDefined();

            const promise = manager.createCredential('userNameX', 'passwordX');

            expect(manager.state).toBe(ManagerState.PENDING_VALIDATION);
            expect(manager.existingCredential).not.toBeDefined();

            return promise.then(credential => {
                expect(manager.state).toBe(ManagerState.SAVE_SUCCESS);
                expect(credential).toBeDefined();
                expect(credential.credentialId).toBe(apiMock.credentialId);
                expect(manager.existingCredential).toBeDefined();
                expect(manager.existingCredential.credentialId).toBe(apiMock.credentialId);
                expect(apiMock.capturedCreateParams).toMatchObject({
                    repositoryUrl: repoUrl,
                    userName: 'userNameX',
                    password: 'passwordX',
                    branchName: 'master',
                    requirePush: undefined,
                });
            });
        });

        it('passes requirePush', () => {
            apiMock.findExistingCredentialShouldSucceed = true;

            expect.assertions(6);

            expect(manager.state).toBe(ManagerState.PENDING_LOADING_CREDS);

            const promise = manager.createCredential('userNameX', 'passwordX', true);

            expect(manager.state).toBe(ManagerState.PENDING_VALIDATION);

            return promise.then(credential => {
                expect(manager.state).toBe(ManagerState.SAVE_SUCCESS);
                expect(credential).toBeDefined();
                expect(credential.credentialId).toBe(apiMock.credentialId);
                expect(apiMock.capturedCreateParams).toMatchObject({
                    repositoryUrl: repoUrl,
                    userName: 'userNameX',
                    password: 'passwordX',
                    branchName: 'master',
                    requirePush: true,
                });
            });
        });

        it('clears existing while creating', () => {
            apiMock.createCredentialShouldSucceed = true;
            apiMock.findExistingCredentialShouldSucceed = true;

            expect.assertions(13);

            return manager.findExistingCredential()
                .then(credential => {
                    expect(manager.state).toBe(ManagerState.EXISTING_FOUND);
                    expect(credential).toBeDefined();
                    expect(credential.credentialId).toBe(apiMock.credentialId);
                    expect(manager.existingCredential).toBeDefined();
                    expect(manager.existingCredential.credentialId).toBe(apiMock.credentialId);

                    apiMock.credentialId = "newId";

                    const nextPromise = manager.createCredential('userNameX', 'passwordX', true);

                    expect(manager.state).toBe(ManagerState.PENDING_VALIDATION);
                    expect(manager.existingCredential).not.toBeDefined();

                    return nextPromise;
                })
                .then(credential => {
                    expect(manager.state).toBe(ManagerState.SAVE_SUCCESS);
                    expect(credential).toBeDefined();
                    expect(credential.credentialId).toBe(apiMock.credentialId);
                    expect(manager.existingCredential).toBeDefined();
                    expect(manager.existingCredential.credentialId).toBe(apiMock.credentialId);
                    expect(apiMock.capturedCreateParams).toMatchObject({
                        repositoryUrl: repoUrl,
                        userName: 'userNameX',
                        password: 'passwordX',
                        branchName: 'master',
                        requirePush: true,
                    });
                });
        });

        it('updates state when create fails with not found', () => {
            apiMock.createCredentialShouldSucceed = false;

            expect.assertions(7);

            expect(manager.state).toBe(ManagerState.PENDING_LOADING_CREDS);

            const promise = manager.createCredential('userNameX', 'passwordX', true);

            expect(manager.state).toBe(ManagerState.PENDING_VALIDATION);

            return promise.catch(error => {
                expect(error).toBeDefined();
                expect(error).toBeInstanceOf(TypedError);
                expect(manager.state).toBe(ManagerState.INVALID_CREDENTIAL);
                expect(manager.existingCredential).not.toBeDefined();
                expect(apiMock.capturedCreateParams).toMatchObject({
                    repositoryUrl: repoUrl,
                    userName: 'userNameX',
                    password: 'passwordX',
                    branchName: 'master',
                    requirePush: true,
                });
            });
        });

        it('updates state when create fails with mystery meat', () => {
            apiMock.createCredentialShouldSucceed = false;
            apiMock.createCredentialShouldHaveWeirdFailure = true;

            expect.assertions(7);

            expect(manager.state).toBe(ManagerState.PENDING_LOADING_CREDS);

            const promise = manager.createCredential('userNameX', 'passwordX', true);

            expect(manager.state).toBe(ManagerState.PENDING_VALIDATION);

            return promise.catch(error => {
                expect(error).toBeDefined();
                expect(error).toBeInstanceOf(TypedError);
                expect(manager.state).toBe(ManagerState.UNEXPECTED_ERROR_CREDENTIAL);
                expect(manager.existingCredential).not.toBeDefined();
                expect(apiMock.capturedCreateParams).toMatchObject({
                    repositoryUrl: repoUrl,
                    userName: 'userNameX',
                    password: 'passwordX',
                    branchName: 'master',
                    requirePush: true,
                });
            });
        });
    });

});

// Helpers

function later(promiseResolver) {
    return new Promise((resolve, reject) => {
        process.nextTick(() => {
            try {
                resolve(promiseResolver());
            }
            catch (err) {
                reject(err);
            }
        });
    });
}

class GitPWCredentialsApiMock /* FIXME: implements GitPWCredentialsApiPublic */ {

    findExistingCredentialShouldSucceed = false;
    createCredentialShouldSucceed = true;
    createCredentialShouldHaveWeirdFailure = false;

    credentialId = 'someCredentialId';

    capturedCreateParams = {};

    findExistingCredential(repositoryUrl) {
        return later(() => {
            if (this.findExistingCredentialShouldSucceed) {
                return {
                    credentialId: this.credentialId,
                };
            }
            throw new TypedError(LoadError.TOKEN_NOT_FOUND);
        });
    }

    createCredential(repositoryUrl, userName, password, branchName, requirePush) {
        this.capturedCreateParams = { repositoryUrl, userName, password, branchName, requirePush };

        return later(() => {
            if (this.createCredentialShouldSucceed) {
                return {};
            }
            if (this.createCredentialShouldHaveWeirdFailure) {
                throw new TypedError(SaveError.UNKNOWN_ERROR);
            }
            throw new TypedError(SaveError.INVALID_CREDENTIAL);
        });
    }
}

import {mockExtensionsForI18n} from '../../mock-extensions-i18n';

import PerforceFlowManager from "../../../../main/js/creation/perforce/PerforceFlowManager";
import Promise from "bluebird";
import {ListProjectsOutcome} from "../../../../main/js/creation/perforce/api/PerforceCreationApi";
import STATE from "../../../../main/js/creation/perforce/PerforceCreationState";

mockExtensionsForI18n();

describe('PerforceFlowManager', () => {
    let manager;
    let creationApi;

    beforeEach(() => {
        // mock out the required api objects & methods
        creationApi = new PerforceCreationApiMock();
        manager = new PerforceFlowManager(creationApi, {
            findExistingCredential: () => new Promise(() => {
            })
        },);
        manager.initialize();
    });

    describe('listProjects', () => {
        it('behaves', () => {
            manager.selectedCred = 'AnyCredId';
            creationApi.listProjectsShouldSucceed = true;

            expect.assertions(1);

            const exp_projects = manager.listProjects();
            expect(manager.projects).toBeDefined();
        });
    });

    describe('_listProjectsSuccess', () => {
        it('behaves when successful', () => {
            expect.assertions(5);

            const response = {
                outcome: ListProjectsOutcome.SUCCESS,
                projects: ["SwarmProject1", "SwarmProject2", "SwarmProject3"],
            };
            manager.changeState(STATE.STEP_CHOOSE_CREDENTIAL);
            manager._listProjectsSuccess(response);
            expect(manager.projects).toBeDefined();
            expect(manager.projects.length).toBe(3);
            expect(manager.projects[0]).toEqual("SwarmProject1");
            expect(manager.projects[1]).toEqual("SwarmProject2");
            expect(manager.projects[2]).toEqual("SwarmProject3");
        });

        it('behaves when invalid credentials', () => {
            expect.assertions(2);
            const response = {
                outcome: ListProjectsOutcome.INVALID_CREDENTIAL_ID,
                projects: [],
            };
            manager.changeState(STATE.STEP_CHOOSE_CREDENTIAL);
            manager._listProjectsSuccess(response);
            expect(manager.stateId).toBe(STATE.STEP_CHOOSE_CREDENTIAL);
            expect(manager.projects.length).toEqual(0);
        });

        it('behaves when unknown error', () => {
            expect.assertions(2);
            const response = {
                outcome: "Anything else",
                projects: [],
            };
            manager.changeState(STATE.STEP_CHOOSE_CREDENTIAL);
            manager._listProjectsSuccess(response);
            expect(manager.stateId).toBe(STATE.ERROR_UNKNOWN);
            expect(manager.projects.length).toEqual(0);
            console.log(manager.stepElement);
        });
    });

});

//Helpers
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

class PerforceCreationApiMock {
    listProjectsShouldSucceed = false;

    listProjects(credentialId) {
        return later(() => {
            if (this.listProjectsShouldSucceed) {
                return {
                    outcome: ListProjectsOutcome.SUCCESS,
                    projects: ["blueoceantest", "hip", "jenkins-system-tests", "p4eclipse", "p4java", "technical-publications", "testforblue"],
                };
            }

        });

    }
}

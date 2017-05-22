/**
 * @module localRepo
 * @memberof git
 * @description
 *
 * Tests: test that creating a pipeline from a local Git repo works
 */
const git = require("../../../../main/js/api/git");
const path = require("path");

const jobName = 'test-project-folder';
const pathToRepo = path.resolve('./target/' + jobName);
const sourceRep = './src/test/resources/multibranch_2';


module.exports = {
    before: !function(browser, done) {
        browser.waitForJobDeleted(jobName, function () {
            git.createRepo(sourceRep, pathToRepo)
                .then(function () {
                    git.createBranch('feature/alpha', pathToRepo)
                        .then(done);
                });
        });
    },

    'Step 01 - Create Pipeline': !function (browser) {
        const create = browser.page.bluePipelineCreate().navigate();
        create.waitForElementVisible('.scm-provider-list');
        create.click('@gitCreationButton');
        create.waitForElementVisible('.git-step-connect');
        create.setValue('@repositoryUrlText', pathToRepo);
        create.click('@newCredentialTypeSystemSSh');
        create.click('@createButton');
        create.assertCompleted();
    },
    'Step 02 - Check Activity Tab': !function (browser) {
        const activity = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        activity.assertBasicLayoutOkay();
        activity.waitForRunSuccessVisible(jobName, '1');
    }
};

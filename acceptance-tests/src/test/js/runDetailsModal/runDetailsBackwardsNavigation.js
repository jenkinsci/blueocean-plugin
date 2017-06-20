const path = require("path");
const jobName = 'runDetailsBackwardNavigation';
const pathToRepo = path.resolve('./target/' + jobName);
const soureRep = './src/test/resources/multibranch_2';
const git = require("../../../main/js/api/git");

var activity, branches, runDetails;

/**
 * @module runDetailsBackwardNavigation
 * @memberof runDetailsModal
 * @description
 *
 * Tests: test whether navigating within run details and closing it returns to the correct page.
 *
 * REGRESSION covered: JENKINS-43217
 *
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-43217|JENKINS-43217}
 * Closing pipeline results "modal" after clicking on a stage sets the url incorrectly to the previous URL, not activity
 */
module.exports = {
    before: function(browser, done) {
        // we creating a git repo in target based on the src repo (see above)
        git.createRepo(soureRep, pathToRepo)
            .then(function () {
                git.createBranch('feature/1', pathToRepo)
                    .then(done);
            });
    },
    'Step 01 - Create Multibranch Job': function (browser) {
        var multibranchCreate = browser.page.multibranchCreate().navigate();
        multibranchCreate.createBranch(jobName, pathToRepo);
    },
    'Step 02 - navigate to Activity tab via URL': function (browser) {
        activity = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        activity.waitForElementVisible('.Header-pageTabs .branches');
    },
    'Step 03 - click to Branches tab': function(browser) {
        activity.click('.Header-pageTabs .branches');
        branches = browser.page.bluePipelineBranch();
        branches.waitForElementVisible('.JTable-row[data-branch="master"]');
    },
    'Step 04 - click to Run Details': function(browser) {
        branches.click('.JTable-row[data-branch="master"]');
        runDetails = browser.page.bluePipelineRunDetail();
        runDetails.waitForElementVisible('.RunDetailsHeader');
    },
    'Step 05 - click around Run Details tabs': function() {
        runDetails.clickTab('changes');
        runDetails.waitForLocationContains('/changes');
        runDetails.clickTab('artifacts');
        runDetails.waitForLocationContains('/artifacts');
        runDetails.clickTab('tests');
        runDetails.waitForLocationContains('/tests');
    },
    'Step 06 - close modal and confirm returned to Branches tab': function () {
        runDetails.closeModal();
        runDetails.waitForLocationContains('/branches');
    }
};

/** @module folder
 * @memberof multibranch
 * @description
 *
 * Tests: Tests specific to MBP in folders
 *
 * REGRESSION covered:
 *
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-39842|JENKINS-39842} - Open Blue Ocean button should not try to load /activity for a folder
 *
 */
const git = require("../../../main/js/api/git");
const path = require("path");
const pageHelper = require("../../../main/js/util/pageHelper");
const sanityCheck = pageHelper.sanityCheck;

// base configuration for the path of the folders
const projectFolderPath = ['aFolder', 'bFolder', 'cFolder'];
//  our job should be named the same way in both folders
const jobName = 'MBPInFolderTree';
// git repo details
const pathToRepo = path.resolve('./target/test-project-folder');
const soureRep = './src/test/resources/multibranch_1';

module.exports = {
    /**
     * creating a git repo
     */
    before: function (browser, done) {
        browser.waitForJobDeleted('aFolder', function () {
            // we creating a git repo in target based on the src repo (see above)
            git.createRepo(soureRep, pathToRepo)
                .then(function () {
                    git.createBranch('feature/1', pathToRepo)
                        .then(done);
                });
        });
    },
    /**
     * Create folder structure - "aFolder/bFolder/cFolder"
     */
    'step 01 - create folder': function (browser) {
        // Initial folder create page
        const folderCreate = browser.page.folderCreate().navigate();
        // create nested folder for the project
        folderCreate.createFolders(projectFolderPath);
    },
    /**
     * Create multibranch job - "MBPInFolderTree"
     */
    'step 02 - create multibranch job': function (browser) {
        // go to the newItem page
        browser.page.jobUtils().newItem();
        // and then use the multibranchCreate page object to create
        // a multibranch project
        browser.page.multibranchCreate().createBranch(jobName, pathToRepo);
    },
    /**
     * test open blueocean from classic - run details
     * @param browser
     */
    'step 03 - Open Blue Ocean (from a run details)': function(browser) {
        var classicRunPage = browser.page.classicRun();

        classicRunPage.navigateToRun('aFolder/job/bFolder/job/cFolder/job/MBPInFolderTree/job/master');

        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to the run details page for the first run.
        browser.openBlueOcean();
        browser.url(function (response) {
           sanityCheck(browser, response);
           response.value.endsWith('/blue/organizations/jenkins/aFolder%2FbFolder%2FcFolder%2FMBPInFolderTree/branches/');

            // Make sure the page has all the bits and bobs
            // See JENKINS-40137
            const blueRunDetailPage = browser.page.bluePipelineRunDetail();
            blueRunDetailPage.assertBasicLayoutOkay();
        });
    },
    /**
     * test escape to classic Jenkins from blueocean pipeline page.
     * @param browser
     */
    'step 04 - escape to classic Jenkins from pipeline page': function(browser) {
        const blueMultiBranchPipeline = browser.page.blueMultiBranchPipeline();
        blueMultiBranchPipeline.forPipeline('aFolder/bFolder/cFolder/MBPInFolderTree');
        browser.url(function (response) {
            response.value.endsWith('/blue/organizations/jenkins/aFolder%2FbFolder%2FcFolder%2FMBPInFolderTree/activity');
            blueMultiBranchPipeline.assertBasicLayoutOkay();

            blueMultiBranchPipeline.click('@exitToClassicWidget');
            browser.waitForElementVisible('#open-blueocean-in-context');
            browser.url(function (response) {
                response.value.endsWith('/aFolder/job/bFolder/job/cFolder/job/MBPInFolderTree/');
            });
        });
    },
    /**
     * test escape to classic Jenkins from blueocean pipeline run page.
     * @param browser
     */
    'step 05 - escape to classic Jenkins from pipeline run page': function(browser) {
        const bluePipelineRunDetail = browser.page.bluePipelineRunDetail();
        bluePipelineRunDetail.navigate(bluePipelineRunDetail.api.launchUrl + '/blue/organizations/jenkins/aFolder%2FbFolder%2FcFolder%2FMBPInFolderTree/detail/feature%2F1/1');
        browser.url(function (response) {
            response.value.endsWith('/blue/organizations/jenkins/aFolder%2FbFolder%2FcFolder%2FMBPInFolderTree/detail/feature%2F1/1/pipeline');
            bluePipelineRunDetail.assertBasicLayoutOkay();

            bluePipelineRunDetail.click('@exitToClassicWidget');
            browser.waitForElementVisible('#open-blueocean-in-context');
            browser.url(function (response) {
                response.value.endsWith('/job/aFolder/job/bFolder/job/cFolder/job/MBPInFolderTree/job/feature%2F1/1/');
            });
        });
    },
};

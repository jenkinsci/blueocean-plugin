/** @module folder
 * @memberof edgeCases
 * @description
 *
 * Tests: Condensed state of commit author in detail header
 *
 * REGRESSION covered:
 *
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36618|JENKINS-36618} - create same job but in another folder
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36616|JENKINS-36616} - Unable to load multibranch projects in a folder
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36773|JENKINS-36773} / {@link https://issues.jenkins-ci.org/browse/JENKINS-37605|JENKINS-37605} verify encoding and spacing of details
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36613|JENKINS-36613} Unable to load steps for multibranch pipelines with / in them
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36674|JENKINS-36674} Tests are not being reported
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36615|JENKINS-36615} the multibranch project has the branch 'feature/1'
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-39842|JENKINS-39842} - Open Blue Ocean button should not try to load /activity for a folder
 *
 */
const git = require("../../../main/js/api/git");
const path = require("path");
const pageHelper = require("../../../main/js/util/pageHelper");
const sanityCheck = pageHelper.sanityCheck;

// base configuration for the path of the folders
const folders = ['firstFolder', '三百', 'ñba', '七'];
const anotherFolders = ['anotherFolder', '三百', 'ñba', '七'];
//  our job should be named the same way in both folders
const jobName = 'Sohn';
// git repo details
const pathToRepo = path.resolve('./target/test-project-folder');
const soureRep = './src/test/resources/multibranch_1';
// helper to return the project name including a seperator or '/'
function getProjectName(nameArray, seperator) {
    if (!seperator) {
        seperator = '/';
    }
    return nameArray.join(seperator) + seperator + jobName;
}
// here we need to escape the real projectName to a urlEncoded string
const projectName = getProjectName(anotherFolders, '%2F');

module.exports = {
    /** creating a git repo */
    before: function (browser, done) {
        browser.waitForJobDeleted('firstFolder', function () {
            browser.waitForJobDeleted('anotherFolder', function () {
                // we creating a git repo in target based on the src repo (see above)
                git.createRepo(soureRep, pathToRepo)
                    .then(function () {
                        git.createBranch('feature/1', pathToRepo)
                            .then(done);
                    });
            });
        });
    },
    /** Create folder - "firstFolder"*/
    'step 01': function (browser) {
       // Initial folder create page
       const folderCreate = browser.page.folderCreate().navigate();
       // create nested folder for the project
       folderCreate.createFolders(folders);
    },
    /** Create freestyle job - "Sohn"*/
    'step 02': function (browser) {
        // create the freestyle job in the folder
        browser.page.jobUtils().newItem();
        const freestyleCreate = browser.page.freestyleCreate();
        freestyleCreate.createFreestyle(jobName, 'freestyle.sh');

        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to an empty pipeline activity
        // page.
        var bluePipelineActivity = browser.page.bluePipelineActivity();
        browser.openBlueOcean();
        bluePipelineActivity.assertEmptyLayoutOkay(jobName);
        browser.assert.urlEndsWith('/blue/organizations/jenkins/firstFolder%2F三百%2Fñba%2F七%2FSohn/activity');
    },
    /** Create folder - "anotherFolder"
     *
     * @see  {@link https://issues.jenkins-ci.org/browse/JENKINS-36618|JENKINS-36618} part 1 - create same job but in another folder
     */
    'step 03': function (browser) {
        // Initial folder create page
        const folderCreate = browser.page.folderCreate().navigate();
        // create nested folder for the project
        folderCreate.createFolders(anotherFolders);
    },
    /** Create multibranch job - "Sohn"
     *
     * @see  {@link https://issues.jenkins-ci.org/browse/JENKINS-36618|JENKINS-36618} part 1 - create same job but in another folder
     */
    'step 04': function (browser) {
        // go to the newItem page
        browser.page.jobUtils().newItem();
        // and then use the multibranchCreate page object to create
        // a multibranch project
        browser.page.multibranchCreate().createBranch(jobName, pathToRepo);
    },
    /** Jobs can have the same name in different folders, they should show up in the gui
    *
    * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36618|JENKINS-36618} part 2 - verify
    */
    'step 05': function (browser) {
       const bluePipelinesPage = browser.page.bluePipelines().navigate();
       // simply validate that the pipline listing is showing the basic things
       bluePipelinesPage.assertBasicLayoutOkay();
       // by now we should have 2 different jobs from prior steps
       bluePipelinesPage.countJobToBeEqual(browser, jobName, 2);
    },
    /** Build freestyle job */
    'step 06': function (browser) {
       const freestyleJob = browser.page.jobUtils()
           .forJob(getProjectName(folders));
       // start a build on the nested freestyle project
       freestyleJob.buildStarted(function () {
           // Reload the job page and check that there was a build done.
           freestyleJob
               .forRun(1)
               .waitForElementVisible('@executer');
       });
       // See whether we have changed the url
       browser.url(function (response) {
           sanityCheck(browser, response);
           // if we have changed the url then we should have now firstFolder in the path
           browser.assert.equal(response.value.indexOf('firstFolder') > -1, true);
       })
    },
    /** Validate correct encoding, pipeline graph and steps */
    'step 07': function (browser) {
       // /JENKINS-36616 - Unable to load multibranch projects in a folder
       const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(projectName, 'jenkins', 'feature%2F1', 1);
       // {@link https://issues.jenkins-ci.org/browse/JENKINS-36773|JENKINS-36773} / JENKINS-37605 verify encoding and spacing of details
       blueRunDetailPage.assertTitle('feature/1');
       // JENKINS-36613 Unable to load steps for multibranch pipelines with / in them
       // FIXME should show the graph but it is failing because underlying 500 -> is under inverstigation currently
      // blueRunDetailPage.validateGraph(); // test whether we have a pipeline graph
      // blueRunDetailPage.validateSteps(); // validate that steps are displayed
       // There should be no authors
       blueRunDetailPage.authorsIsNotSet();
       // FIXME JENKINS-36619 -> somehow the close in AT is not working
       blueRunDetailPage.closeModal();
    },
    /** Check whether the artifacts tab shows artifacts*/
    'step 08': function (browser) {
       const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(projectName, 'jenkins', 'feature%2F1', 1);
       // make sure we are finished before doing the next validations
       blueRunDetailPage.waitForJobRunEnded(getProjectName(anotherFolders) + '/feature%2F1');
       // go to the artifact page by clicking the tab
       blueRunDetailPage.clickTab('artifacts', function (result) {
           sanityCheck(result);
           // we have added 2 files as artifact
           blueRunDetailPage.validateNotEmptyArtifacts(2); // -> ATH is failing but local is not
       });
    },
    /** Check whether the test tab shows failing tests
    *
    * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36674|JENKINS-36674} Tests are not being reported
    */
    'step 09': function (browser) {
       const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(projectName, 'jenkins', 'feature%2F1', 1);
       // Go to the test page by clicking the tab
       blueRunDetailPage.clickTab('tests', function (result) {
           sanityCheck(result);
           // There should be failing tests
           blueRunDetailPage.validateFailingTests(); // -> ATH is failing but local is not
       });
    },
    /** Jobs can be started from branch tab. - RUN
    *
    * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-36615|JENKINS-36615} the multibranch project has the branch 'feature/1'
    */
    'step 10': function (browser) {
       // first get the activity screen for the project
       const blueActivityPage = browser.page.bluePipelineActivity().forJob(projectName, 'jenkins');
       // validate that we have 3 activities from the previous tests
       blueActivityPage.assertActivitiesToBeEqual(2); // -> FIXME previous test had been deactivated
       // change to the branch page, clicking on the tab
       blueActivityPage.clickTab('branches');
       // click on the first matching run button (small one)
       browser.page.bluePipelineBranch().clickRunButton();
       // go to the detail page
       const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(projectName, 'jenkins', 'feature%2F1', 2);
       // Wait for the job to end
       blueRunDetailPage.waitForJobRunEnded(getProjectName(anotherFolders) + '/feature%2F1');
    },
    /**
     * test open blueocean from classic - run details
     * @param browser
     */
    'step 11': function(browser) {
        var classicRunPage = browser.page.classicRun();

        classicRunPage.navigateToRun('anotherFolder/job/三百/job/ñba/job/七/job/Sohn/job/feature%252F1');

        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to the run details page for the first run.
        browser.openBlueOcean();
        browser.url(function (response) {
           sanityCheck(browser, response);
           response.value.endsWith('/blue/organizations/jenkins/anotherFolder%2F三百%2Fñba%2F七%2FSohn/detail/feature%2F1/1/pipeline');

            // Make sure the page has all the bits and bobs
            // See JENKINS-40137
            const blueRunDetailPage = browser.page.bluePipelineRunDetail();
            blueRunDetailPage.assertBasicLayoutOkay();
        });
    },
    /**
     * test open blueocean from classic - a normal folder page in classic jenkins.
     * <p>
     * It should send the user to the top level blue ocean page (pipelines).
     * @param browser
     */
    'step 12': function(browser) {
        var classicGeneral = browser.page.classicGeneral();

        // Go to a folder along the path to the MBP, but one
        // of the parent folders i.e. not the MBP project folder.
        classicGeneral.navigateToRun('job/anotherFolder/job/三百/job/ñba');

        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to the main top-level pipelines page.
        // See https://issues.jenkins-ci.org/browse/JENKINS-39842
        browser.openBlueOcean();
        browser.url(function (response) {
            sanityCheck(browser, response);
            response.value.endsWith('/blue/pipelines');

            // Make sure the page has all the bits and bobs
            // See JENKINS-40137
            const bluePipelines = browser.page.bluePipelines();
            bluePipelines.assertBasicLayoutOkay();
        });
    },
};

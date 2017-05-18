/** @module authorCollapsed
 * @memberof edgeCases
 * @description
 *
 * FIXME WIP
 * Tests: Condensed state of commit author in detail header
 *
 */
const git = require("../../../main/js/api/git");
const path = require("path");
const fse = require('fs-extra');
const pageHelper = require("../../../main/js/util/pageHelper");
const sanityCheck = pageHelper.sanityCheck;

const folder = ['committer', '三百', 'ñba', '七'];
//  our job should be named the same way in both folders
const jobName = 'Sohn';
// git repo details
const pathToRepo = path.resolve('./target/test-project-folder1');
const soureRep = './src/test/resources/multibranch_1';
// helper to return the project name including a seperator or '/'
function getProjectName(nameArray, seperator) {
    if (!seperator) {
        seperator = '/';
    }
    return nameArray.join(seperator) + seperator + jobName;
}
// here we need to escape the real projectName to a urlEncoded string
const projectName = getProjectName(folder, '%2F');

module.exports = {
    /** creating a git repo */
    before: !function (browser, done) {
        browser.waitForJobDeleted('committer', function () {
            // we creating a git repo in target based on the src repo (see above)
            git.createRepo(soureRep, pathToRepo)
                .then(function () {
                    git.createBranch('feature/1', pathToRepo)
                        .then(done);
                });
        });
    },
    beforeEach: !function (browser, done) {

    },
    /** Create folder - "committer"
     *
     * @see  {@link https://issues.jenkins-ci.org/browse/JENKINS-36618|JENKINS-36618} part 1 - create same job but in another folder
     */
    'step 01': !function (browser) {
        // Initial folder create page
        const folderCreate = browser.page.folderCreate().navigate();
        // create nested folder for the project
        folderCreate.createFolders(folder);
    },
    /** Create multibranch job - "Sohn"
     *
     * @see  {@link https://issues.jenkins-ci.org/browse/JENKINS-36618|JENKINS-36618} part 1 - create same job but in another folder
     */
    'step 02': !function (browser) {
        // go to the newItem page
        browser.page.jobUtils().newItem();
        // and then use the multibranchCreate page object to create
        // a multibranch project
        browser.page.multibranchCreate().createBranch(jobName, pathToRepo);
        // /JENKINS-36616 - Unable to load multibranch projects in a folder
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(projectName, 'jenkins', 'feature%2F1', 1);
        // There should be no authors
        blueRunDetailPage.authorsIsNotSet();
    },
    // FIXME: this and the test 12 is failing because the test is not closing probably. DEACTIVATED FOR NOW
    /** Check whether the changes tab shows changes - one commit*/
    'step 10': !function (browser) {
       // magic number
       const magic = 1;
       // creating an array
       const committs = Array.from(new Array(magic), function (x, i) {
           return i;
       });
       // now we have to index the branch, it is important that we create the page out of the asyncSeries
       const masterJob = browser.page.jobUtils()
           .forJob(getProjectName(folder), '/indexing');
       var recordedCommits = 0;
       // creating commits from that array with a mapSeries -> not parallel
       async.mapSeries(committs, function (file, callback) {
           const filename = file + '.txt';
           // writeFile is async so we need to use callback
           fse.writeFile(path.join(pathToRepo, filename), file, function (err) {
               // when we get an error we call with error
               if (err) {
                   callback(err);
               }
               // createCommit returns a promise just passing it alone
               return git.createCommit(pathToRepo, [filename])
                   .then(function (commitId) {
                       // if we reached here we have a commit
                       console.log('commitId', commitId);
                       /* We are sure that all async functions have finished.
                        * Now we let async know about it by
                        * callback without error and the commitId
                        */
                       callback(null, commitId);
                   })
           });

       }, function(err, results) {
           // results is an array of names
           console.log('Now starting the indexing', results.length, 'commits recorded');
           // start a new build by starting indexing
           masterJob.indexingStarted();
           // test whether we have commit
           const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(projectName, 'jenkins', 'master', 2);
           // click on the changes tab
           blueRunDetailPage.clickTab('changes');
           // we should have one commits now
           blueRunDetailPage.validateNotEmptyChanges();
           // the author title should be shown
           blueRunDetailPage.authorsIsNotCondensed();
           // Wait for the job to end
           blueRunDetailPage.waitForJobRunEnded(getProjectName(folder) + '/master', function (event) {
               console.log(event, 'ending the misery')
               browser.end();
           });
       });
    },
    // FIXME: this and the test 12 is failing because the test is not closing probably. DEACTIVATED FOR NOW
    /** Check whether the changes tab shows changes - condensed*/
    'step 12': !function (browser) {
       // magic number of how many commits we want to create
       const magic = 15;
       // creating an array
       const committs = Array.from(new Array(magic), function (x, i) {
           return i;
       });
       // now we have to index the branch, it is important that we create the page out of the asyncSeries
       const masterJob = browser.page.jobUtils()
           .forJob(getProjectName(folder), '/indexing');
       // creating commits from that array with a mapSeries -> not parallel
       async.mapSeries(committs, function (file, callback) {
           const filename = file + '.txt';
           // writeFile is async so we need to use callback
           fse.writeFile(path.join(pathToRepo, filename), file, function (err) {
               // when we get an error we call with error
               if (err) {
                   callback(err);
               }
               // createCommit returns a promise just passing it alone
               return git.createCommit(pathToRepo, [filename])
                   .then(function (commitId) {
                       // if we reached here we have a commit
                       console.log('commitId', commitId);
                       /* We are sure that all async functions have finished.
                        * Now we let async know about it by
                        * callback without error and the commitId
                        */
                       callback(null, commitId);
                   })
           });

       }, function(err, results) {
           // results is an array of names
           console.log('Now starting the indexing', results.length, 'commits recorded')
           // start a new build by starting indexing
           masterJob.indexingStarted();
           // test whether we have commit
           const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(projectName, 'jenkins', 'master', 2); // -> FIXME previous test had been deactivated
           // click on the changes tab
           blueRunDetailPage.clickTab('changes');
           // we should have a couple of commits now
           blueRunDetailPage.validateNotEmptyChanges();
           // make sure the windows is small
           browser.resizeWindow(1000, 600);
           // test now whether the authors are not listed but condendes
           blueRunDetailPage.authorsIsCondensed();
           // make the browser big again
           browser.resizeWindow(1680, 1050);
           // Wait for the job to end
           blueRunDetailPage.waitForJobRunEnded(getProjectName(folder) + '/master');
       });
    },
};

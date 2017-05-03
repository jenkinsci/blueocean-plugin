const git = require('../../main/js/api/git');
const path = require("path");
/** @module queued
 * @memberof testcases
 * @description TEST: basic tests around the queued state.
 */
const pathToRepo = path.resolve('./target/test-project');
const multiBranchJob = 'multiBranch';
const jobNameFreestyle = 'Eleven';
console.log('*** ' + pathToRepo);

module.exports = {
    //
    // TODO: Fix createBranch and re-inable these tests
    // The earlier create branch fails on multiple machines. The job gets created,
    // but the branch indexing is failing, causing the test to fail at step 05.
    // Strangely, it works on Thorstens machine. Tried upgrading git version. Tried on
    // MacOS and on Ubuntu 16.
    //
    // Looks to be the same issue as with edgeCases/folder.js. We had to disable a
    // number of steps there too.
    //
    //
    //before: function (browser, done) {
    //    git.createRepo('./src/test/resources/multibranch_1', pathToRepo)
    //        .then(function() {
    //            git.createBranch('feature/1', pathToRepo)
    //                .then(done);
    //        });
    //},
    //
    ///**
    // * Test queued jobs - disable executors
    // * @param browser
    // */
    //'Step 01': function (browser) {
    //    const configure = browser.page.computer().navigate();
    //    configure.setNumber(0);
    //    // now testing queued jobs
    //    // first starting a freestyle
    //},
    //
    ///**
    // * Create Multbranch Job
    // * @param browser
    // */
    //'Step 02': function (browser) {
    //    const branchCreate = browser.page.multibranchCreate().navigate();
    //    branchCreate.createBranch(multiBranchJob, pathToRepo); //-> if we start the job we have to wait for the finish
    //},
    //
    ///**
    // * Create freestyle job "Eleven"
    // * @param browser
    // */
    //'Step 03': function (browser) {
    //    const freestyleCreate = browser.page.freestyleCreate().navigate();
    //    freestyleCreate.createFreestyle(jobNameFreestyle, 'freestyle.sh');
    //},
    //
    ///**
    // * Queue for build freestyle job
    // * @param browser
    // */
    //'Step 04': function (browser) {
    //    const freestylePage = browser.page.jobUtils().forJob(jobNameFreestyle);
    //    freestylePage.buildQueued();
    //},
    //// FIXME: Disabled because of https://issues.jenkins-ci.org/browse/JENKINS-37843
    //// to enable remove ! before function
    ///**
    // * Validate queued state on freestyle job
    // */
    //'Step 05': !function (browser) {
    //    const blueRunDetailPage = browser.page.bluePipelineRunDetail()
    //        .forRun(jobNameFreestyle, 'jenkins', 1);
    //    blueRunDetailPage.validateQueued();
    //},
    //
    ////
    //// TODO: Remove once the following is disabled tests (below) are re-enabled..
    ////
    //after: function (browser) {
    //    const configure = browser.page.computer().navigate();
    //    // now let us reset the executors again
    //    configure.setNumber(2);
    //},
    //
    //
    ///**
    // * Validate queued state on multibranch job
    // * @param browser
    // */
    //'Step 06': function (browser) {
    //    const blueRunDetailPage = browser.page.bluePipelineRunDetail()
    //        .forRun(multiBranchJob, 'jenkins', 'feature%2F1', 1);
    //    blueRunDetailPage.validateQueued();
    //},
    //
    ///**
    // * Test queued jobs - enable executors
    // * @param browser
    // */
    //'Step 07': function (browser) {
    //    const configure = browser.page.computer().navigate();
    //    // now let us reset the executors again
    //    configure.setNumber(2);
    //},
    //
    ///**
    // * Validate graph on multibranch job
    // * @param browser
    // */
    //'Step 08': function (browser) {
    //    const blueRunDetailPage = browser.page.bluePipelineRunDetail()
    //        .forRun(multiBranchJob, 'jenkins', 'feature%2F1', 1);
    //    blueRunDetailPage.validateGraph();
    //},
    //// FIXME: Disabled because of https://issues.jenkins-ci.org/browse/JENKINS-37843
    //// to enable remove ! before function
    ///**
    // * Validate logConsole on freestyle job
    // */
    //'Step 09': !function (browser) {
    //    const blueRunDetailPage = browser.page.bluePipelineRunDetail()
    //        .forRun(jobNameFreestyle, 'jenkins', 1);
    //    blueRunDetailPage.validateLog();
    //    blueRunDetailPage.waitForJobRunEnded(jobNameFreestyle);
    //},
};
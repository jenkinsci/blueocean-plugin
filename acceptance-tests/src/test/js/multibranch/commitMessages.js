const tmp = require('tmp');
const async = require('async');

const repo = tmp.dirSync();
const pathToRepo = repo.name;
const jobName = 'commitMessages';
const path = require("path");
const soureRep = './src/test/resources/multibranch/commit_message';
const git = require("../../../main/js/api/git");

/** 
 * @module commitMessages
 * @memberof multibranch
 * @description Creates 2 commits and checks that the latest commit message is shown
 */
module.exports = {

    // ** creating a git repo */
    before: function (browser, done) {
          // we creating a git repo in target based on the src repo (see above)
          git.createRepo(soureRep, pathToRepo).then(done);
    },
    
    // Create the multibranch job
    'Create Job': function (browser) {
        var multibranchCreate = browser.page.multibranchCreate().navigate();      
        multibranchCreate.createBranch(jobName, pathToRepo);
    },

    'Open acitivty page wait for first run to finish': function(browser) {
        const blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName);
        // validate that we have 3 activities from the previous tests
        blueActivityPage.assertActivitiesToBeEqual(1);

        blueActivityPage.waitForRunSuccessVisible(jobName, '1');
    },

    'Create new commits and check activity and branches page for correct commit messages': (client) => {
        // perform async operation
        client.perform((browser, done) => {
            // run a sequence of async functions
            async.waterfall([
                // Sleep for 5 seconds to make sure that Jenkins picks up changes. 
                // TODO: figure out why this is needed given that the first run has completed.
                callback => { console.log("Waiting 5 seconds before adding files"); setTimeout(callback, 5000) },
                callback => git.createFile(pathToRepo, 'onefile', { message: 'onefile created' })
                    .then(commitOid => callback(null)),
                // Create commit and return the commitId
                callback => git.createFile(pathToRepo, 'somefile', { message: 'somefile created' })
                    .then(commitOid => callback(null, commitOid.tostrS()))
            ],(err, commitId) => {    
                const blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName);

                // Navigate to branches tab.
                blueActivityPage.clickTab('branches');
            
                // click the run button.
                blueActivityPage.waitForElementVisible('.actions a.run-button');
                blueActivityPage.click('.actions a.run-button');

                // wait for commit id and commit message to show on branches tab.
                browser.useXpath().waitForElementVisible(`//*[text()="${commitId.slice(0,7)}"]`);
                browser.useXpath().waitForElementVisible(`//*[text()="somefile created"]`);
                
                // Navigate back to activites page. 
                // TODO: blueActivityPage.clickTab('activity') doesnt work, why?
                const blueActivityPage2 = browser.page.bluePipelineActivity().forJob(jobName);
                
                // Should now be 2 runs.
                blueActivityPage2.assertActivitiesToBeEqual(2);
                blueActivityPage2.waitForRunSuccessVisible(jobName, '2');


                // Look for commit on 2nd run.
                browser.useXpath().waitForElementVisible(`//tr[@id="${jobName}-2"]/*/a/code[text()="${commitId.slice(0,7)}"]`);
                
                browser.useCss();
                blueActivityPage.assert.containsText('.RunMessageCellInner', 'somefile created');
                blueActivityPage.assert.containsText('.Lozenge', '2 commits');
             
                done();
            });
           
        }).end();
    },

}

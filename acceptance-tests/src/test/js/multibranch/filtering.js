const jobName = 'filterActivityMB';
const path = require("path");
const pathToRepo = path.resolve('./target/testfilter-project-folder');
const soureRep = './src/test/resources/multibranch_2';
const git = require("../../../main/js/api/git");

var activity;

/** @module filtering
 * @memberof multibranch
 * @description Check that can filter the activity list
 */
module.exports = {
    // ** creating a git repo */
    before: function (browser, done) {
          // we creating a git repo in target based on the src repo (see above)
          git.createRepo(soureRep, pathToRepo)
              .then(function () {
                  git.createBranch('feature/1', pathToRepo)
                      .then(done);
              });
    },
    'Step 1 - create multibranch job': function(browser) {
        var multibranchCreate = browser.page.multibranchCreate().navigate();
        multibranchCreate.createBranch(jobName, pathToRepo);
    },
    'Step 2 - view all runs on Activity tab': function (browser) {
        activity = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        activity.waitForElementVisible('@activityTable');
        browser.elements('css selector', '.activity-table .JTable-row[data-pipeline]', function(res) {
            browser.assert.equal(res.value.length, 2, 'Correct number of runs shown initially');
        });
    },
    'Step 3 - filter to branch with no runs': function(browser) {
        activity.waitForElementVisible('input.autocomplete');
        activity.click('input.autocomplete');
        activity.waitForElementVisible('.item[title=master]');
        activity.click('.item[title=master]');
        activity.waitForElementVisible('input.autocomplete[value=master');
        browser.elements('css selector', '.activity-table .JTable-row[data-pipeline]', function(res) {
            browser.assert.equal(res.value.length, 1, 'Correct number of runs filtered down');
        });
    }
};

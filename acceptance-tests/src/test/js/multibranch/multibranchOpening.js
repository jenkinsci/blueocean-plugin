const jobName = 'withGitFlow';
const path = require("path");
const pathToRepo = path.resolve('./target/test2-project-folder');
const soureRep = './src/test/resources/multibranch_2';
const git = require("../../../main/js/api/git");

function rowSelectorFor(jobName) {
    return `.JTable-row[data-pipeline='${jobName}']`;
}

/** @module multibranchOpening
 * @memberof multibranch
 * @description Check we run and open results screen for multibranch projects, 
 *              and that the stage graph shows and completes.
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

    /**
     * Make sure we can open the master branch results screen from activity
     */
    'open master branch from activity': function (browser) {

        var jobName = "masterActivityMB";
        var multibranchCreate = browser.page.multibranchCreate().navigate();
        multibranchCreate.createBranch(jobName, pathToRepo);

        var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');

        const rowSelector = rowSelectorFor(jobName);
        blueActivityPage.waitForElementVisible(rowSelector);
        blueActivityPage.click(rowSelector);

        blueActivityPage.assertStageGraphShows();
    },

    /**
     * Make sure we can open the master branch from branch screen
     */
    'open master branch from branches tab': function (browser) {

        var jobName = "masterBranchesMB";
        var multibranchCreate = browser.page.multibranchCreate().navigate();
        multibranchCreate.createBranch(jobName, pathToRepo);

         var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
         blueActivityPage.click(".branches");

         blueActivityPage.waitForElementVisible('.JTable-row[data-branch="master"]');
         blueActivityPage.click('.JTable-row[data-branch="master"]');

         blueActivityPage.assertStageGraphShows();

    },

    /**
     * Make sure we can open the feature/1 branch results screen from activity
     * Regression: https://issues.jenkins-ci.org/browse/JENKINS-40027
     */
    'open feature/1 branch from activity': function (browser) {

        var jobName = "featureActivityMB";
        var multibranchCreate = browser.page.multibranchCreate().navigate();
        multibranchCreate.createBranch(jobName, pathToRepo);

        var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');

        var rowSelector = rowSelectorFor(jobName);
        blueActivityPage.waitForElementVisible(rowSelector);
        blueActivityPage.click(rowSelector);

        blueActivityPage.assertStageGraphShows();

    },
    
    /**
     * Make sure we can open the feature/1 branch from branch screen
     * Regression: https://issues.jenkins-ci.org/browse/JENKINS-40027     
     */
    'open feature/1 from branches tab': function (browser) {
      
        var jobName = "featureBranchesMB";      
        var multibranchCreate = browser.page.multibranchCreate().navigate();      
        multibranchCreate.createBranch(jobName, pathToRepo);

      
         var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
         blueActivityPage.waitForElementVisible('.branches');
         blueActivityPage.click(".branches");

         const rowSelector = '.JTable-row[data-branch="feature/1"]';
         blueActivityPage.waitForElementVisible(rowSelector);
         blueActivityPage.click(rowSelector);

         blueActivityPage.assertStageGraphShows();
   
    }

}

/** @module freestyle
 * @memberof karaoke
 * @description TEST: Branch and PR tabs show the correct display.
 */
module.exports = {
/** Create freestyle Job "hijo"*/
    'Step 01': function (browser) {
        const freestyleCreate = browser.page.freestyleCreate().navigate();
        freestyleCreate.createFreestyle('nonmultibranch_freestyle', 'freestyle.sh');
    },
    'Step 02': function (browser) {
        const blueActivityPage = browser.page.bluePipelineActivity().forJob('nonmultibranch_freestyle', 'jenkins');
        
        blueActivityPage.clickTab('branches');
     
        blueActivityPage.assert.containsText('.PlaceholderContent.Unsupported', 'Branches are unsupported');       
    },

    'Step 03': function (browser) {
        const blueActivityPage = browser.page.bluePipelineActivity().forJob('nonmultibranch_freestyle', 'jenkins');
        
        blueActivityPage.clickTab('pr');
     
        blueActivityPage.assert.containsText('.PlaceholderContent.Unsupported', 'Pull request runs only work with the Multibranch Pipeline job type');       
    }
};

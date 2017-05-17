/** @module removePageHead
 * @memberof custom_commands
 * @description Nightwatch command to remove the breadcrumb bar on classic jenkins pages.
 * See http://nightwatchjs.org/guide#writing-custom-commands
 * <p/>
 * The breadcrumb bar in classic Jenkins is sticky positioned at the top
 * of the page and can block events getting to elements e.g. selecting
 * the job type on the create item page. This command removes it completely
 * by injecting some JS into the page.
 * */
const util = require('util');
const events = require('events');

function Cmd() {
    events.EventEmitter.call(this);
}
util.inherits(Cmd, events.EventEmitter);

Cmd.prototype.command = function () {

    this.api.execute(function() {
        (function () {
            function waitForJQuery() {
                try {
                    // In classic Jenkins, we can try dipping into the js-modules
                    // and get jQuery. If it's not there, then we're not in classic Jenkins
                    // and we don't care.
                    var $ = window.jenkinsCIGlobal.plugins["jquery-detached"].jquery2.exports;
                    doTweaks($);
                } catch(e) {
                    setTimeout(waitForJQuery, 50);
                }
            }
            function doTweaks($) {
                $(function() {
                    // Remove the page-head
                    $('#page-head').remove();

                    // Make the emission of the "complete" event (below) a bit
                    // more deterministic.
                    $('body').addClass('page-head-removed');
                });
            }
            waitForJQuery();
        }());
    });

    var self = this;
    setTimeout(function() {
        self.api.waitForElementPresent('body.page-head-removed', function() {
            self.emit('complete');
        });
    }, 10);

    return this;
};

module.exports = Cmd;
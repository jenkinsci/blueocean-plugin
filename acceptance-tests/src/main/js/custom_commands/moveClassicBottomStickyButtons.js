/**@module moveClassicBottomStickyButtons
 * @memberof custom_commands
 * @description Nightwatch command to move the config page buttons out of the way.
 * See http://nightwatchjs.org/guide#writing-custom-commands
 * <p/>
 * The config page save/apply buttons in classic Jenkins is sticky positioned at the bottom
 * of the page and can block events getting to elements e.g. selecting
 * build step dropdown.
 */

const util = require('util');
const events = require('events');

function Cmd() {
    events.EventEmitter.call(this);
}
util.inherits(Cmd, events.EventEmitter);

Cmd.prototype.command = function () {
    var self = this;

    this.api.execute(function() {
        (function () {
            function waitForJQuery() {
                try {
                    // In classic Jenkins, we can try dipping into the js-modules
                    // and get jQuery. If it's not there, then we're not in classic Jenkins
                    // and we don't care.
                    var $jquery = window.jenkinsCIGlobal.plugins["jquery-detached"].jquery2.exports;
                    doTweaks($jquery);
                } catch(e) {
                    setTimeout(waitForJQuery, 50);
                }
            }
            function doTweaks($jquery) {
                $jquery(function() {
                    function waitForElement(selector, callback) {
                        var theSelection = $jquery(selector);
                        if (theSelection.length > 0) {
                            callback(theSelection);
                        } else {
                            setTimeout(function() {
                                waitForElement(selector, callback)
                            }, 50);
                        }
                    }
                    function replaceStickFormButtons(formSelector, removeStickiesSelector, submitCallback) {
                        waitForElement(removeStickiesSelector, function (sticky) {
                            // Completely remove the sticky controls because they can
                            // play havoc with selenium tests. We tried moving etc,
                            // but in most cases doesn't work because of other
                            // listeners etc in jenkins that keep putting them back.
                            sticky.remove();

                            // In replacement of that, lets add a completely new button
                            // that we use to submit the config form.
                            var form = $jquery(formSelector);
                            var newSubmitButton = $jquery('<button id="newFormSubmitButtonForATH" type="submit">Form Submit Button For ATH</button>');

                            newSubmitButton.css({
                                'height': '30px',
                                'color': 'red'
                            });
                            newSubmitButton.click(function() {
                                submitCallback(form);
                            });
                            form.append(newSubmitButton);

                            // And now the page is ready from this pov.
                            markAsDone();
                        });
                    }
                    function markAsDone() {
                        // Make the emission of the "complete" event (below) a bit
                        // more deterministic.
                        $jquery('body').addClass('bottom-buttons-unstickied');
                    }

                    var pageUrl = window.location.href;

                    if (pageUrl.match(/\/newJob$/)) {
                        // The new item page
                        replaceStickFormButtons('form[name="createItem"]', '#add-item-panel .footer', function (form) {
                            form.submit();
                        });
                    } else if (pageUrl.match(/\/configure$/)) {
                        // A config page
                        replaceStickFormButtons('form[name="config"]', '#bottom-sticker', function (form) {
                            form.attr('path', '/Submit');
                            form.submit();
                        });
                    } else {
                        markAsDone();
                    }
                });
            }
            waitForJQuery();
        }());
    });

    setTimeout(function() {
        self.api.waitForElementPresent('body.bottom-buttons-unstickied', function() {
            self.emit('complete');
        });
    }, 10);

    return this;
};

module.exports = Cmd;
/**
 * @module addOpenBlueOceanLinkToFooter
 * @memberof custom_commands
 * @description Nightwatch command to add a link to Blue Ocean in the footer. Only relevant for classic Jenkins.
 * See http://nightwatchjs.org/guide#writing-custom-commands
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
                    var contextUrlDiv = $jquery('#blueocean-context-url');

                    if (contextUrlDiv.length === 1) {
                        var tryBlueOceanUrl;
                        var blueUrl = contextUrlDiv.attr('data-context-url');
                        var classicUrl = contextUrlDiv.attr('data-classic-url');

                        if (classicUrl) {
                            var indexOfContext = window.location.href.indexOf(classicUrl);
                            if (indexOfContext !== -1) {
                                tryBlueOceanUrl = window.location.href.substring(0, indexOfContext) + blueUrl;
                            }
                        }
                        if (!tryBlueOceanUrl) {
                            tryBlueOceanUrl = `./blue`;
                        }

                        var footer = $jquery('#footer');
                        var link = $jquery('<a>Open Blue Ocean</a>');
                        link.attr('id', 'open-blueocean-in-context');
                        link.attr('href', tryBlueOceanUrl);
                        footer.append(link);
                    }

                    // Make the emission of the "complete" event (below) a bit
                    // more deterministic.
                    $jquery('body').addClass('open-blueocean-link-added');
                });
            }
            waitForJQuery();
        }());
    });

    setTimeout(function() {
        self.api.waitForElementPresent('body.open-blueocean-link-added', function() {
            self.emit('complete');
        });
    }, 10);

    return this;
};

module.exports = Cmd;
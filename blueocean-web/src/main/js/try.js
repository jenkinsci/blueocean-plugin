var $ = require('jquery-detached').getJQuery();

function getAppUrl() {
    const rootUrl = $('head').attr('data-rooturl');
    if (!rootUrl) {
        return '';
    } else {
        return rootUrl;
    }
}

function aHref(tryBlueOceanUrl) {
    var tryBlueOcean = $('<a id="open-blueocean-in-context" class="try-blueocean header-callout">Open Blue Ocean</a>');
    tryBlueOcean.attr('href', tryBlueOceanUrl);
    return tryBlueOcean;
}

function configureJenkinsUrl() {
    var tryBlueOcean = $('<span id="open-blueocean-in-context" class="try-blueocean header-callout">Open Blue Ocean</span>');
    var tryBlueOceanClickHandler = function() {
        // Remove the click handler, preventing it from picking up clicks
        // on the the popup (below).
        tryBlueOcean.off();

        var popup = $(`<span class="unable-to-open"><h3>Unable to Open Blue Ocean</h3>The configured Jenkins URL does not match the current URL.<p/><a href="${getAppUrl()}/configure">Please configure the Jenkins URL</a></span>`);
        tryBlueOcean.append(popup);
        popup.css({
            top: tryBlueOcean.outerHeight() + 10,
            right: (0 - (popup.outerWidth()/2 - tryBlueOcean.outerWidth()/2))
        });
        popup.click(function(event) {
            event.stopPropagation();
            popup.remove();
            tryBlueOcean.click(tryBlueOceanClickHandler);
        });

        // if the link is clicked on
        $('a', popup).click(function() {
            event.stopPropagation();
            popup.empty();
            popup.append('<h3>Redirecting to System Configuration</h3><h3>Please wait ...</h3>');
        });
    };
    tryBlueOcean.click(tryBlueOceanClickHandler);
    return tryBlueOcean;
}

$(document).ready(() => {
    var contextUrlDiv = $('#blueocean-context-url');
    var tryBlueOceanUrl;

    if (contextUrlDiv.length === 1) {
        var everythingAfterSlashBlue = /\/blue.*$/;
        tryBlueOceanUrl = contextUrlDiv.attr('data-context-url');
        if (window.location.href.indexOf(tryBlueOceanUrl.replace(everythingAfterSlashBlue, '')) === 0) {
            $('#page-head #header').append(aHref(tryBlueOceanUrl));
        } else {
            $('#page-head #header').append(configureJenkinsUrl());
        }
    } else {
        tryBlueOceanUrl = `${getAppUrl()}/blue`;
        $('#page-head #header').append(aHref(tryBlueOceanUrl));
    }
});

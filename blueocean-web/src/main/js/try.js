var $ = require('jquery-detached').getJQuery();

$(document).ready(() => {
    var contextUrlDiv = $('#blueocean-context-url');
    var tryBlueOceanUrl;

    if (contextUrlDiv.length === 1) {
        var blueUrl = contextUrlDiv.attr('data-context-url');
        var classicUrl = contextUrlDiv.attr('data-classic-url');
        if (classicUrl) {
            var indexOfContext = window.location.href.indexOf(classicUrl);
            if (indexOfContext !== -1) {
                tryBlueOceanUrl = window.location.href.substring(0, indexOfContext) + blueUrl;
            }
        }
    }

    if (!tryBlueOceanUrl) {
        // Unable to find a backend Stapler context path for the current page.
        // Only option left is to assume that the rooturl in the <head>
        // is accurate. This *might* not be 100% accurate if there's funky
        // reverse proxying or url rewriting happening on a proxy/intermediary
        // sitting between Jenkins and the browser/client.
        var rooturl = $('head').attr('data-rooturl');
        tryBlueOceanUrl = rooturl + '/blue';
    }

    var tryBlueOcean = $('<a id="open-blueocean-in-context" class="try-blueocean header-callout">Open Blue Ocean</a>');
    tryBlueOcean.attr('href', tryBlueOceanUrl);
    $('#page-head #header').append(tryBlueOcean);
});

var $ = require('jquery-detached').getJQuery();

function getAppUrl() {
    return rootUrl = $('head').attr('data-rooturl');
}

$(document).ready(() => {
    var tryBlueOcean = $('<a id="open-blueocean-in-context" class="try-blueocean header-callout">Open Blue Ocean</a>');
    var contextUrlDiv = $('#blueocean-context-url');
    var tryBlueOceanUrl;

    if (contextUrlDiv.length === 1) {
        tryBlueOceanUrl = contextUrlDiv.attr('data-context-url');
    } else {
        tryBlueOceanUrl = `${getAppUrl()}/blue`;
    }
    tryBlueOcean.attr('href', tryBlueOceanUrl);

    $('#page-head #header').append(tryBlueOcean);
});

var $ = require('jquery-detached').getJQuery();

function getAppUrl() {
    const rootUrl = $('head').attr('data-rooturl');
    if (!rootUrl) {
        return '';
    } else {
        return rootUrl;
    }
}

$(document).ready(() => {
    var tryBlueOcean = $('<div class="try-blueocean header-callout">Try Blue Ocean UI ...</div>');
    var contextUrlDiv = $('#blueocean-context-url');
    var tryBlueOceanUrl;

    if (contextUrlDiv.length === 1) {
        tryBlueOceanUrl = contextUrlDiv.attr('data-context-url');
    } else {
        tryBlueOceanUrl = `${getAppUrl()}/blue`;
    }

    tryBlueOcean.click(() => {
        window.location.replace(tryBlueOceanUrl);
    });

    $('#page-head #header').append(tryBlueOcean);
});

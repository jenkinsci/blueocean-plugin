var $ = require('jquery-detached').getJQuery();

$(document).ready(() => {
    var tryBlueOcean = $('<a id="open-blueocean-in-context" class="try-blueocean header-callout">Open Blue Ocean</a>');
    var contextUrlDiv = $('#blueocean-context-url');
    var tryBlueOceanUrl;

    if (contextUrlDiv.length === 1) {
        var blueUrl = contextUrlDiv.attr('data-context-url');
        var classicUrl = contextUrlDiv.attr('data-classic-url');
        if (classicUrl) {
            tryBlueOceanUrl = window.location.href.replace(classicUrl, blueUrl);
        } else {
            tryBlueOceanUrl = `./blue`;
        }
    } else {
        tryBlueOceanUrl = `./blue`;
    }
    tryBlueOcean.attr('href', tryBlueOceanUrl);

    $('#page-head #header').append(tryBlueOcean);
});

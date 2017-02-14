var $ = require('jquery-detached').getJQuery();

$(document).ready(() => {
    var tryBlueOcean = $('<a id="open-blueocean-in-context" class="try-blueocean header-callout">Open Blue Ocean</a>');
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
        tryBlueOceanUrl = `./blue`;
    }

    tryBlueOcean.attr('href', tryBlueOceanUrl);

    $('#page-head #header').append(tryBlueOcean);
});

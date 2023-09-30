window.addEventListener('load', function() {
    var eventData = { name: 'pageview', properties: { mode: 'classic' } };
    fetch(rootURL + '/blue/rest/analytics/track', {
        method: 'POST',
        headers: crumb.wrap({
            'Content-Type': 'application/json'
        }),
        // TODO simplify when Prototype.js is removed
        body: Object.toJSON ? Object.toJSON(eventData) : JSON.stringify(eventData)
    }).then(function(rsp) {
        if (!rsp.ok) {
            console.error('Could not send pageview event');
        }
    });
});

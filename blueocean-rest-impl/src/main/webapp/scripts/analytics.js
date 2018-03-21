window.addEventListener('load', function() {
    var eventData = { name: 'pageview', properties: { mode: 'classic' } };
    new Ajax.Request(rootURL + '/blue/rest/analytics/track', {
        method: 'POST',
        contentType: 'application/json',
        postBody: JSON.stringify(eventData),
        onFailure: function() {
            console.error('Could not send pageview event');
        },
    });
});

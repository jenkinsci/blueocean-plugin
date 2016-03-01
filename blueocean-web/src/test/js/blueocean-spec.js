//
// Test that we can load the app
//

// See http://zombie.js.org/
var Browser = require("zombie");

describe("blueocean.js", function () {

    it("- test App load", function (done) {
        var browser = new Browser();
        var loads = [];

        browser.debug();
        browser.on('request', function(request) {
            var url = request.url;
            loads.push(url);
        });

        browser.visit('http://localhost:18999/src/test/js/zombie-test-01.html', function() {
            expect(browser.success).toBe(true);

            // Check the requests are as expected.
            expect(loads.length).toBe(3);
            expect(loads[0]).toBe('http://localhost:18999/src/test/js/zombie-test-01.html');
            expect(loads[1]).toBe('http://localhost:18999/target/classes/io/jenkins/blueocean/no_imports/blueocean.js');

            expect(loads[2]).toBe('http://localhost:18999/src/test/resources/blue/javaScriptExtensionInfo');
            //expect(loads[3]).toBe('http://localhost:18999/src/test/resources/mock-adjuncts/io/jenkins/blueocean-admin/jenkins-js-extension.js');

            // Check for some of the elements. We know that the following should
            // be rendered by the React components.
            browser.assert.elements('header', 1);
            browser.assert.elements('footer', 1);
            browser.assert.elements('span.jenkins-logo', 1);

            done();
        });
    });
});

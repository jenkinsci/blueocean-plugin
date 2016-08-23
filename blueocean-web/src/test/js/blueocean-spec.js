//
// Test that we can load the app
//

// See http://zombie.js.org/
var Browser = require('zombie');
/* Disabling this test for now, ti needs to be moved
  into a java test as we need the jwt token 
    
describe('blueocean.js', () => {
    
    it('- test App load', (done) => {
        var browser = new Browser();
        var loads = [];

        browser.debug();
        browser.on('request', (request) => {
            var url = request.url;
            loads.push(url);
        });

        browser.visit('http://localhost:18999/src/test/js/zombie-test-01.html',  () => {
            expect(browser.success).toBe(true);

            // Check the requests are as expected.
            expect(loads.length).toBe(4);
            expect(loads[0]).toBe('http://localhost:18999/src/test/js/zombie-test-01.html');
            expect(loads[1]).toBe('http://localhost:18999/target/classes/io/jenkins/blueocean/no_imports/blueocean.js');
            console.log(loads);
            expect(loads[2]).toBe('http://localhost:18999/src/test/resources/blue/js-extensions');
            //expect(loads[3]).toBe('http://localhost:18999/src/test/resources/mock-adjuncts/io/jenkins/blueocean-dashboard/jenkins-js-extension.js');
            browser.dump(process.stderr);
            // Check for some of the elements. We know that the following should
            // be rendered by the React components.
            browser.assert.elements('header', 1);

            done();
        });
    });
});
*/

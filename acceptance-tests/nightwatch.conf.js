module.exports = (function (settings) {
    var fs = require('fs');
    var url = require('url');
    var launchUrl;
    var netaddr = require('network-address');
  
    if (process.env.LAUNCH_URL) {
        //
        // This allows you to run the tests against a Jenkins instance of
        // your choosing when manually running nightwatch e.g. one running in your
        // IDE, allowing you to debug the server etc.
        //
        // Command line example:
        //   LAUNCH_URL=http://localhost:8080/jenkins nightwatch
        //
        // Remember, you'll need to:
        //   1. set <useSecurity> to "false" in $JENKINS_HOME/config.xml.
        //   2. remove <crumbIssuer> from $JENKINS_HOME/config.xml.
        //   3. run "touch target/.jenkins_test" in the directory in which
        //      you are running jenkins i.e. in the aggregator dir.
        //
        launchUrl = process.env.LAUNCH_URL;
    } else {
        var jenkins_url_file = './runner/.blueocean-ath-jenkins-url';

        if (!fs.existsSync(jenkins_url_file)) {
            throw 'Jenkins not running. Failed to find file: ' + jenkins_url_file;
        }

        launchUrl = fs.readFileSync(jenkins_url_file, 'utf8');
    }
    
    // Replace localhost addresses with the actual IP, allowing it
    // to work inside a docker container running on the host.
    launchUrl = launchUrl.replace('localhost', netaddr());
    launchUrl = launchUrl.replace('127.0.0.1', netaddr());

    var launchUrlObj = url.parse(launchUrl);

    settings.test_settings.default.launch_url = launchUrl;
    settings.test_settings.default.selenium_host = launchUrlObj.hostname;

    if (process.env.BLUEO_SELENIUM_SERVER_ADDR) {
        settings.test_settings.default.selenium_host = process.env.BLUEO_SELENIUM_SERVER_ADDR.trim();
    }

    console.log('Jenkins running at: ' + settings.test_settings.default.launch_url);
    console.log("    NOTE:");
    console.log("        Selenium and the browser (Firefox) are running in a docker");
    console.log("        container that also has VNC. This allows you to connect if");
    console.log("        you'd like to look at the browser while the tests run.");
    console.log("        Simple run:");
    console.log("         $ open vnc://:secret@localhost:15900");
    console.log("");
    console.log("    NOTE:");
    console.log("        The selenium server is at " + settings.test_settings.default.selenium_host + ":4444");
    console.log("");

    if (fs.existsSync('target/.selenium_server_provided')) {
        settings.selenium.start_process = false;
    }
    
    return settings;
})(require('./src/main/nightwatch.json'));

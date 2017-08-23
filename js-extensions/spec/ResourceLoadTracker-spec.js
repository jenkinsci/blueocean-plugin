var jsTest = require('@jenkins-cd/js-test');
var expect = require('chai').expect;

var ResourceLoadTracker = new (require('../dist/ResourceLoadTracker').default)();

describe("ResourceLoadTracker.js", function () {

    // Looking at './javaScriptExtensionInfo-02.json' you'll see that there are a few extension
    // points with impls spread across 2 plugins:
    //
    //  ep-1: implementations in both plugin-1 and plugin-2
    //  ep-2: implementation in plugin-1 only
    //  ep-3: implementation in plugin-2 only
    //
    // So this test should verify that:
    //  1. Mounting ep-1 should result in the CSS for both plugins being loaded.
    //  2. Mounting ep-2 should result in only the CSS from plugin-1 being loaded.
    //  3. Mounting ep-3 should result in only the CSS from plugin-2 being loaded.
    //  4. Mounting all ep-* should result in the CSS for both plugins being loaded.
    //  5. Following from previous ... unmounting ep-1 should result in the CSS for
    //     both plugins still being loaded because ep-2 and ep-3 keep them loaded.
    //  6. Following from previous ... unmounting ep-2 should result in the CSS for
    //     plugin-1 being unloaded, leaving just the CSS for plugin-2.
    //  7. Following from previous ... unmounting ep-3 should result in the CSS for
    //     plugin-2 being unloaded, leaving no CSS.

    it("- test ep-1 loads css from both plugins", function (done) {
        jsTest.onPage(function() {
            var javaScriptExtensionInfo = require('./javaScriptExtensionInfo-02.json');

            // Initialise the load tracker with plugin extension point info.
            ResourceLoadTracker.setExtensionPointMetadata(javaScriptExtensionInfo);

            // Verify that there's no link elements on the page.
            var cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(0);

            // Mounting ep-1 should result in the CSS for both plugins being
            // loaded ...
            ResourceLoadTracker.onMount('ep-1');
            cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(2);
            expect(cssElements[0].getAttribute('href')).to.include('/jenkins/adjuncts/908d75c1/org/jenkins/ui/jsmodules/plugin-1/extensions.css');
            expect(cssElements[1].getAttribute('href')).to.include('/jenkins/adjuncts/908d75c1/org/jenkins/ui/jsmodules/plugin-2/extensions.css');

            // Unmounting ep-1 should result in the CSS for both plugins being
            // unloaded ...
            ResourceLoadTracker.onUnmount('ep-1');
            cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(0);

            done();
        });
    });

    it("- test ep-2 loads css from plugin-1 only", function (done) {
        jsTest.onPage(function() {
            var javaScriptExtensionInfo = require('./javaScriptExtensionInfo-02.json');

            // Initialise the load tracker with plugin extension point info.
            ResourceLoadTracker.setExtensionPointMetadata(javaScriptExtensionInfo);

            // Verify that there's no link elements on the page.
            var cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(0);

            // Mounting ep-2 should result in the CSS for plugin-1 only being
            // loaded ...
            ResourceLoadTracker.onMount('ep-2');
            cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(1);
            expect(cssElements[0].getAttribute('href')).to.include('/jenkins/adjuncts/908d75c1/org/jenkins/ui/jsmodules/plugin-1/extensions.css');

            // Unmounting ep-2 should result in no CSS on the page.
            ResourceLoadTracker.onUnmount('ep-2');
            cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(0);

            done();
        });
    });

    it("- test ep-1, ep-2 and ep-3 loads css in correct order", function (done) {
        jsTest.onPage(function() {
            var javaScriptExtensionInfo = require('./javaScriptExtensionInfo-02.json');

            // Initialise the load tracker with plugin extension point info.
            ResourceLoadTracker.setExtensionPointMetadata(javaScriptExtensionInfo);

            // Verify that there's no link elements on the page.
            var cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(0);

            // Mounting ep-* should result in the CSS for both plugins being
            // loaded ...
            ResourceLoadTracker.onMount('ep-1');
            ResourceLoadTracker.onMount('ep-2');
            ResourceLoadTracker.onMount('ep-3');
            cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(2);
            expect(cssElements[0].getAttribute('href')).to.include('/jenkins/adjuncts/908d75c1/org/jenkins/ui/jsmodules/plugin-1/extensions.css');
            expect(cssElements[1].getAttribute('href')).to.include('/jenkins/adjuncts/908d75c1/org/jenkins/ui/jsmodules/plugin-2/extensions.css');

            done();
        });
    });

    it("- test loading extension CSS from another location for a specific plugin", function (done) {
        jsTest.onPage(function() {
            var javaScriptExtensionInfo = require('./javaScriptExtensionInfo-02.json');
            var jsModules = require('@jenkins-cd/js-modules');
            var ResourceLocationResolver = require('@jenkins-cd/js-modules/js/ResourceLocationResolver');

            // get the 'plugin-1' resources from another host
            // continue to get 'plugin-2' resources from Jenkins adjuncts
            jsModules.addResourceLocationResolver(
                new ResourceLocationResolver(function(moduleSpec) {
                    // 'plugin-1' is the hpiPluginId set in ./javaScriptExtensionInfo-02.json
                    return (moduleSpec.namespace === 'plugin-1');
                }, function(moduleSpec, srcPath) {
                    return "http://resources.acme.com/javascript/" + srcPath;
                })
            );

            // Initialise the load tracker with plugin extension point info.
            ResourceLoadTracker.setExtensionPointMetadata(javaScriptExtensionInfo);

            // Verify that there's no link elements on the page.
            var cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(0);

            // Mounting ep-* should result in the CSS for both plugins being
            // loaded ...
            ResourceLoadTracker.onMount('ep-1');
            ResourceLoadTracker.onMount('ep-2');
            ResourceLoadTracker.onMount('ep-3');
            cssElements = document.getElementsByTagName('link');
            expect(cssElements.length).to.equal(2);
            expect(cssElements[0].getAttribute('href')).to.equal('http://resources.acme.com/javascript/org/jenkins/ui/jsmodules/plugin-1/extensions.css');
            expect(cssElements[1].getAttribute('href')).to.include('/jenkins/adjuncts/908d75c1/org/jenkins/ui/jsmodules/plugin-2/extensions.css');

            done();
        });
    });
});

var expect = require('chai').expect;

describe("js-builder plugin test", function () {

    it("- test readYAMLFile", function () {
        var jsBuilder = require('../@jenkins-cd/subs/extensions-bundle');
        var asJSON = jsBuilder.readYAMLFile('./spec/sample-extensions.yaml');

        assertSampleJSONOkay(asJSON);
    });

    it("- test yamlToJSON", function () {
        var jsBuilder = require('../@jenkins-cd/subs/extensions-bundle');

        jsBuilder.yamlToJSON('./spec/sample-extensions.yaml', './target/sample-extensions.json');
        var asJSON = require('../target/sample-extensions.json');

        assertSampleJSONOkay(asJSON);
    });

    function assertSampleJSONOkay(asJSON) {
        expect(asJSON.id).to.equal('com.example.my.plugin');
        expect(asJSON.artefacts.page.id).to.equal('about-my-plugin');
        expect(asJSON.artefacts.components[0].id).to.equal('MyNeatButton');
        expect(asJSON.artefacts.components[1].id).to.equal('SuperList');
    }
});

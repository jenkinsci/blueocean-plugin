'use strict';

const React = require('react');
const jsTest = require('@jenkins-cd/js-test');
const assert = require('chai').assert;
const mount = require('enzyme').mount;
const jsdom = require('jsdom');
const ExtensionRenderer = require('../dist/ExtensionRenderer.js').default;

function mockExtension(props) {
    return (
        React.createElement('h1', {}, 'Extension is a H1')
    );
}

const extensionPointMetadata = {
    ep1: [
        mockExtension
    ]
};

const mockExtensionStore = {
    getExtensions: function(extensionPoint, filters, onLoad) {
        onLoad = onLoad || filters;
        onLoad(extensionPointMetadata[extensionPoint] || []);
    }
};

const mockResourceLoadTracker = {
    onMount: function(extensionPoint, readyCallback) {
        readyCallback();  // Invoke immediately, just like an extension with no CSS to load
    },

    onUnmount: function() {
        // Don't care
    }
};

describe('ExtensionRenderer', function () {

    var oldDocument;
    var oldWindow;

    var oldExtensionStore;
    var oldResourceLoadTracker;

    before(function() {

        oldDocument = global.document;
        oldWindow = global.window;

        const doc = jsdom.jsdom('<!doctype html><html><body></body></html>');
        global.document = doc;
        global.window = doc.defaultView;
    });

    beforeEach(function () {
        oldExtensionStore = ExtensionRenderer.ExtensionStore;
        oldResourceLoadTracker = ExtensionRenderer.ResourceLoadTracker;

        ExtensionRenderer.extensionStore = mockExtensionStore;
        ExtensionRenderer.resourceLoadTracker = mockResourceLoadTracker;
    });

    afterEach(function () {
        ExtensionRenderer.extensionStore = oldExtensionStore;
        ExtensionRenderer.resourceLoadTracker = oldResourceLoadTracker;
    });

    after(function () {
        global.document = oldDocument;
        global.window = oldWindow;
    });

    it('should do nothing interesting by default', function () {
        const result = mount(React.createElement(ExtensionRenderer, {extensionPoint: 'foo.bar.baz'}));
        assert.isTrue(result.is('ExtensionRenderer'), 'should be ExtensionRenderer');
        assert.equal(result.length, 1, 'length');
        assert.equal(result.children().length, 0, 'children.length');
        assert.equal(result.html(), '<div class="ExtensionPoint foo-bar-baz"></div>', 'html');
        // Fixme: ^^^^ figure out how to test the rendered element name other than html() string comparison
    });

    it('should show default children if no extension found', function () {
        const result = mount(React.createElement(ExtensionRenderer, {extensionPoint: 'foo.bar.baz'}, 'Default text node'));
        assert.isTrue(result.is('ExtensionRenderer'), 'should be ExtensionRenderer');
        assert.equal(result.length, 1, 'length');
        assert.equal(result.html(), '<div class="ExtensionPoint foo-bar-baz">Default text node</div>', 'html output');
    });

    it('should change the wrapping element', function () {
        const result = mount(React.createElement(ExtensionRenderer, {extensionPoint: 'ep1', wrappingElement: 'section'}));
        assert.equal(result.html(), '<section class="ExtensionPoint ep1"><div><h1>Extension is a H1</h1></div></section>', 'html output');
    });

    it('should render the extension', function () {
        const result = mount(React.createElement(ExtensionRenderer, {extensionPoint: 'ep1'}));
        assert.equal(result.html(), '<div class="ExtensionPoint ep1"><div><h1>Extension is a H1</h1></div></div>', 'html output');
    });

    it('should render a custom class name', function () {
        const result = mount(React.createElement(ExtensionRenderer, {extensionPoint: 'ep1', className: 'super-dooper'}));
        assert.equal(result.html(), '<div class="ExtensionPoint ep1 super-dooper"><div><h1>Extension is a H1</h1></div></div>', 'html output');
    });

    it('should should not show default children when extension is present', function () {
        const result = mount(React.createElement(ExtensionRenderer, {extensionPoint: 'ep1'}, 'Default text node'));
        assert.equal(result.html(), '<div class="ExtensionPoint ep1"><div><h1>Extension is a H1</h1></div></div>', 'html output');
    });


});

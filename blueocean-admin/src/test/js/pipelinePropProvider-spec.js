import React, {Component} from 'react';
import sd from 'skin-deep';
import { assert} from 'chai';

import pipelinePropProvider from '../../main/js/components/pipelinePropProvider';

class DummyChild extends Component {
    render() {
        return <div>Pipeline is '{String(this.props.pipeline)}'</div>;
    }
}

describe("pipelinePropProvider", () => {

    const pipelineValue = {
        foo: "bar",
        baz: "quux",
        toString() {return "pipeline value"}
    };

    const otherValue = "Rubber baby buggy bumpers";

    const WrappedDummy = pipelinePropProvider(DummyChild);

    let props = {};

    // Using a provider function turns on Skin-deep's context support for some reason
    const providerFn = () => React.createElement(WrappedDummy, props);

    let tree, instance, vdom; // Rendered results
    let context = {};

    beforeEach(() => {
        context = {};
        props = {};
    });

    describe("with value in context", () => {

        beforeEach(() => {
            context.pipeline = pipelineValue;
            props.otherProp = otherValue;

            tree = sd.shallowRender(providerFn, context);
            instance = tree.getMountedInstance();
            vdom = tree.getRenderOutput();
        });

        it("should render", () => {
            assert.isNotNull(tree);
            assert.isNotNull(instance);
            assert.isNotNull(vdom);
        });

        it("should extract the correct value", () => {
            assert.equal(vdom.props.pipeline, pipelineValue, "pipeline property on vdom");
        });

        it("should pass on other props", () => {
            assert.equal(vdom.props.otherProp, otherValue, "otherProp property on vdom");
        });
    });

    describe("with no value in context", () => {

        beforeEach(() => {
            props.otherProp = otherValue;

            tree = sd.shallowRender(providerFn, context);
            instance = tree.getMountedInstance();
            vdom = tree.getRenderOutput();
        });

        it("should render", () => {
            assert.isNotNull(tree);
            assert.isNotNull(instance);
            assert.isNotNull(vdom);
        });

        it("should extract nothing from context", () => {
            assert.equal(vdom.props.pipeline, null, "pipeline property on vdom");
        });

        it("should pass on other props", () => {
            assert.equal(vdom.props.otherProp, otherValue, "otherProp property on vdom");
        });
    });
});

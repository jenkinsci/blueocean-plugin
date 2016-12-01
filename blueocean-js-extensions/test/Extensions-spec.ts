/// <reference path="../node_modules/@types/mocha/index.d.ts" />
/// <reference path="../node_modules/@types/chai/index.d.ts" />

import 'mocha';
import { assert } from 'chai';

import extensionRegistry, {
    ExtensionPoint,
    extensionPoints,
    Extension,
    ExtensionList,
    ExtensionTypes,
    Inject,
    Ordinal,
    ExtensionListChangeListener,
    ExtensionPointChangeListener
} from '../src/Extensions';

extensionRegistry._reset();

@ExtensionPoint
class SampleExtensionPoint {
    _title: string;
    title(): string {
        return this._title;
    }
    activate(context: any): void {}
}

@Extension(SampleExtensionPoint)
class SampleExtensionNotSubclass {
    activate(context: any): void {
        context.activated = true;
        context.activatedBy = 'SampleExtensionNotSubclass';
    }
}

@Extension
@Ordinal(1)
class SampleExtensionIsSubclass extends SampleExtensionPoint {
    activate(context: any): void {
        context.activated = true;
        context.activatedBy = 'SampleExtensionIsSubclass';
    }
}

class ExtensionConsumer {
    @ExtensionList(SampleExtensionPoint) passedByArgument: any[];
}

@Extension
@Ordinal(2)
class SampleExtensionIsSubclass2 extends SampleExtensionPoint {
    activate(context: any): void {
        context.activated = true;
        context.activatedBy = 'SampleExtensionIsSubclass';
    }
}

@Extension
@Ordinal(0)
class SampleExtensionIsSubclass0 extends SampleExtensionPoint {
    activate(context: any): void {
        context.activated = true;
        context.activatedBy = 'SampleExtensionIsSubclass';
    }
}

describe('ExtensionPoints', function() {
  describe('Basic functionality', function() {
    it('should add extensions', function() {
      assert(extensionRegistry.getExtensions(SampleExtensionPoint).length > 0, 'Should have some extensions added...');
    });

    it('should return correct extensions', function() {
      assert(extensionRegistry.getExtensions(SampleExtensionPoint).length == 4, 'Should have exactly 2 extensions');
    });

    it('should return extensions sorted by priorty', function() {
      assert(extensionRegistry.getExtensions(SampleExtensionPoint)[0] === SampleExtensionIsSubclass0, 'Should have extensions ordered properly');
      assert(extensionRegistry.getExtensions(SampleExtensionPoint)[1] === SampleExtensionIsSubclass, 'Should have extensions ordered properly');
      assert(extensionRegistry.getExtensions(SampleExtensionPoint)[2] === SampleExtensionIsSubclass2, 'Should have extensions ordered properly');
      assert(extensionRegistry.getExtensions(SampleExtensionPoint)[3] === SampleExtensionNotSubclass, 'Should have extensions ordered properly');
    });

    it('should provide @ExtensionList properly', function() {
        const e = new ExtensionConsumer();
        assert(e.passedByArgument.length === 4, 'ExtensionList should work by argument');
    });

    it('should register, deregister, and notify extension listeners', function() {
        let wasCalled = false;
        const listener: ExtensionListChangeListener = (a, b, c) => {
            wasCalled = true;
        };
        extensionRegistry.addExtensionListener(listener);

        class ManuallyRegistered {}
        class ManuallyRegistered2 {}

        extensionRegistry.registerTypedExtension(SampleExtensionPoint, ManuallyRegistered, () => null);

        assert(wasCalled, 'ExtensionList should call listeners when changed');

        wasCalled = false;
        extensionRegistry.removeExtensionListener(listener);
        extensionRegistry.registerTypedExtension(SampleExtensionPoint, ManuallyRegistered2, () => null);

        assert(!wasCalled, 'ExtensionList should not call listeners once removed');
    });

    it('should register, deregister, and notify extension point listeners', function() {
        let wasCalled = false;
        const listener: ExtensionPointChangeListener = (a, b) => {
            wasCalled = true;
        };
        extensionRegistry.addExtensionPointListener(listener);

        class ManuallyRegistered {}
        class ManuallyRegistered2 {}

        extensionRegistry.registerTypedExtensionPoint(ManuallyRegistered);

        assert(wasCalled, 'ExtensionList should call listeners when changed');

        wasCalled = false;
        extensionRegistry.removeExtensionPointListener(listener);
        extensionRegistry.registerTypedExtensionPoint(ManuallyRegistered2);

        assert(!wasCalled, 'ExtensionList should not call listeners once removed');
    });

    it('should provide classes when @ExtensionTypes is used', function() {
        class c {
            @ExtensionTypes(SampleExtensionPoint) injected: any[];
        }
        let instance = new c();
        assert(instance.injected
            && instance.injected.length
            && instance.injected[0].constructor === Function
            && instance.injected[0] === SampleExtensionIsSubclass0, 'Classes should be provided with @ExtensionTypes');
    });

    it('should provide instances when @ExtensionList is used', function() {
        class c {
            @ExtensionList(SampleExtensionPoint) injected: any[];
        }
        let instance = new c();
        assert(instance.injected
            && instance.injected.length
            && instance.injected[0] instanceof SampleExtensionIsSubclass0, 'Instances should be provided with @ExtensionList');
    });

    it('should provide a single instance when @Inject is used', function() {
        class c {
            @Inject(SampleExtensionPoint) injected: any;
        }
        let instance = new c();
        assert(instance.injected.constructor === SampleExtensionIsSubclass0, 'Instances should be provided with @Inject');
    });
  });
});

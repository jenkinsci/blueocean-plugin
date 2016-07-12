var jsdom = require('jsdom').jsdom;

var exposedProperties = ['window', 'navigator', 'document'];

global.document = jsdom('');
global.window = document.defaultView;
Object.keys(document.defaultView).forEach((property) => {
    if (typeof global[property] === 'undefined') {
        exposedProperties.push(property);
        global[property] = document.defaultView[property];
    }
});

global.navigator = {
    userAgent: 'node.js',
};


import React from 'react';
import { assert } from 'chai';
import { mount, render, shallow } from 'enzyme';
import sd from 'skin-deep';
import Immutable from 'immutable';
import { store as ExtensionStore } from '@jenkins-cd/js-extensions';

import Pipelines from '../../main/js/components/Pipelines.jsx';
import { pipelines } from   './data/pipelines/pipelinesSingle';
import { pipelinesDupName } from './data/pipelines/pipelinesTwoJobsSameName';

const
  resultArrayHeaders = ['Name', 'Status', 'Branches', 'Pull Requests', '']
  ;

describe('Pipelines', () => {
    let tree;

    const pipelineList = Immutable.fromJS(pipelines);

    const config = {
        getRootURL: () => '/',
    };

    const params = {};

    describe('basic table rendering', () => {
        beforeEach(() => {
            tree = sd.shallowRender(
                () => React.createElement(Pipelines), // For some reason using a fn turns on context
                {
                    pipelines: pipelineList,
                    params,
                    config,
                }
            );
        });

        it('check header to be as expected', () => {
            const header = tree.subTree('Table').getRenderOutput();
            assert.equal(header.props.headers.length, resultArrayHeaders.length);
        });

        it('check rows number to be as expected', () => {
            const row = tree.everySubTree('PipelineRowItem');
            assert.equal(row.length, 2);
        });
    });

    describe('duplicate job names', () => {
        it('should render two rows when job names are duplicated across folders', () => {
            const context = {
                config,
                params,
                pipelines: pipelinesDupName,
            };

            ExtensionStore.init({
                extensionDataProvider: function () {},
            });

            const wrapper = mount(
                <Pipelines />,
                { context },
            );

            assert.equal(wrapper.find('PipelineRowItem').length, 2);
        });
    });
});

import React from 'react';
import { assert } from 'chai';
import { shallow } from 'enzyme';

import { Pipelines } from '../../main/js/components/Pipelines.jsx';
import { pipelines } from './data/pipelines/pipelinesSingle';
import { pipelinesDupName } from './data/pipelines/pipelinesTwoJobsSameName';

import { mockExtensionsForI18n } from './mock-extensions-i18n';

const resultArrayHeaders = ['Name', 'Status', 'Branches', 'Pull Requests', ''];

describe('Pipelines', () => {
    beforeAll(() => mockExtensionsForI18n());

    describe('basic table rendering', () => {
        let wrapper;

        beforeEach(() => {
            const context = {
                params: {},
                location: {},
                pipelineService: {
                    allPipelinesPager() {
                        return {
                            data: pipelines,
                        };
                    },
                    pipelinesPager() {
                        return {
                            data: pipelines,
                        };
                    },
                },
            };

            const location = {
                query: {}
            };


            wrapper = shallow(
                <Pipelines location={location} params={context.params} setTitle={() => {}} />,
                {
                    context,
                }
            );
        });

        it('check header to be as expected', () => {
            assert.equal(wrapper.find('JTable').props().columns.length, resultArrayHeaders.length);
        });

        it('check rows number to be as expected', () => {
            assert.equal(wrapper.find('PipelineRowItem').length, 2);
        });
    });

    describe('pending state', () => {
        it('should continue to render existing data while a fetch is pending', () => {
            const context = {
                params: {},
                location: {},
                pipelineService: {
                    allPipelinesPager() {
                        return {
                            pending: true,
                            data: pipelines,
                        };
                    },
                    pipelinesPager() {
                        return {
                            data: pipelines,
                        };
                    },
                },
            };

            const location = {
                query: {}
            };

            const wrapper = shallow(
                <Pipelines location={location} params={context.params} setTitle={() => {}} />,
                { context },
            );

            assert.equal(wrapper.find('PipelineRowItem').length, 2);
        });
    });

    describe('duplicate job names', () => {
        it('should render two rows when job names are duplicated across folders', () => {
            const context = {
                params: {
                    organization: 'jenkins',
                },
                pipelineService: {
                    organiztionPipelinesPager() {
                        return {
                            data: pipelinesDupName,
                        };
                    },
                    pipelinesPager() {
                        return {
                            data: pipelines,
                        };
                    },
                },
            };

            const location = {
                query: {}
            };

            const wrapper = shallow(
                <Pipelines location={location} params={context.params} setTitle={() => {}} />,
                { context },
            );

            assert.equal(wrapper.find('PipelineRowItem').length, 2);
        });
    });
});

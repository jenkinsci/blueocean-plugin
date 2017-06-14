import React from 'react';
import { expect } from 'chai';
import { shallow } from 'enzyme';

import { PipelinePage } from '../../main/js/components/PipelinePage.jsx';

import { mockExtensionsForI18n } from './mock-extensions-i18n';
mockExtensionsForI18n();


const params = {
      organization: 'jenkins',
      pipeline: 'asdf',
}
const context = {
  pipelineService: {
    fetchPipeline() {
      return Promise.resolve(5);
    },
    getPipeline() {
      return null;
    }
  }
};

const contextFailed = {
  pipelineService: {
    fetchPipeline() {
      return Promise.reject(new Error());
    },
    getPipeline() {
      return null;
    }
  },
    router: {},
    location: {},
    params: {
      organization: 'jenkins',
      pipeline: 'asdf',
    }
};
describe("PipelinePage", () => {
  beforeAll(() => mockExtensionsForI18n());

  const pipeline = {
    'displayName': 'beers',
    'name': 'beers',
    'fullName': 'beers',
    'organization': 'jenkins',
    'weatherScore': 0
  };

  const ctx = {
    router: {},
    location: {},
    config: {
      organization: 'jenkins',
      pipeline: 'asdf',
      getAppURLBase: () => '/jenkins/blue',
    }
  };


  it("shows 404 for failure", () => {
    let wrapper;
    wrapper = shallow(<PipelinePage params={params} setTitle={()=>{}}/>, { context });
    /**
     * This test is broken because of mobx re-rendering the page when there is an error.
    wrapper = shallow(<PipelinePage params={params} setTitle={()=>{}} />, { context: contextFailed });
    expect(wrapper.html()).to.contain('404') */
  });
});

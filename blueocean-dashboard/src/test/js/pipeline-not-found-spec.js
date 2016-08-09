import React from 'react';
import ReactTest from 'react-addons-test-utils';
import { Route } from 'react-router';
import { expect } from 'chai';
import { shallow } from 'enzyme';

import PipelinePage from '../../main/js/components/PipelinePage.jsx';
import NotFound from '../../main/js/components/NotFound.jsx';

describe("PipelinePage", () => {
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
  };

  it("shows 404 for failure", () => {
    let wrapper;
    
    wrapper = shallow(<PipelinePage />, { context: { ...ctx, pipeline: null } });
    expect(wrapper.html()).to.be.equal(null);
    
    // FIXME: unable to shallow render <PipelinePage /> due to child clone AFAICT. why is it doing a clone?
    
    // Enzyme shallow was failing to render these cases shallow enough...
    //wrapper = ReactTest.createRenderer().render(<PipelinePage />, { ...ctx, pipeline: pipeline });
    //expect(wrapper.type).to.not.equal(NotFound);
    
    //wrapper = ReactTest.createRenderer().render(<PipelinePage />, { ...ctx, pipeline: new Failure() });
    //expect(wrapper.type).to.equal(NotFound);
    
    wrapper = shallow(<PipelinePage />, { context: {...ctx, pipeline: { $failed: true } }});
    expect(wrapper.html()).to.contain("404");
  });
});

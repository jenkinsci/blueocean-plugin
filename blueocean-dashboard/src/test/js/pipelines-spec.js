import React from 'react';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';

import Pipelines from '../../main/js/components/Pipelines.jsx';
import { pipelines } from './pipelines';

const
  resultArrayHeaders = ['Name', 'Status', 'Branches', 'Pull Requests', '']
  ;

describe("pipelines", () => {
  let tree;

  const config = {
    getRootURL: () => "/"
  };

  beforeEach(() => {
      tree = sd.shallowRender(
          ()=>React.createElement(Pipelines), // For some reason using a fn turns on context
          {
            pipelines: Immutable.fromJS(pipelines),
            config
          }
      );
  });

  it("renders pipelines - check header to be as expected", () => {
    const
      header = tree.subTree('Table').getRenderOutput();
    assert.equal(header.props.headers.length, resultArrayHeaders.length);
  });

  it("renders pipelines - check rows number to be as expected", () => {
    const
      row = tree.everySubTree('PipelineRowItem')
      ;
    assert.equal(row.length, 2);
  });

});

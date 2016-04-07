//FIXME: should be part of the jdl, but test are broken there
import React from 'react';
import { assert} from 'chai';
import sd from 'skin-deep';
import Immutable from 'immutable';

import { LogConsole }
    from '@jenkins-cd/design-language';

import {log} from './runs_log'

describe("LogConsole should not render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<LogConsole />);
  });

  it("without result set", () => {
    const logConsole = tree.getRenderOutput();
    assert.isNull(logConsole);
  });

});

describe("LogConsole should render", () => {
  let tree = null;

  beforeEach(() => {
    tree = sd.shallowRender(<LogConsole result={log}/>);
  });

  it("does render success", () => {
    const logConsole = tree.getRenderOutput();
    assert.isNotNull(logConsole);
    assert.isNotNull(logConsole.props.result);
  });

});

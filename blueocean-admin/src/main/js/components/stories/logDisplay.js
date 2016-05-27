import React from 'react';
import { storiesOf } from '@kadira/storybook';
import Node from '../Node';
import Nodes from '../Nodes';
import { getStagesInformation } from '../../util/logDisplayHelper';

import { runNodesRunning, runNodesFail, runNodesSuccess } from '../../../../test/js/runNodes';
import { finishedMultipleFailure } from '../../../../test/js/runNodes-finishedMultipleFailure';
import {
  firstFinishedSecondRunning,
} from '../../../../test/js/runNodes-firstFinishedSecondRunning';

const nodes = getStagesInformation(runNodesRunning);
storiesOf('logNode', module)
  .add('with a running node', () => (
    <Node node={nodes.model[0]} />
  ))
  .add('no Node should return null', () => (
    <Node />
  ));
const informationFailed = getStagesInformation(runNodesFail);
const informationFailed2 = getStagesInformation(finishedMultipleFailure);
const informationSuccess = getStagesInformation(runNodesSuccess);
const informationRunning = getStagesInformation(firstFinishedSecondRunning);
storiesOf('logNodes', module)
  .add('with nodes failing', () => (
    <Nodes nodeInformation={informationFailed} />
  ))
  .add('with nodes failing more', () => (
    <Nodes nodeInformation={informationFailed2} />
  ))
  .add('with nodes success', () => (
    <Nodes nodeInformation={informationSuccess} />
  ))
  .add('with nodes running', () => (
    <Nodes nodeInformation={informationRunning} />
  ))
  .add('no Nodes should return null', () => (
    <Nodes />
  ));

/* eslint-disable */

import React from 'react';
import { storiesOf } from '@kadira/storybook';
import Node from '../Step';
import Nodes from '../Steps';
import { getNodesInformation } from '../../util/logDisplayHelper';

import { runNodesRunning, runNodesFail, runNodesSuccess } from '../../../../test/js/runNodes';
import { finishedMultipleFailure } from '../../../../test/js/runNodes-finishedMultipleFailure';
import {
  firstFinishedSecondRunning,
} from '../../../../test/js/runNodes-firstFinishedSecondRunning';

// FIXME if we want to keep them fix them, if not remove it
const fetchLog = (config) => console.log('fetching', config);
const nodes = getNodesInformation(runNodesRunning);
// storiesOf('logNode', module)
//   .add('with a running node', () => (
//     <Node node={nodes.model[0]} fetchLog={fetchLog} />
//   ))
//   .add('no Node should return null', () => (
//     <Node fetchLog={fetchLog} />
//   ));
const informationFailed = getNodesInformation(runNodesFail);
const informationFailed2 = getNodesInformation(finishedMultipleFailure);
const informationSuccess = getNodesInformation(runNodesSuccess);
const informationRunning = getNodesInformation(firstFinishedSecondRunning);
// storiesOf('logNodes', module)
//   .add('with nodes failing', () => (
//     <Nodes nodeInformation={informationFailed} fetchLog={fetchLog} />
//   ))
//   .add('with nodes failing more', () => (
//     <Nodes nodeInformation={informationFailed2} fetchLog={fetchLog} />
//   ))
//   .add('with nodes success', () => (
//     <Nodes nodeInformation={informationSuccess} fetchLog={fetchLog} />
//   ))
//   .add('with nodes running', () => (
//     <Nodes nodeInformation={informationRunning} fetchLog={fetchLog} />
//   ))
//   .add('no Nodes should return null', () => (
//     <Nodes fetchLog={fetchLog} />
//   ));

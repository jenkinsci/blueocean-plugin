/**
 * Created by cmeyers on 9/23/16.
 */

import { PipelineApi } from './PipelineApi';
import { QueueApi } from './QueueApi';
import { RunApi } from './RunApi';

export default {
    pipelineApi: new PipelineApi(),
    queueApi: new QueueApi(),
    runApi: new RunApi(),
};

import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { LogConsole } from '@jenkins-cd/design-language';

import { log } from '../../../../test/js/runs_log';

storiesOf('LogConsole', module)
    .add('render xxx', () => (
        <LogConsole result="xxx" />
    ))
    .add('render real log', () => (
        <LogConsole result={log} />
    ))
;

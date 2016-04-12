import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Link } from 'react-router';
import { PipelineResult } from '../pipeResult/Result.jsx';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PageTabs,
    TabLink,
    LogConsole
} from '@jenkins-cd/design-language';

import { log } from '../../../../test/js/runs_log';

storiesOf('ModalView', module)
    .add('render success ModalView', () => (
        <ModalView isVisible />
    ))
    .add('render success ModalView', () => (
        <ModalView isVisible>
            <ModalHeader>
                <PipelineResult result="success" />
            </ModalHeader>
        </ModalView>
    ))
    .add('render error ModalView', () => (
        <ModalView isVisible>
            <ModalHeader>
                <PipelineResult result="failure" />
            </ModalHeader>
        </ModalView>
    ))
;

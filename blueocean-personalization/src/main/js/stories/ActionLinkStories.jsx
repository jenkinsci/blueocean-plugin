/**
 * Created by cmeyers on 6/28/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';

import ActionLink from '../components/ActionLink';

storiesOf('ActionLink', module)
    .add('test', () =>
        <ActionLink />
    );

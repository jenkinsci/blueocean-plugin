import React from 'react';
import { storiesOf } from '@kadira/storybook';

import SampleIcon from './SampleIcon.jsx';


storiesOf('Morpho', module)
    .add('icon', () => (
        <SampleIcon />
    ))
;

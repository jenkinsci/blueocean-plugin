import React from 'react';
import { storiesOf } from '@kadira/storybook';

import InputStep from '../karaoke/components/InputStep';
import { step } from './data/step';

storiesOf('InputStep', module)
    .add('default', () => <Default />)
;

const style = { padding: 10 };

function Default() {
    return (
        <div style={style}>
            <InputStep node={step} />
        </div>
    );
}

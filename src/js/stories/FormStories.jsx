/**
 * Created by cmeyers on 11/2/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import {RadioButtonGroup} from '../components/forms/RadioButtonGroup';
import {FormTextInput} from '../components/forms/FormTextInput';

storiesOf('Forms', module)
    .add('example', () => <Example />)
;

const style = {
    padding: 5,
};

function Example() {
    return (
        <div style={style}>
            <RadioButtonGroup
              options={['A','B','C']}
              defaultOption={'B'}
              labelFunction={(option) => `${option}!`}
            />

            <FormTextInput
              title="Full Name"
              placeholder="Name"
              defaultValue="John Smith"
            />
        </div>
    );
}

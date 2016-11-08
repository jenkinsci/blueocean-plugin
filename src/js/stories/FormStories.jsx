/**
 * Created by cmeyers on 11/2/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import RadioGroup from '../components/forms/RadioButtonGroup';
import TextInput from '../components/forms/FormTextInput';

storiesOf('Forms', module)
    .add('example', () => <Example />)
;

const style = {
    padding: 5,
};

function Example() {
    return (
        <div style={style}>
            <RadioGroup
              options={['A','B','C']}
              defaultOption={'B'}
              labelFunction={(option) => `${option}!`}
            />

            <TextInput placeholder="Name" defaultValue="John Smith" />
        </div>
    );
}

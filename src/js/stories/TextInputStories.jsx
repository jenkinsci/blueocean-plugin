import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { TextInput } from '../components/forms/TextInput';

storiesOf('TextInput', module)
    .add('default', () => <Default />)
    .add('placeholder', () => <Placeholder />)
    .add('default value', () => <DefaultValue />)
    .add('callbacks', () => <Callbacks />)
;

const style = {padding: 10};

function Default() {
    return (
        <div style={style}>
            <TextInput />
        </div>
    );
}

function Placeholder() {
    return (
        <div style={style}>
            <TextInput placeholder="This is a placeholder." />
        </div>
    );
}

function DefaultValue() {
    return (
        <div style={style}>
            <TextInput defaultValue="I have a default value." />
        </div>
    );
}

function Callbacks() {
    return (
        <div style={style}>
            <TextInput
                onChange={val => console.log('onChange', val)}
                onBlur={val => console.log('onBlur', val)}
            />
        </div>
    );
}

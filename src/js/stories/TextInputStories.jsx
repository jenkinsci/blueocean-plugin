import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { PasswordInput } from '../components/forms/PasswordInput';
import { TextInput } from '../components/forms/TextInput';

storiesOf('TextInput', module)
    .add('general', () => <General />)
    .add('callbacks', () => <Callbacks />)
    .add('sizes', () => <Sizes />)
;

const style = {padding: 10};
const name= { name: 'testTextInput' };

function General() {
    return (
        <div>
            <div style={style}>
                <p>Default</p>

                <TextInput />
            </div>
            <div style={style}>
                <p>Disabled</p>

                <TextInput disabled />
            </div>
            <div style={style}>
                <p>Placeholder</p>

                <TextInput placeholder="This is a placeholder." />
            </div>
            <div style={style}>
                <p>Placeholder</p>

                <TextInput defaultValue="I have a default value." />
            </div>
            <div style={style}>
                <p>Password</p>

                <PasswordInput defaultValue="I have a default value." />
            </div>
        </div>
    );
}

function Callbacks() {
    return (
        <div style={style}>
            <TextInput
                { ...name }
                onChange={val => console.log('onChange', val)}
                onBlur={val => console.log('onBlur', val)}
            />
        </div>
    );
}

function Sizes() {
    const style2 = { padding: '20px 10px'};
    return (
        <div>
            <div style={style2}>
                <TextInput placeholder="TextInput using no layout" { ...name } />
                <br /> <br />
                <PasswordInput placeholder="PasswordInput using no layout" { ...name } />
            </div>
            <div className="layout-small" style={style2}>
                <TextInput placeholder="TextInput using layout-small" { ...name } />
                <br /> <br />
                <PasswordInput placeholder="PasswordInput using layout-small" { ...name } />
            </div>
            <div className="layout-medium" style={style2}>
                <TextInput placeholder="TextInput using layout-medium" { ...name } />
                <br /> <br />
                <PasswordInput placeholder="PasswordInput using layout-medium" { ...name } />
            </div>
            <div className="layout-large" style={style2}>
                <TextInput placeholder="TextInput using layout-large" { ...name } />
                <br /> <br />
                <PasswordInput placeholder="PasswordInput using layout-large" { ...name } />
            </div>
        </div>
    );
}

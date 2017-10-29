import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { PasswordInput } from '../components/forms/PasswordInput';
import { TextInput } from '../components/forms/TextInput';

storiesOf('TextInput', module)
    .add('general', () => <General />)
    .add('icons', () => <Icons />)
    .add('callbacks', () => <Callbacks />)
    .add('sizes', () => <Sizes />)
    .add('sizes - icons', () => <SizesIcons />)
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

function Icons() {
    return (
        <div>
            <div style={style}>
                <p>None</p>

                <TextInput />
            </div>
            <div style={style}>
                <p>Left</p>

                <TextInput iconLeft="ActionSearch" />
            </div>
            <div style={style}>
                <p>Right</p>

                <TextInput iconRight="NavigationClose" />
            </div>
            <div style={style}>
                <p>Both</p>

                <TextInput iconLeft="ActionSearch" iconRight="NavigationClose" />
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

function SizesIcons() {
    const style2 = { padding: '20px 10px'};
    return (
        <div>
            <div style={style2}>
                <TextInput placeholder="no layout" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" placeholder="no layout" { ...name } />
                <br /> <br />
                <TextInput iconRight="close" placeholder="no layout" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" iconRight="close" placeholder="no layout" { ...name } />
            </div>
            <div className="layout-small" style={style2}>
                <TextInput placeholder="layout-small" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" placeholder="layout-small" { ...name } />
                <br /> <br />
                <TextInput iconRight="close" placeholder="layout-small" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" iconRight="close" placeholder="layout-small" { ...name } />
            </div>
            <div className="layout-medium" style={style2}>
                <TextInput placeholder="layout-medium" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" placeholder="layout-medium" { ...name } />
                <br /> <br />
                <TextInput iconRight="close" placeholder="layout-medium" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" iconRight="close" placeholder="layout-medium" { ...name } />
            </div>
            <div className="layout-large" style={style2}>
                <TextInput placeholder="layout-large" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" placeholder="layout-large" { ...name } />
                <br /> <br />
                <TextInput iconRight="close" placeholder="layout-large" { ...name } />
                <br /> <br />
                <TextInput iconLeft="search" iconRight="close" placeholder="layout-large" { ...name } />
            </div>
        </div>
    );
}

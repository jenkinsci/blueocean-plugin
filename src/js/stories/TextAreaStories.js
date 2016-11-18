import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { TextArea } from '../components/forms/TextArea';

storiesOf('TextArea', module)
    .add('default', () => <Default />)
    .add('placeholder', () => <Placeholder />)
    .add('default value', () => <DefaultValue />)
    .add('callbacks', () => <Callbacks />)
    .add('sizes', () => <Sizes />)
;

const style = {padding: 10};

function Default() {
    return (
        <div style={style}>
            <TextArea />
        </div>
    );
}

function Placeholder() {
    return (
        <div style={style}>
            <TextArea placeholder="This is a placeholder." />
        </div>
    );
}

function DefaultValue() {
    return (
        <div style={style}>
            <TextArea defaultValue="I have a default value." />
        </div>
    );
}

function Callbacks() {
    return (
        <div style={style}>
            <TextArea
                onChange={val => console.log('onChange', val)}
                onBlur={val => console.log('onBlur', val)}
            />
        </div>
    );
}

function Sizes() {
    return (
        <div>
            <div style={style}>
                <TextArea placeholder="Using no layout" />
            </div>
            <div className="layout-small" style={style}>
                <TextArea placeholder="Using layout-small" />
            </div>
            <div className="layout-medium" style={style}>
                <TextArea placeholder="Using layout-medium" />
            </div>
            <div className="layout-large" style={style}>
                <TextArea placeholder="Using layout-large" />
            </div>
        </div>
    );
}

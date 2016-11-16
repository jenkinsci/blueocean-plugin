import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { RadioButtonGroup } from '../components/forms/RadioButtonGroup';

storiesOf('RadioButtonGroup', module)
    .add('vertical', () => <Vertical />)
    .add('horizontal', () => <Horizontal />)
    .add('default value', () => <DefaultOption />)
    .add('label field', () => <LabelField />)
    .add('label function', () => <LabelFunction />)
    .add('callbacks', () => <Callbacks />)
;

const style = {
    padding: 5,
};

function Vertical() {
    return (
        <div style={style}>
            <RadioButtonGroup
                options={['A','B','C']}
            />
        </div>
    );
}

function Horizontal() {
    return (
        <div style={style}>
            <RadioButtonGroup
                className="is-layout-horizontal"
                options={['A','B','C']}
            />
        </div>
    );
}

function DefaultOption() {
    return (
        <div style={style}>
            <RadioButtonGroup
                options={['A','B','C']}
                defaultOption="C"
            />
        </div>
    );
}

function LabelField() {
    const options = [
        { label: 'Foo' },
        { label: 'Bar' },
        { label: 'Baz' },
    ];

    return (
        <div style={style}>
            <RadioButtonGroup
                options={options}
                labelField="label"
            />
        </div>
    );
}

function LabelFunction() {
    const options = [
        { label: 'Foo' },
        { label: 'Bar' },
        { label: 'Baz' },
    ];

    return (
        <div style={style}>
            <RadioButtonGroup
                options={options}
                labelFunction={item => `!${item.label}!`}
            />
        </div>
    );
}

function Callbacks() {
    return (
        <div style={style}>
            <RadioButtonGroup
                options={['A','B','C']}
                onChange={val => console.log('onChange', val)}
            />
        </div>
    );
}

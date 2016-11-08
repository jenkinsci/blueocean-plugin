/**
 * Created by cmeyers on 11/2/16.
 */

/**
 * Created by cmeyers on 11/2/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import RadioGroup from '../components/forms/RadioButtonGroup';

storiesOf('RadioButtonGroup', module)
    .add('vertical', () => <Vertical />)
    .add('horizontal', () => <Horizontal />)
    .add('default value', () => <DefaultOption />)
    .add('label field', () => <LabelField />)
    .add('label function', () => <LabelFunction />)
;

const style = {
    padding: 5,
};

function Vertical() {
    return (
        <div style={style}>
            <RadioGroup
                options={['A','B','C']}
            />
        </div>
    );
}

function Horizontal() {
    return (
        <div style={style}>
            <RadioGroup
                className="horizontal-layout"
                options={['A','B','C']}
            />
        </div>
    );
}

function DefaultOption() {
    return (
        <div style={style}>
            <RadioGroup
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
            <RadioGroup
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
            <RadioGroup
                options={options}
                labelFunction={item => `!${item.label}!`}
            />
        </div>
    );
}

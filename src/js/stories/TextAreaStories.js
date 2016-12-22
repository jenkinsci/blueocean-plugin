import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { TextArea } from '../components/forms/TextArea';

storiesOf('TextArea', module)
    .add('general', () => <General />)
    .add('callbacks', () => <Callbacks />)
    .add('sizes', () => <Sizes />)
;

const style = {padding: 10};

function General() {
    return (
        <div>
            <div style={style}>
                <p>Default</p>

                <TextArea />
            </div>

            <div style={style}>
                <p>Disabled</p>

                <TextArea disabled />
            </div>

            <div style={style}>
                <p>Placeholder</p>

                <TextArea placeholder="This is a placeholder." />
            </div>

            <div style={style}>
                <p>Placeholder</p>

                <TextArea defaultValue="I have a default value." />
            </div>
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
                <TextArea placeholder="Using layout-small" value="five\ntotal\nlines" />
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

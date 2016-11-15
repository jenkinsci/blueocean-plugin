/**
 * Created by cmeyers on 11/14/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Checkbox } from '../components/forms/Checkbox';

storiesOf('Checkbox', module)
    .add('default', () => <Default />)
;

const style = {padding: 5};

function Default() {
    return (
        <div>
            <div className="layout-small" style={style}>
                <Checkbox label="Small" />
            </div>
            <div style={style}>
                <Checkbox label="Medium" />
            </div>
            <div className="layout-large" style={style}>
                <Checkbox label="Large" />
            </div>
            <div style={style}>
                <Checkbox checked label="Checked" />
            </div>
            <div style={style}>
                <Checkbox />
            </div>
        </div>
    );
}

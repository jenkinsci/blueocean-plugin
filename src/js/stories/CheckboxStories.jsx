/**
 * Created by cmeyers on 11/14/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Checkbox } from '../components/forms/Checkbox';

storiesOf('Checkbox', module)
    .add('default', () => <Default />)
;

const style = { display: 'flex', alignItems: 'center', padding: 5 };

function Default() {
    return (
        <div>
            <div style={style}>
                <Checkbox />
                <span style={{marginLeft: 5}}>Unchecked</span>
            </div>
            <div style={style}>
                <Checkbox checked />
                <span style={{marginLeft: 5}}>Checked</span>
            </div>
        </div>
    );
}

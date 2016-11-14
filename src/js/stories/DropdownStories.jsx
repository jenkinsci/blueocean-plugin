/**
 * Created by cmeyers on 11/3/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Dropdown } from '../components';

storiesOf('Dropdown', module)
    .add('default', () => <Dropdown1 />)
;

const style = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: 5,
};

const style2 = {
    display: 'flex',
    width: '100%',
    alignItems: 'center',
    justifyContent: 'space-around',
};

function createOptions(count) {
    const options = [];
    options.push('ABCDEFGHIJKLMNOPQRSTUVWXYZ');
    for (let index = 0; index < count; index++) {
        options.push(index + 1);
    }
    return options;
}


function Dropdown1() {
    return (
        <div style={style}>
            <div style={style2}>
                <button>Test 1</button>

                <select>
                    <option disabled selected>- Select -</option>
                    { createOptions(200).map((option, index) =>
                        <option key={index}>{option}</option>
                    )}
                </select>

                <button>Test 2</button>

                <Dropdown
                    options={createOptions(200)}
                />

                <button>Test 3</button>
            </div>
        </div>
    );
}

import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { FilterableList } from '../components';

storiesOf('FilterableList', module)
    .add('general', () => <General />)
;

const simpleData = ['A', 'B', 'C', 'D', 'EFGHIJKLMNOPQRSTUV', 'W', 'X', 'Y', 'Z'];

const detailedData = [
    { name: 'Ryu', dob: '21-7-1964',  height: '5\'10"', weight: '150 lbs', blood: 'O' },
    { name: 'E. Honda', dob: '3-11-1960',  height: '6\'2"', weight: '304 lbs', blood: 'A' },
    { name: 'Blanka', dob: '12-2-1966',  height: '6\'5"', weight: '218 lbs', blood: 'B' },
    { name: 'Guile', dob: '23-12-1960',  height: '6\'1"', weight: '191 lbs', blood: 'O' },
    { name: 'Ken', dob: '14-2-1966',  height: '5\'10"', weight: '169 lbs', blood: 'B' },
    { name: 'Chun-Li', dob: '1-3-1968',  height: '5\'8"', weight: '-', blood: 'A' },
    { name: 'Zangief', dob: '1-6-1956',  height: '7\'', weight: '256 lbs', blood: 'A' },
    { name: 'Dhalsim', dob: '22-11-1962',  height: '5\'10"', weight: '107 lbs', blood: 'O' },
];


function General() {
    const heading = {
        height: 40,
    };

    const outer = {
        display: 'flex',
        justifyContent: 'space-around',
    };

    const inner = {
        padding: 10,
        width: 300,
    };
    const list = {
        maxHeight: 176,
    };

    return (
        <div style={outer}>
            <div style={inner}>
                <p style={heading}>Simple data</p>

                <FilterableList
                    data={simpleData}
                    listStyle={list}
                />
            </div>
            <div style={inner}>
                <p style={heading}>Simple data, with 'placeholder' and 'emptyText'</p>

                <FilterableList
                    data={simpleData}
                    listStyle={list}
                    placeholder="Suche..."
                    emptyText="Nichts."
                />
            </div>
            <div style={inner}>
                <p style={heading}>Complex data, with labelFunction and filterFunction</p>

                <FilterableList
                    data={detailedData}
                    listStyle={list}
                    labelFunction={item => `${item.name}, ${item.height}, ${item.weight}`}
                    filterFunction={(text, item) => item.name.toLowerCase().indexOf(text) !== -1}
                />
            </div>
        </div>
    );
}

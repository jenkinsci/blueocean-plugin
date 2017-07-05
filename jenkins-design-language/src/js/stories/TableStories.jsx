import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Table } from '../components/Table';

import lipsum from './lipsum.js';

const headers = ['A','B','C'];
const headers2 = [
    { className: 'red', label: 'A' },
    { className: 'blue', label: 'B' },
    { className: 'green', label: 'C' }
];

storiesOf('Table', module)
    .add('Standard', scenario1)
    .add('Hover Highlight', scenario1Hover)
    .add('No Divider', scenario1NoDivider)
    .add('No Default Padding', scenario1NoPadding)
    .add('Long Text', longText)
    .add('Table class', scenario2)
    .add('Header class', scenario3)
    .add('Fluid columns', scenario4)
    .add('Fixed columns', scenario5);


// SHORYUKEN
const detailedHeaders = ['Name', 'DOB', 'Height', 'Weight', 'Blood Type'];
const detailedData = [
    [ 'Ryu',      '21-7-1964',  '5\'10"', '150 lbs', 'O' ],
    [ 'E. Honda', '3-11-1960',  '6\'2"',  '304 lbs', 'A' ],
    [ 'Blanka',   '12-2-1966',  '6\'5"',  '218 lbs', 'B' ],
    [ 'Guile',    '23-12-1960', '6\'1"',  '191 lbs', 'O' ],
    [ 'Ken',      '14-2-1966',  '5\'10"', '169 lbs', 'B' ],
    [ 'Chun-Li',  '1-3-1968',   '5\'8"',  '-',       'A' ],
    [ 'Zangief',  '1-6-1956',   '7\'',    '256 lbs', 'A' ],
    [ 'Dhalsim',  '22-11-1962', '5\'10"', '107 lbs', 'O' ]
];

function renderRow(values) {
    return (
        <tr key={values[0]}>
            { values.map(value => <td key={value}>{value}</td>) }
        </tr>
    );
}

function scenario1() {
    return (
        <Table headers={detailedHeaders}>
            { detailedData.map(renderRow) }
        </Table>
    );
}

function scenario1Hover() {
    return (
        <Table headers={detailedHeaders} className="u-highlight-rows">
            { detailedData.map(renderRow) }
        </Table>
    );
}

function scenario1NoDivider() {
    return (
        <Table disableHeaderDivider headers={detailedHeaders}>
            { detailedData.map(renderRow) }
        </Table>
    );
}

function scenario1NoPadding() {
    return (
        <Table disableDefaultPadding headers={detailedHeaders}>
            { detailedData.map(renderRow) }
        </Table>
    );
}

function longText() {

    const tableStyles = {
        margin: '1em 0',
        boxShadow: '0px 4px 5px 3px rgba(0,0,0,0.21)'
    };

    // Good news, everyone!
    const h = [ "Amy", "Bender", "Calculon", "Donbot" ];

    const lipsum2 = lipsum[0].substr(0,180) + ' blah blah blah.';

    const tbody = (<tbody>
            <tr>
                <td>Elzar</td>
                <td>Fry</td>
                <td>Gypsybot</td>
                <td>
                    <b>ALL HAIL HYPNOTOAD</b>
                </td>
            </tr>
            <tr>
                <td>Inez Wong</td>
                <td>Joey Mousepad</td>
                <td>Kif</td>
                <td>Lrrr</td>
            </tr>
            <tr>
                <td>Morbo</td>
                <td>Nixon</td>
                <td>{lipsum2}</td>
                <td>Mr Panucci</td>
            </tr>
            <tr>
                <td>{lipsum2}</td>
                <td>Roberto</td>
                <td>Scruffy</td>
                <td>Turanga Leela</td>
            </tr>
            <tr>
                <td>URL</td>
                <td>Warden Vogel</td>
                <td>
                    <i>Wernstrom!</i>
                </td>
                <td>Fle<b>x</b>o
                </td>
            </tr>
        </tbody>
    );

    return (
        <div style={{padding: '1em'}}>
            <h3>Normal</h3>
            <Table style={tableStyles} headers={h}>
                {tbody}
            </Table>

            <h3>With disableNoWrap</h3>
            <Table style={tableStyles} headers={h} disableNoWrap>
                {tbody}
            </Table>
        </div>
    );
}

function scenario2() {
    return (
        <Table className="red" headers={headers}>
            <tr>
                <td>1</td>
                <td>2</td>
                <td>3</td>
            </tr>
        </Table>
    );
}

function scenario3() {
    return (
        <Table headers={headers2}>
            <tr>
                <td>1</td>
                <td>2</td>
                <td>3</td>
            </tr>
        </Table>
    );
}

function scenario4() {
    return (
        <Table headers={headers} disableFixed>
            <tr>
                <td>this column has very long text, therefore gets a very long width</td>
                <td>short column 1</td>
                <td>short column 2</td>
            </tr>
        </Table>
    );
}

function scenario5() {
    return (
        <Table headers={headers}>
            <tr>
                <td>this column has very long text, but is still the same size</td>
                <td>short column 1</td>
                <td>short column 2</td>
            </tr>
        </Table>
    );
}

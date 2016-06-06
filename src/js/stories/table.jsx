import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Table } from '../components/Table';

const headers = ['A','B','C'];
const headers2 = [
    { className: 'red', label: 'A' },
    { className: 'blue', label: 'B' },
    { className: 'green', label: 'C' },
];

storiesOf('Table', module)
    .add('standard', scenario1)
    .add('table class', scenario2)
    .add('header class', scenario3)
    .add('fluid columns', scenario4)
    .add('fixed columns', scenario5);

function scenario1() {
    return (
        <Table headers={headers}>
            <tr>
                <td>1</td>
                <td>2</td>
                <td>3</td>
            </tr>
        </Table>
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
        <Table headers={headers}>
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
        <Table className="fixed" headers={headers}>
            <tr>
                <td>this column has very long text, but is still the same size</td>
                <td>short column 1</td>
                <td>short column 2</td>
            </tr>
        </Table>
    );
}

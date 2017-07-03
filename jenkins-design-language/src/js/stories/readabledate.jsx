import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { ReadableDate } from '../components/ReadableDate';
import moment from 'moment';

storiesOf('ReadableDate', module)
    .add('Mixed', scenario2)
    .add('standard, distant', scenario1)
    .add('bad data', scenario3);

function scenario1() {
    return (
        <ReadableDate date="2015-05-24T08:57:06.406+0000" />

    );
}

function scenario2() {
    let aMoment = moment();
    const date1 = aMoment.toISOString();

    aMoment = moment(aMoment).subtract(10,'ms');
    const date2 = aMoment.toISOString();

    aMoment = moment(aMoment).subtract(20,'s');
    const date3 = aMoment.toISOString();

    aMoment = moment(aMoment).subtract(5,'m');
    const date4 = aMoment.toISOString();

    aMoment = moment(aMoment).subtract(2,'h');
    const date5 = aMoment.toISOString();

    aMoment = moment(aMoment).subtract(2,'d');
    const date6 = aMoment.toISOString();

    return (
        <table>
            <thead>
                <tr>
                    <th>Description</th>
                    <th>ISO</th>
                    <th>ReadableDate</th>
                    <th>ReadableDate (liveUpdate)</th>
                </tr>
            </thead>
            <tbody>
            <tr>
                <th>Now()</th>
                <td>{date1}</td>
                <td><ReadableDate date={date1} /></td>
                <td><ReadableDate date={date1} liveUpdate/></td>
            </tr>
            <tr>
                <th>+10 ms</th>
                <td>{date2}</td>
                <td><ReadableDate date={date2} /></td>
                <td><ReadableDate date={date2} liveUpdate/></td>
            </tr>
            <tr>
                <th>+20 s</th>
                <td>{date3}</td>
                <td><ReadableDate date={date3} /></td>
                <td><ReadableDate date={date3} liveUpdate/></td>
            </tr>
            <tr>
                <th>+5 m</th>
                <td>{date4}</td>
                <td><ReadableDate date={date4} /></td>
                <td><ReadableDate date={date4} liveUpdate/></td>
            </tr>
            <tr>
                <th>+2 h</th>
                <td>{date5}</td>
                <td><ReadableDate date={date5} /></td>
                <td><ReadableDate date={date5} liveUpdate/></td>
            </tr>
            <tr>
                <th>+2 d</th>
                <td>{date6}</td>
                <td><ReadableDate date={date6} /></td>
                <td><ReadableDate date={date6} liveUpdate/></td>
            </tr>
            </tbody>
        </table>

    );
}

function scenario3() {
    return (
        <ReadableDate date="bad date" />
    );
}

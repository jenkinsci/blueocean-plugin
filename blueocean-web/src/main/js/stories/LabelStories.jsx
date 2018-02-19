import React from 'react';
import {storiesOf} from '@kadira/storybook';
import {TruncatingLabel} from '../components/TruncatingLabel';
import {hgw} from './data/textdata';

storiesOf('Label', module)
    .add('Basic', basic)
    .add('Broken 1', basic2)
    .add('Null children', empty1)
    .add('Missing children', empty2)
    .add('Blank children', empty3)
    .add('Many', many)
;

function basic() {
    return (
        <div style={{margin: '1em'}}>
            {example(210, 121, hgw)}
        </div>
    );
}

function empty1() {
    return (
        <div style={{margin: '1em'}}>
            {example(200, 200, null)}
        </div>
    );
}

function empty2() {
    return (
        <div style={{margin: '1em'}}>
            {example(200, 200, undefined)}
        </div>
    );
}

function empty3() {
    return (
        <div style={{margin: '1em'}}>
            {example(200, 200, "")}
        </div>
    );
}

function basic2() {

    // This was showing a failure to settle within 50 iterations in the pipeline graph

    const badString = "Das komputermaschine ist nicht auf mittengraben unt die gerfingerpoken. Watchen das blinkenlights.";

    return (
        <div style={{margin: '1em', fontSize: '80%'}}>
            {example(89, 34, badString)}
        </div>
    );
}

function many() {
    return (
        <div style={{margin: '1em'}}>
            {example(100, 100, hgw)}
            <br/>
            {example(180, 100, hgw)}
            <br/>
            {example(200, 100, hgw)}
            <br/>
            {example(50, 180, hgw)}
            <br/>
            {example(80, 80, hgw)}
            <br/>
            {example(180, 180, hgw)}
        </div>
    );
}

function example(width, height, txt) {

    const w = width + 'px';
    const h = height + 'px';

    const outer = { // We'll put the bg on this, because it's got a fixed size instead of max
        width: w,
        height: h,
        background: '#eee'
    };

    const style2 = {
        maxWidth: w,
        maxHeight: h
    };

    return (
        <div style={outer}>
            <TruncatingLabel style={style2}>
                {txt}
            </TruncatingLabel>
        </div>
    );
}

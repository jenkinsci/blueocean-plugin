import React from 'react';
import { storiesOf } from '@kadira/storybook';

import { ResultItem } from '../components';

import lipsum from './lipsum';

storiesOf('ResultItem')
    .add('Basic', basicStory);

const wrapperStyle = {
    padding: '1em'
};

let __ctr = 1;

function moLipsum() {

    const l = lipsum.length;

    return (
        <div>
            <p>{lipsum[(++__ctr) % l]}</p>
            <p>{lipsum[(++__ctr) % l]}</p>
        </div>
    );
}

function onExpand(data) {
    // eslint-disable-next-line
    console.log("Expanding",data);
}

function onCollapse(data) {
    // eslint-disable-next-line
    console.log("Collapsing",data);
}

function basicStory() {

    return (
        <div style={wrapperStyle}>
            <ResultItem result="success" label="Successful Step" durationMillis={11337} onExpand={onExpand}
                        onCollapse={onCollapse} data="bravo">{moLipsum()}</ResultItem>
            <ResultItem result="failure" label="Failed Step" durationMillis={29876} onExpand={onExpand}
                        onCollapse={onCollapse} data="charlie">{moLipsum()}</ResultItem>
            <ResultItem result="queued" label="Pending Step" durationMillis={94567} onExpand={onExpand}
                        onCollapse={onCollapse} data="delta">{moLipsum()}</ResultItem>
            <ResultItem result="aborted" label="Aborted Step" durationMillis={33} onExpand={onExpand}
                        onCollapse={onCollapse} data="echo">{moLipsum()}</ResultItem>
            <ResultItem result="running" label="Running Step, with taller child" durationMillis={12345678}
                        onExpand={onExpand} onCollapse={onCollapse} data="foxtrot">
                <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Consequuntur deserunt dicta impedit nam
                quaerat quasi sapiente sint ullam veniam.</p>
                <p>&nbsp;</p><p>&nbsp;</p><p>&nbsp;</p>
                <p>&nbsp;</p><p>&nbsp;</p><p>&nbsp;</p>
                <p style={{textAlign: "center"}}><i>This space intentionally left blank</i></p>
                <p>&nbsp;</p><p>&nbsp;</p><p>&nbsp;</p>
                <p>&nbsp;</p><p>&nbsp;</p><p>&nbsp;</p>
                <p>Eius enim magnam obcaecati perferendis quam?</p>
            </ResultItem>
            <ResultItem result="unstable" label="Unstable Step" durationMillis={5342345} onExpand={onExpand}
                        onCollapse={onCollapse} data="golf">{moLipsum()}</ResultItem>
            <ResultItem result="not_built" label="Not Built Step - no details"/>
            <ResultItem result="unknown" label="Unknown Step" durationMillis={999999} onExpand={onExpand}
                        onCollapse={onCollapse} data="indigo">{moLipsum()}</ResultItem>
        </div>
    );
}


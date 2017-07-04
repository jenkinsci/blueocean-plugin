import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { List } from '../components';

storiesOf('List', module)
    .add('general', () => <General />)
    .add('renderers', () => <Renderers />)
    .add('keyboard & focus', () => <KeyboardFocus />)
    .add('constraining', () => <Constraining />)
    .add('disabled', () => <Disabled />)
    .add('callbacks', () => <Callbacks />)
;

const simpleData = ['A', 'B', 'C', 'D', 'EFGHIJKLMNOPQRSTUV', 'W', 'X', 'Y', 'Z'];

const container = {
    padding: 10,
    maxWidth: 300,
};

const list = {
    maxHeight: 200,
};


function General() {
    return (
        <div>
            <div style={container}>
                <p>Default w/ max height set</p>

                <List data={simpleData} style={list} />
            </div>

            <div style={container}>
                <p>with defaultStyles=false</p>

                <List data={simpleData} style={list} defaultStyles={false} />
            </div>

            <div style={container}>
                <p>with defaultSelection</p>

                <List data={simpleData} style={list} defaultSelection="C" />
            </div>

            <div style={container}>
                <p>Default w/ no height</p>

                <List data={simpleData} />
            </div>
        </div>
    );
}

// renderers

function Renderer1(props) {
    /* eslint-disable react/prop-types */
    return (
        <div style={{padding: 20, fontSize: 20}}>Large {props.listItem}</div>
    );
    /* eslint-enable react/prop-types */
}


function Renderers() {
    /* eslint-disable react/prop-types */
    return (
        <div>
            <div style={container}>
                <p>inline renderer w/ default styles</p>

                <List data={simpleData} style={list}>
                    {React.createElement((props) => (<div>#{props.listIndex} - {props.listItem}</div>))}
                </List>
            </div>
            <div style={container}>
                <p>custom renderer w/ no styles</p>

                <List data={simpleData} style={list} defaultStyles={false}>
                    <Renderer1 />
                </List>
            </div>
        </div>
    );
    /* eslint-enable react/prop-types */
}

function KeyboardFocus() {
    const buttonStyle = { margin: 10, flexShrink: 0 };
    const outer = {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-around',
        alignItems: 'center',
        padding: 10,
        height: 400,
    };

    return (
        <div style={outer}>
            <p>This layout is useful for demonstrating keyboard accessibility and focus behavior.</p>

            <button style={buttonStyle}>Test 1</button>

            <List data={simpleData} style={{...list, maxWidth: 300}} />

            <button style={buttonStyle}>Test 2</button>
        </div>
    );
}

const WIDTH = 200;
const HEIGHT = 250;

function Constraining() {
    const outer = { display: 'flex' };
    const inner = { width: WIDTH + 50, padding: 10 };
    const title = { height: 50 };

    const constrain = {maxWidth: WIDTH, maxHeight: HEIGHT};
    const explicit = {width: WIDTH, height: HEIGHT};


    return (
        <div>
            <div style={outer}>
                <div style={inner}>
                    <p style={title}>width / height directly on List</p>

                    <List data={simpleData} style={explicit} />
                </div>

                <div style={inner}>
                    <p style={title}>maxWidth / maxHeight directly on List</p>

                    <List data={simpleData} style={constrain} />
                </div>

                <div style={inner}>
                    <p style={title}>List anchored to parent via absolute positioning</p>

                    <div style={{...constrain, height: HEIGHT, position: 'relative'}}>
                        <List data={simpleData} style={{position: 'absolute', top: 0, bottom: 0}} />
                    </div>
                </div>

                <div style={inner}>
                    <p style={title}>maxWidth / maxHeight on parent container (flexbox)</p>

                    <div style={{...constrain, display: 'flex'}}>
                        <List data={simpleData} />
                    </div>
                </div>
            </div>
            <div style={outer}>
                <div style={inner}>
                    <p style={title}>maxWidth / maxHeight on parent container (not flexbox)</p>

                    <div style={constrain}>
                        <List data={simpleData} />
                    </div>
                </div>
            </div>
        </div>
    );
}

function Disabled() {
    return (
        <div>
            <div style={container}>
                <p>props.disabled = true</p>

                <List data={simpleData} style={list} disabled />
            </div>
            <div style={container}>
                <p>nested in fieldset.disabled=true</p>

                <fieldset disabled="disabled">
                    <List data={simpleData} style={list} />
                </fieldset>
            </div>
        </div>
    );
}


function Callbacks() {
    return (
        <div style={container}>
            <p>onItemSelect</p>

            <List
                data={simpleData}
                style={list}
                onItemSelect={(index, item) => console.log(index, item)}
            />
        </div>
    );
}

import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { List } from '../components';

storiesOf('List', module)
    .add('general', () => <General />)
    .add('renderers', () => <RendererOptions />)
    .add('keyboard & focus', () => <KeyboardFocus />)
    .add('callbacks', () => <Callbacks />)
;

const style = {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    padding: 10,
};

const simpleData = ['A', 'B', 'C', 'DEFGHIJKLMNOPQRSTUVW', 'X', 'Y', 'Z'];


function General() {
    const style = {
        padding: 10,
    };

    return (
        <div>
            <div style={style}>
                <p>Default</p>

                <List data={simpleData} />
            </div>

            <div style={style}>
                <p>No Default Styles</p>

                <List data={simpleData} defaultStyles={false} />
            </div>

            <div style={style}>
                <p>Default Value</p>

                <List data={simpleData} defaultSelection="C" />
            </div>

            <div style={style}>
                <p>Truncation</p>

                <List data={simpleData} style={{maxWidth: 150, maxHeight: 150}} />
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


function RendererOptions() {
    const style = {
        display: 'flex',
        justifyContent: 'space-around',
    };

    /* eslint-disable react/prop-types */
    return (
        <div style={style}>
            <div>
                <p>inline renderer w/ default styles</p>

                <List data={simpleData}>
                    {React.createElement((props) => (<div>#{props.listIndex} - {props.listItem}</div>))}
                </List>
            </div>
            <div>
                <p>custom renderer w/ no styles</p>

                <List data={simpleData} defaultStyles={false}>
                    <Renderer1 />
                </List>
            </div>
        </div>
    );
    /* eslint-enable react/prop-types */
}

function KeyboardFocus() {
    const buttonStyle = { margin: 10, flexShrink: 0 };

    return (
        <div style={{...style, height: 300}}>
            <p>This Layout is useful for demonstrating keyboard accessibility and focus behavior.</p>

            <button style={buttonStyle}>Test 1</button>

            <List data={simpleData} />

            <button style={buttonStyle}>Test 2</button>
        </div>
    );
}

function Callbacks() {
    return (
        <div style={style}>
            <p>onItemSelect</p>

            <List data={simpleData} onItemSelect={(index, item) => console.log(index, item)} />
        </div>
    );
}

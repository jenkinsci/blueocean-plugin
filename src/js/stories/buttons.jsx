import React from 'react';
import { storiesOf } from '@kadira/storybook';

storiesOf('Button', module)
    .add('default (light bg)', buttonsLightBg)
    .add('inverse (dark bg)', buttonsDarkBg);

const container = {
    display: 'flex',
    justifyContent: 'center',
    width: '800px',
    height: '150px',
    padding: '10px',
};

const buttons = {
    alignItems: 'center',
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    justifyContent: 'space-between',
};

function buttonsLightBg() {
    return (
        <div style={container}>
            <div style={buttons}>
                <button>Default Button</button>
                <a>Default Anchor</a>
                <button disabled="disabled">Default Button Disabled</button>
            </div>
            <div style={buttons}>
                <button className="btn-primary">Primary Button</button>
                <a className="btn-primary">Primary Anchor</a>
                <button className="btn-primary" disabled="disabled">Primary Button Disabled</button>
            </div>
            <div style={buttons}>
                <button className="btn-secondary">Secondary Button</button>
                <a className="btn-secondary">Secondary Anchor</a>
                <button className="btn-secondary" disabled="disabled">Secondary Button Disabled</button>
            </div>
        </div>
    );
}

function buttonsDarkBg() {
    const containerDark = {
        ... container,
        backgroundColor: '#4A90E2',
    };

    return (
        <div style={containerDark}>
            <div style={buttons}>
                <button className="inverse">Default Button</button>
                <a className="inverse">Default Anchor</a>
                <button className="inverse" disabled="disabled">Default Button Disabled</button>
            </div>
            <div style={buttons}>
                <button className="btn-primary inverse">Primary Button</button>
                <a className="btn-primary inverse">Primary Anchor</a>
                <button className="btn-primary inverse" disabled="disabled">Primary Button Disabled</button>
            </div>
            <div style={buttons}>
                <button className="btn-secondary inverse">Secondary Button</button>
                <a className="btn-secondary inverse">Secondary Anchor</a>
                <button className="btn-secondary inverse" disabled="disabled">Secondary Button Disabled</button>
            </div>
        </div>
    );
}

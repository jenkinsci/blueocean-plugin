import React from 'react';
import { storiesOf } from '@kadira/storybook';

import { CustomIcons } from './IconButtonStoryHelpers';

import { Icon } from '../components/Icon';
import * as IconId from '../components/material-ui/svg-icons';

/* eslint-disable max-len, react/self-closing-comp */

storiesOf('Icon', module)
    .add('all icons', AllIcons)
    .add('vertical positioning', VerticalPositioning)
    .add('sizing', Sizing)
;

const style = {
    padding: 10,
};

function AllIcons() {
    return (
        <div>
            <div style={style}> 
                <h1>Material-UI-Icons</h1>

                <div style={{ display: 'flex', flexFlow: 'row wrap', marginTop: '20px' }}>
                    { Object.keys(IconId).sort().map(shape => (
                        <div key={shape} style={{marginTop: '20px', textAlign: 'center', width: '19%'}}>
                            <Icon icon={shape} color="#4A90E2" />
                            <div>{shape}</div>
                        </div>
                    ) )}
                </div>
            </div>
        </div>
    );
}

function VerticalPositioning() {
    return (
        <div>
            <div style={style}> 
                <h1>Material-UI-Icons</h1>

                <p style={{ marginTop: '40px' }}>
                    Vertical Align Middle
                </p>

                <div style={{ display: 'flex', flexFlow: 'row wrap' }}>
                    { Object.keys(IconId).slice(50, 75).sort().map(shape => (
                        <div key={shape} style={{marginTop: '20px', width: '19%'}}>
                            <Icon icon={shape} color="#4A90E2" />
                            <span> {shape}</span>
                        </div>
                    ) )}
                </div>

                <p style={{ marginTop: '40px' }}>
                    Vertical Align Bottom
                </p>
                <div style={{ display: 'flex', flexFlow: 'row wrap' }}>
                    { Object.keys(IconId).slice(50, 75).sort().map(shape => (
                        <div key={shape} style={{marginTop: '20px', width: '19%'}}>
                            <Icon icon={shape} color="#4A90E2" style={{verticalAlign: 'bottom'}} />
                            <span> {shape}</span>
                        </div>
                    ) )}
                </div>
                
            </div>
        </div>
    );
}

const buttonRow = {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10
};

function Sizing() {
    const cellStyle = {...buttonRow, marginBottom: 0};

    return (
        <div style={style}>
            { Object.keys(IconId).slice(50, 60).sort().map(shape => (
            <div key={shape} style={buttonRow}>
                <div className="layout-small" style={cellStyle}>
                    Small &nbsp;
                    <Icon icon={shape} color="#4A90E2" label="Small" />
                </div>
                <div style={cellStyle}>
                    Medium &nbsp;
                    <Icon icon={shape} color="#4A90E2" label="Medium" />
                </div>
                <div className="layout-large" style={cellStyle}>
                    Large &nbsp;
                    <Icon icon={shape} color="#4A90E2" label="Large" />
                </div>
            </div>
            )) }
        </div>
    );
}

//@flow

import React, {Component} from 'react';
import { storiesOf } from '@kadira/storybook';

import {
    BasicDialog,
    DialogHeader,
    DialogContent,
    DialogButtonBar,
    Dialog
} from '../components';

storiesOf('Dialog', module)
    .add('Easy', renderEasy)
    .add('Easy - No Title', renderEasyNoTitle)
    .add('Easy - No Buttons', renderEasyNoButtons)
    .add('Input', () => renderWithClassName('Dialog--input'))
    .add('Error', () => renderWithClassName('Dialog--error'))
    .add('Success', () => renderWithClassName('Dialog--success'))
    .add('Warning', () => renderWithClassName('Dialog--warning'))
    .add('Barebones', renderBarebones)
    .add('Artisanal', renderArtisanal)
;

//--------------------------------------
//  Stories
//--------------------------------------

// Render with a dialog class name
function renderWithClassName(className) {

    return (
        <div>
            <p>Background page.</p>

            <Dialog onDismiss={() => console.log('User dismiss')}
                    title={'ClassName ' + className}
                    className={className}>
                <p>Voltage and magnetism form a pair of twins; they are two halves of a duality.</p>
            </Dialog>
        </div>
    );
}

// Easy-to-use (least flexible) dialog component example
function renderEasy() {

    const buttons = [
        <button>C'est ne Pas</button>,
        <button>Une Button</button>
    ];

    return (
        <div>
            <p>Background page.</p>

            <Dialog onDismiss={() => console.log('User dismiss')}
                    title="Easy to Use Dialog"
                    buttons={buttons}>
                <p>Designed for the common case, to make life easy.</p>
                <ToggleWide/>
                <ToggleTall/>
            </Dialog>
        </div>
    );
}

// Easy-to-use (least flexible) dialog component example - no title set
function renderEasyNoTitle() {

    const buttons = <button>Do Stuff</button>;

    return (
        <div>
            <p>Background page.</p>

            <Dialog onDismiss={() => console.log('User dismiss')}
                    buttons={buttons}>
                <p>This dialog was not given a title.</p>
                <ToggleWide/>
                <ToggleTall/>
            </Dialog>
        </div>
    );
}

// Easy-to-use (least flexible) dialog component example - no buttons provided
function renderEasyNoButtons() {

    return (
        <div>
            <p>Background page.</p>

            <Dialog onDismiss={() => console.log('User dismiss')}
                    title="Default Button Bar">
                <p>Demonstrates default close button when no buttons specified.</p>
            </Dialog>
        </div>
    );
}

// Basic "shell" of a dialog
function renderBarebones() {
    return (
        <div>
            <p>Background page.</p>

            <BasicDialog onDismiss={() => console.log('User dismiss')}>
                <h3>This is a barebones dialog</h3>
                <p>It doesn't do a lot for you, but you can put whatever you like in it.</p>
                <ToggleWide/>
                <ToggleTall/>
            </BasicDialog>
        </div>
    );
}

// Regular old dialog, only hand-crafted with love and moonbeams
function renderArtisanal() {
    return (
        <div>
            <p>Background page.</p>

            <BasicDialog onDismiss={() => console.log('User dismiss')}>
                <DialogHeader>Bespoke Artisanal Dialog</DialogHeader>
                <DialogContent>
                    <p>You can also manually construct a dialog out of all the various helper components. Exciting.</p>
                    <ToggleWide/>
                    <ToggleTall/>
                </DialogContent>
                <DialogButtonBar>
                    <button>Button</button>
                    <button>Bar</button>
                    <button>Buttons</button>
                </DialogButtonBar>
            </BasicDialog>
        </div>
    );
}

//--------------------------------------
//  Helpers
//--------------------------------------

class ToggleWide extends Component {
    state: {active:bool};

    render() {
        const {active = false} = this.state || {};
        const expando = active && (
                <p>
                    Lorem ipsum dolor sit amet, consectetur adipisicing elit. Atque autem consequatur delectus dolorum
                    eaque eius illo, ipsa itaque nam nesciunt odio, officiis provident, tempore unde veniam vitae
                    voluptatibus. Architecto, ea?
                </p>
            );

        return (
            <div style={{margin: '0.5em 0'}}>
                <button onClick={()=>this.setState({active: !active})}>Toggle Wide</button>
                {expando}
            </div>
        );
    }
}

class ToggleTall extends Component {
    state: {active:bool};

    render() {
        const tallStyle = {
            padding: '1rem',
            height: '80rem',
            border: 'dashed 2px #ddd'
        };
        const {active = false} = this.state || {};
        const expando = active && (
                <p style={tallStyle}>
                    Tall
                </p>
            );

        return (
            <div style={{margin: '0.5em 0'}}>
                <button onClick={()=>this.setState({active: !active})}>Toggle Tall</button>
                {expando}
            </div>
        );
    }
}

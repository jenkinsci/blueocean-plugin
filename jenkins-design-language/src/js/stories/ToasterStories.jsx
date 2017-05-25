/**
 * Created by cmeyers on 8/17/16.
 */
import React, { Component, PropTypes } from 'react';
import { storiesOf } from '@kadira/storybook';

import { Toaster } from '../components/Toaster';

class ToasterTester extends Component {

    constructor() {
        super();
        this.toasts = [];
        this.counter = 0;
    }

    componentWillMount() {
        if (this.props.toasts) {
            this.toasts = this.toasts.concat(this.props.toasts);
        }
    }

    _addToast() {
        const id = Math.random() * Math.pow(10,16);
        const counter = this.counter++;

        this.toasts.push({
            id: id,
            text: `Hello World Hello World Hello World Hello World ${counter}`,
            action: 'Action',
        });
        this.forceUpdate();
    }

    _onActionClick(toast) {
        const index = this.toasts.indexOf(toast);
        this.toasts.splice(index, 1);
        this.forceUpdate();
    }

    _onDismiss(toast) {
        const index = this.toasts.indexOf(toast);

        if (index >= 0) {
            this.toasts.splice(index, 1);
            this.forceUpdate();
        }
    }

    render() {
        const containerStyle = { position: 'absolute', bottom: 0, minWidth: 400, height: 250 };
        const toasterStyle = { position: 'absolute', top: 0, bottom: 50, width: 400, backgroundColor: '#eee' };
        const buttonStyle = { position: 'absolute', bottom: 0 };

        return (
            <div style={containerStyle}>
                <div style={toasterStyle}>
                    <Toaster
                        toasts={this.toasts}
                        onActionClick={(toast) => this._onActionClick(toast)}
                        onDismiss={(toast) => this._onDismiss(toast)}
                        dismissDelay={this.props.dismissDelay}
                    />
                </div>

                <button
                    onClick={() => this._addToast()}
                    style={buttonStyle}
                >
                    Add Toast
                </button>
            </div>
        );
    }
}

ToasterTester.propTypes = {
    toasts: PropTypes.array,
    dismissDelay: PropTypes.number,
};

storiesOf('Toaster', module)
    .add('default', () => {
        return (
            <ToasterTester />
        );
    })
    .add('with 1 toast', () => {
        const toasts = [
            {
                id: Math.random() * Math.pow(10,16),
                text: 'Hello World',
                action: 'Toast!',
                dismissDelay: 10000
            }
        ];

        return (
            <ToasterTester toasts={toasts} />
        );
    })
    .add('with caption and error', () => {
        const toasts = [
            {
                id: Math.random() * Math.pow(10,16),
                style: 'error',
                caption: 'Favorites Error',
                text: 'Failed to register favorite'
            }
        ];

        return (
            <ToasterTester toasts={toasts} />
        );
    })
;

// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';

type Props = {
    children: ReactChildren,
    className: string,
    checked: boolean,
    label: string,
    onToggle: (checked: boolean) => void,
}

export class Checkbox extends Component {

    state: {
        checked: boolean
    };

    static defaultProps = {
        className: '',
        checked: false,
    };

    get checked():boolean {
        return this.state.checked;
    }

    componentWillMount() {
        this._updateState(this.props);
    }

    componentWillReceiveProps(props: Props) {
        this._updateState(props);
    }

    _updateState(props: Props) {
        this.setState({
            checked: props.checked
        });
    }

    _toggle(e: Event) {
        if (e.target instanceof HTMLInputElement) {
            const checked = e.target.checked;

            this.setState({
                checked: checked
            });

            if (this.props.onToggle) {
                this.props.onToggle(checked);
            }
        }
    }

    render() {
        const extraClass = this.props.className || '';

        return (
            <div className={`Checkbox ${extraClass}`}>
                <label className="Checkbox-wrapper"
                       onClick={(event) => event.stopPropagation()}
                >
                    <input type="checkbox"
                           { ...{ name: this.props.name } }
                           onChange={this._toggle.bind(this)}
                           checked={this.state.checked}
                           disabled={this.props.disabled}
                    />

                    <div className="Checkbox-indicator">
                        { !this.props.children ?
                            <Icon icon="check" /> :
                            this.props.children
                        }
                    </div>

                    { this.props.label &&
                        <div className="Checkbox-text">{this.props.label}</div>
                    }
                </label>
            </div>
        );
    }
}

Checkbox.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    label: PropTypes.string,
    name: PropTypes.string,
    checked: PropTypes.bool,
    disabled: PropTypes.bool,
    onToggle: PropTypes.func
};

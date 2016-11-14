// @flow

import React, { Component, PropTypes } from 'react';
import { Icon } from 'react-material-icons-blue';

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

    toggle(e: Event) {
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
        const checkecClass = this.state.checked ? 'checked' : '';

        return (
            <label className={`Checkbox ${extraClass} ${checkecClass}`}
              onClick={(event) => event.stopPropagation()}
            >
                <input type="checkbox"
                       onChange={this.toggle.bind(this)}
                       checked={this.state.checked} />

                <Icon icon="check" />
            </label>
        );
    }
}

Checkbox.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    checked: PropTypes.bool,
    onToggle: PropTypes.func
};

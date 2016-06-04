// @flow

import React, { Component, PropTypes } from 'react';

type Props = {
    darkTheme: boolean,
    checked: boolean,
    onToggle: (checked: boolean) => void
}

export class Favorite extends Component {

    state: {
        checked: boolean
    };

    static defaultProps = {
        darkTheme: false,
        checked: false
    };

    constructor(props: Props){
        super(props);
        this.state = {
            checked: this.props.checked
        };
    }

    toggle(e: Event) {
        if (e.target instanceof HTMLInputElement) {
            const checked = e.target.checked;
            this.setState({
                checked: checked
            });

            if (this.props.onToggle != null){
                this.props.onToggle(checked);
            }
        }
    }

    render() {
        let classes = "checkbox";

        if (this.props.darkTheme) {
            classes += " dark";
        }

        return (
            <label className={classes}>
                <input type="checkbox"
                       onChange={this.toggle.bind(this)}
                       checked={this.state.checked} />
                <span></span>
            </label>
        );
    }
}

Favorite.propTypes = {
    darkTheme: PropTypes.bool,
    checked: PropTypes.bool,
    onToggle: PropTypes.func
};

import React, { Component, PropTypes } from 'react';

export class Favorite extends Component {
    constructor(props){
        super(props);
        this.state = {
            checked: this.props.checked
        };
    }
    toggle(e) {
        let checked = e.target.checked;
        this.setState({
            checked: checked
        });

        if (this.props.onToggle != null){
            this.props.onToggle(checked);
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

Favorite.defaultProps = {
    darkTheme: false,
    checked: false
};

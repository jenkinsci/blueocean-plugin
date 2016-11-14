/**
 * Created by cmeyers on 11/1/16.
 */
import React, { PropTypes } from 'react';

export class TextInput extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            value: '',
        };
    }

    componentWillMount() {
        this._initialize(this.props);
    }

    get value() {
        return this.state.value;
    }

    _initialize(props) {
        if (props.defaultValue) {
            this.setState({
                value: props.defaultValue,
            });
        }
    }

    _onChange(event) {
        const { value } = event.currentTarget;

        this.setState({
            value,
        });

        if (this.props.onChange) {
            this.props.onChange(value);
        }
    }

    _onBlur() {
        if (this.props.onBlur) {
            this.props.onBlur();
        }
    }

    render() {
        const extraClass = this.props.className || '';

        return (
            <div className={`text-input ${extraClass}`}>
                <input
                  className="text-field"
                  type="text"
                  placeholder={this.props.placeholder}
                  value={this.state.value}
                  onChange={e => this._onChange(e)}
                  onBlur={() => this._onBlur()}
                />
            </div>
        );
    }

}

TextInput.propTypes = {
    className: PropTypes.string,
    placeholder: PropTypes.string,
    defaultValue: PropTypes.string,
    onChange: PropTypes.func,
    onBlur: PropTypes.func,
};

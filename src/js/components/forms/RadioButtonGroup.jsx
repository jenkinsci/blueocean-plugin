import React, { PropTypes } from 'react';
import Utils from '../../Utils';

/**
 * Renders a group of radio buttons for the provided 'options'
 * By default the radio buttons are arranged vertically
 *
 * Props:
 *      className: additional class name(s) to add to root element.
 *          Use 'is-layout-horizontal' to change layout direction.
 *      options: an array of options to map to radio buttons. Can be strings or objects.
 *      defaultOption: option to select/check by default if no selection was already made by user.
 *      labelField: if using objects for options, name of property to use for label.
 *      labelFunction: a function that receives the option and returns a string for label.
 *      onChange: handler func calls when user changes selection, receiving 'option' as only arg.
 */
export class RadioButtonGroup extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            selectedOption: null,
        };

        this.groupId = () => { return Utils.randomId('RadioButtonGroup'); };
    }

    componentWillMount() {
        this._defaultSelection(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._defaultSelection(nextProps);
    }

    get selectedOption() {
        return this.state.selectedOption;
    }

    _defaultSelection(props) {
        if (!this.state.selectedOption && props.defaultOption) {
            this.setState({
                selectedOption: props.defaultOption,
            });
        }
    }

    _onChange(option) {
        this.setState({
            selectedOption: option,
        });

        if (this.props.onChange) {
            this.props.onChange(option);
        }
    }

    render() {
        const groupId = this.groupId();

        return (
            <div className={`RadioButtonGroup ${this.props.className}`}>
                { this.props.options.map((option, index) => {
                    const checked = option === this.state.selectedOption;

                    let labelValue = '';

                    if (this.props.labelField) {
                        labelValue = option[this.props.labelField];
                    } else if (this.props.labelFunction) {
                        labelValue = this.props.labelFunction(option);
                    } else {
                        labelValue = option.toString();
                    }

                    return (
                        <label
                          key={index}
                          className="RadioButtonGroup-item"
                        >
                            <input
                              className="RadioButtonGroup-button"
                              name={groupId}
                              type="radio"
                              checked={checked}
                              disabled={this.props.disabled}
                              onChange={() => this._onChange(option)}
                            />

                            <Indicator />

                            <span className="RadioButtonGroup-text">{labelValue}</span>
                        </label>
                    );
                })}
            </div>
        );
    }

}

RadioButtonGroup.propTypes = {
    className: PropTypes.string,
    options: PropTypes.array,
    defaultOption: PropTypes.any,
    labelField: PropTypes.string,
    labelFunction: PropTypes.func,
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
};

RadioButtonGroup.defaultProps = {
    className: '',
};

function Indicator() {
    return (
        <div className="RadioButtonGroup-indicator">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
                width="100%" height="100%" className="svg-shape" focusable="false"
            >
                <g>
                    <circle className="outer-circle" cx="10" cy="10" r="9.5" stroke="black" strokeWidth="1" fill="none"
                            shapeRendering="geometricPrecision"/>
                    <circle className="inner-circle" cx="10" cy="10" r="3" fill="white"
                            shapeRendering="geometricPrecision"/>
                </g>
            </svg>
            <div className="RadioButtonGroup-focus" />
        </div>
    );
}

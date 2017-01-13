import React from 'react';

export default class IntegerPropertyInput extends React.Component {
    render() {
        return (
            <div>
                <label className="form-label">{this.props.type.capitalizedName}</label>
                <div className="TextInput">
                    <input type="number" className="TextInput-control" defaultValue={this.props.step.data[this.props.propName]}
                        onChange={e => { this.props.step.data[this.props.propName] = e.target.value; this.props.onChange(this.props.step); }}/>
                </div>
            </div>
        );
    }
}

IntegerPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
};

IntegerPropertyInput.dataTypes = [ 'byte', 'short', 'int', 'long', 'java.lang.Byte', 'java.lang.Short', 'java.lang.Integer', 'java.lang.Long', 'java.math.BigInterger' ];

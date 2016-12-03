import React from 'react';

export default class IntegerPropertyInput extends React.Component {
    render() {
        return (
            <div>
                <input type="number" onChange={e => this.props.step[this.props.propName] = e.target.value}/>
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

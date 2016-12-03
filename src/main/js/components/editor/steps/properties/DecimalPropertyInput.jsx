import React from 'react';

export default class DecimalPropertyInput extends React.Component {
    render() {
        return (
            <div>
                <input type="number" onChange={e => this.props.step[this.props.propName] = e.target.value}/>
            </div>
        );
    }
}

DecimalPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
};

DecimalPropertyInput.dataTypes = [ 'float', 'double', 'java.lang.Float', 'java.lang.Double', 'java.math.BigDecimal' ];

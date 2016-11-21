import React from 'react';

export default class BooleanPropertyInput extends React.Component {
    render() {
        return <div>
            <input onChange={e => this.props.step[this.props.propName] = e.target.value}/>
        </div>;
    }
}

BooleanPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
}

BooleanPropertyInput.dataTypes = [ 'boolean', 'java.lang.Boolean' ];

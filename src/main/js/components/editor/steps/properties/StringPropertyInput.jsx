import React from 'react';

export default class StringPropertyInput extends React.Component {
    render() {
        return <div>
            <input defaultValue={this.props.step.data[this.props.propName]} onChange={e => { this.props.step.data[this.props.propName] = e.target.value; this.props.onChange(this.props.step) }}/>
        </div>;
    }
}

StringPropertyInput.propTypes = {
    propName: React.PropTypes.string,
    step: React.PropTypes.any,
    onChange: React.PropTypes.func,
}

StringPropertyInput.dataTypes = [ 'java.lang.String' ];

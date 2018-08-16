import React, { Component } from 'react';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';

export class Hidden extends Component {
    render() {
        const { defaultParameterValue: { value }, name } = this.props;
        const cleanName = removeMarkupTags(name);

        return (
            <div className="Hidden FullWidth">
                <input type="hidden" name={cleanName} value={value}/>
            </div>
        );
    }
}

Hidden.propTypes = propTypes;

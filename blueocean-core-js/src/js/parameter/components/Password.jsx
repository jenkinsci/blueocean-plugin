import React, { Component } from 'react';
import { FormElement, PasswordInput } from '@jenkins-cd/design-language';
import { propTypes } from '../commonProptypes';
import { removeMarkupTags } from '../../stringUtil';
// import { DebugRender } from './DebugRender';

export class Password extends Component {

    render() {
        // const debugging = React.createElement(DebugRender, this.props);
        // FIXME: defaultValue does not contain the value we wait for
        const { defaultParameterValue: { value }, description, name, onChange } = this.props;
        const cleanName = removeMarkupTags(name);
        return (<FormElement title={ cleanName }>
            <div className="Password">
                <PasswordInput {...{ defaultValue: value, cleanName, onChange }} />
                { description && <div className="inputDescription">{removeMarkupTags(description)}</div> }
            </div>
            {/* { debugging }*/}
        </FormElement>);
    }
}

Password.propTypes = propTypes;

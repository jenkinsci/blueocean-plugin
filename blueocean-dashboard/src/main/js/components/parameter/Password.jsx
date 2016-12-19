import React, { Component } from 'react';
import { FormElement, PasswordInput } from '@jenkins-cd/design-language';
import { propTypes } from './commonProptypes';
// import { DebugRender } from './DebugRender';

export class Password extends Component {

    render() {
        // const debugging = React.createElement(DebugRender, this.props);
        // FIXME: defaultValue does not contain the value we wait for
        const { defaultParameterValue: { value }, description, name } = this.props;
        return (<FormElement title={ name }>
            <div>
                <PasswordInput defaultValue={value} />
                { description && <div>{description}</div> }
            </div>
            {/* { debugging }*/}
        </FormElement>);
    }
}

Password.propTypes = propTypes;

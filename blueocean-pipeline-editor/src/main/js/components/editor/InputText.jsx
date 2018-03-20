// @flow
import React from 'react';

function generateClassNames(...args) {
    return args.filter(arg => !!arg).join(' ');
}

export default function InputText({ defaultValue, onChange, isRequired, hasError, onBlur }) {
    return (
        <div className={generateClassNames('FormElement', hasError && 'u-error-state', isRequired && 'required')}>
            <div className="FormElement-children">
                <div className="TextInput">
                    <input
                        type="text"
                        className="TextInput-control"
                        defaultValue={defaultValue}
                        onChange={e => onChange(e.target.value)}
                        onBlur={e => onBlur && onBlur()}
                    />
                </div>
            </div>
        </div>
    );
}

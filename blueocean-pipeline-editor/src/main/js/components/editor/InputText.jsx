// @flow
import React from 'react';

function cn(...args) {
    let out = null;
    for (let i = 0; i < args.length; i++) {
        if (args[i]) {
            if (!out) {
                out = args[i];
            } else {
                out += ' ' + args[i];
            }
        }
    }
    return out;
}

export default function InputText({ defaultValue, onChange, isRequired, hasError, onBlur }) {
    return (
        <div className={cn('FormElement', hasError && 'u-error-state', isRequired && 'required')}>
            <div className="FormElement-children">
                <div className="TextInput">
                    <input type="text" className="TextInput-control"
                        defaultValue={defaultValue}
                        onChange={e => onChange(e.target.value)}
                        onBlur={e => onBlur && onBlur()} />
                </div>
            </div>
        </div>
    );
}

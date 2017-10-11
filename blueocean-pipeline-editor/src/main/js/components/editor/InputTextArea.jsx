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

function autoResizeHeight() {
    event.target.style.height = 'auto';

    if (event.target.scrollHeight > event.target.offsetHeight) {
        event.target.style.height = (event.target.scrollHeight + 4) + 'px';
    } else {
        event.target.style.height = 'auto';
    }
}

export default function InputText({ defaultValue, onChange, isRequired, hasError, onBlur }) {
    return (
        <div className={cn('FormElement', hasError && 'u-error-state', isRequired && 'required')}>
            <div className="FormElement-children">
                <div className="TextArea">
                    <textarea className="TextArea-control"
                        defaultValue={defaultValue}
                        onInput={() => autoResizeHeight()}
                        onChange={e => onChange(e.target.value)}
                        onBlur={e => onBlur && onBlur()} ></textarea>
                </div>
            </div>
        </div>
    );
}

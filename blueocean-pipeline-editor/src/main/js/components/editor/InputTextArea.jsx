import React from 'react';

function generateClassNames(...args) {
    return args.filter(arg => !!arg).join(' ');
}

function autoResizeHeight() {
    event.target.style.height = 'auto';

    if (event.target.scrollHeight > event.target.offsetHeight) {
        event.target.style.height = event.target.scrollHeight + 4 + 'px';
    } else {
        event.target.style.height = 'auto';
    }
}

export default function InputTextArea({ defaultValue, onChange, isRequired, hasError, onBlur }) {
    return (
        <div className={generateClassNames('FormElement', hasError && 'u-error-state', isRequired && 'required')}>
            <div className="FormElement-children">
                <div className="TextArea">
                    <textarea
                        className="TextArea-control"
                        defaultValue={defaultValue}
                        onInput={() => autoResizeHeight()}
                        onChange={e => onChange(e.target.value)}
                        onBlur={e => onBlur && onBlur()}
                    />
                </div>
            </div>
        </div>
    );
}

// @flow

import React from 'react';
import InputText from "./InputText";

export function Split({children, className}) {
    if (!children) {
        return null;
    }
    return (<div className="split">
        {React.Children.map(children, child => <div className={`split-child ${className}`}>
            {child}
        </div>)}
    </div>);
}

Split.propTypes = {
    children: React.PropTypes.any,
    className: React.PropTypes.any,
};

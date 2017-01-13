// @flow

import React from 'react';

export function Split({children}) {
    if (!children) {
        return null;
    }
    return (<div className="split">
        {React.Children.map(children, child => <div className="split-child">
            {child}
        </div>)}
    </div>);
}

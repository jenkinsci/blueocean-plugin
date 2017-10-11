// @flow

import React, { PropTypes } from 'react';

type Props = {
    className?: string,
    style?: Object,
    children?: ReactChildren,
};

export const TopNav = (props: Props) => {
    const style = props.style || {};
    const classNames = ['Header-topNav'];

    if (props.className) {
        classNames.push(props.className);
    }

    return (
        <div className={classNames.join(' ')} style={style}>
            <div className="Header-topNav-inner">{props.children}</div>
        </div>
    );
};

TopNav.propTypes = {
    children: PropTypes.node,
};

// @flow

import React, { PropTypes } from 'react';

type Props = {
    className?: string,
    style?: Object,
    children?: ReactChildren
}

export const HeaderDetails = (props: Props) => {

    const style = props.style || {};
    const classNames = ["Header-details"];

    if (props.className) {
        classNames.push(props.className);
    }

    return (
        <div className={classNames.join(' ')} style={style}>
            {props.children}
        </div>
    );
};

HeaderDetails.propTypes = {
    children: PropTypes.node
};

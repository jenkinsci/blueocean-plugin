import React, { PropTypes } from 'react';
import * as IconId from './material-ui/svg-icons';

export const Icon = props => {
    const Icon = IconId[props.icon];

    return (
        <Icon {...props} />
    );
};

Icon.propTypes = {
    children: PropTypes.element,
    className: PropTypes.string,
    color: PropTypes.string,
    hoverColor: PropTypes.string,
    style: PropTypes.object,
    label: PropTypes.string,
    icon: PropTypes.string,
    size: PropTypes.number,
};

Icon.defaultProps = {
    icon: 'ContentClear',
};

import React, { PropTypes } from 'react';
import * as IconId from './material-ui/svg-icons';

export function Icon(props) {
    const ChildIcon = IconId[props.icon];

    if (!ChildIcon) {
        return null;
    }

    return (
        <ChildIcon {...props} />
    );
}

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

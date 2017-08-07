import React, { PropTypes } from 'react';
import * as IconId from './material-ui/svg-icons';

type Props = {
    className?: string,
    children?: ReactChildren,
    color?: string,
    style?: Object,
    label?: string,
    icon?: string,
    size?: number,
    iconFill?: string,
}

export const Icon = props => {
    const {
        className,
        children,
        style,
        label,
        icon,
        size,
        iconFill,
    } = props;

    const Icon = IconId[icon];

    return (
        <Icon {...props} />
    );
}

Icon.propTypes = {
    children: PropTypes.element,
    className: PropTypes.string,
    color: PropTypes.string,
    style: PropTypes.object,
    label: PropTypes.string,
    icon: PropTypes.string,
    size: PropTypes.number,
    iconFill: PropTypes.string,
};
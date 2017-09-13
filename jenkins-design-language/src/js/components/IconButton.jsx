// @flow

import React, { PropTypes } from 'react';
import { Icon } from '../components/Icon';

type Props = {
    className?: string,
    children?: ReactChildren,
    style?: Object,
    label?: string,
    iconName?: string,
    iconSize?: number,
    iconFill?: string,
    onClick?: Function,
}

/**
 * button element with icon.
 * Use "iconName" for standard material-ui icons
 * Use "children" for custom icon. Provide element (e.g. raw svg), or React component
 *
 * @param {object} [props.children] - React element or custom component to render as icon
 * @param {string} [props.className] - custom class name for outer element
 * @param {object} [props.style] - custom style object
 * @param {string} [props.label] - button text
 * @param {string} [props.iconName] - name of material-ui icon to display
 * @param {number} [props.iconSize] - width/height of icon
 * @param {string} [props.iconFill] - color code to apply as fill
 * @param {function} [props.onClick] - onclick callback function
 * @constructor
 */
export function IconButton(props:Props) {
    function _onClick() {
        if (onClick) {
            onClick();
        }
    }

    const { children, className, style, label, iconName, iconSize, iconFill, onClick } = props;

    let icon = null;

    if (iconName) {
        icon = <Icon icon={iconName} size={iconSize ? iconSize : 20} color={iconFill} />;
    } else if (children) {
        // pass down props of interest to the child so it can react
        const iconProps = {
            width: iconSize,
            height: iconSize,
            size: iconSize,
            fill: iconFill,
        };

        icon = React.Children.map(children, child => React.cloneElement(child, iconProps));
    }

    const customClass = className || '';
    const iconNameClass = iconName ? `u-icon-${iconName}` : '';
    const materialClass = iconName ? 'u-material-icon' : '';
    const spacingClass = label && icon ? 'u-inner-margin' : '';

    return (
        <button
            className={`IconButton ${customClass} ${iconNameClass} ${materialClass} ${spacingClass}`}
            style={style}
            onClick={_onClick}
        >
            <div className="IconButton-wrapper">
                { icon &&
                <span className="IconButton-icon">{icon}</span>
                }
                { label &&
                <span className="IconButton-text">{label}</span>
                }
            </div>
        </button>
    );
}

IconButton.propTypes = {
    children: PropTypes.element,
    className: PropTypes.string,
    style: PropTypes.object,
    label: PropTypes.string,
    iconName: PropTypes.string,
    iconSize: PropTypes.number,
    iconFill: PropTypes.string,
    onClick: PropTypes.func,
};

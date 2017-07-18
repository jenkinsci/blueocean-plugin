import React, { PropTypes } from 'react';
import { IconButton } from '@jenkins-cd/design-language';

import { BbIcon } from './BbIcon';

export default function BbDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }
    const className = `${props.className} monochrome ${props.isSelected ? 'active' : ''}`;

    return (
        <IconButton className={className} label={props.label} onClick={onSelect}>
            <BbIcon />
        </IconButton>
    );
}

BbDefaultOption.propTypes = {
    className: PropTypes.string,
    label: PropTypes.string,
    onSelect: PropTypes.func,
    isSelected: PropTypes.bool,
};


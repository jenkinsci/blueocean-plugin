import React, { PropTypes } from 'react';
import { IconButton } from '@jenkins-cd/design-language';

import { GitIcon } from './GitIcon';

/**
 * Simple button used to launch Git creation flow
 * @param props
 * @constructor
 */
export default function GitDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }

    const className = `git-creation monochrome ${props.isSelected ? 'active' : ''}`;

    return (
        <div>
            <IconButton className={className} label="Git" onClick={onSelect}>
                <GitIcon />
            </IconButton>
        </div>
    );
}

GitDefaultOption.propTypes = {
    onSelect: PropTypes.func,
    isSelected: PropTypes.bool,
};

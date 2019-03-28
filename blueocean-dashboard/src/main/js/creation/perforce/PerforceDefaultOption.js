import React, {PropTypes} from 'react';
import {IconButton} from '@jenkins-cd/design-language';

import {PerforceIcon} from './PerforceIcon';

/**
 * Simple button used to launch Perforce creation flow
 * @param props
 * @constructor
 */
export default function PerforceDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }

    const className = `p4-creation monochrome ${props.isSelected ? 'active' : ''}`;
    return (
        <div>
            <IconButton className={className} label="Perforce" onClick={onSelect}>
                <PerforceIcon/>
            </IconButton>
        </div>
    );
}

PerforceDefaultOption.propTypes = {
    onSelect: PropTypes.func,
    isSelected: PropTypes.bool,
};

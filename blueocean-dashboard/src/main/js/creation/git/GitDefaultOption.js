import React, { PropTypes } from 'react';
import { IconButton } from '@jenkins-cd/design-language';

import { GitIcon } from './GitIcon';

export default function GitDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }

    return (
        <div>
            <IconButton className="monochrome" label="Git" onClick={onSelect}>
                <GitIcon />
            </IconButton>
        </div>
    );
}

GitDefaultOption.propTypes = {
    onSelect: PropTypes.func,
};

import React, { PropTypes } from 'react';
import { IconButton } from '@jenkins-cd/design-language';

import { GithubIcon } from './GithubIcon';

export default function GitHubDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }
    const className = `monochrome ${props.isSelected ? 'active' : ''}`;
    
    return (
        <IconButton className={className} label="Github" onClick={onSelect}>
            <GithubIcon />
        </IconButton>
    );
}

GitHubDefaultOption.propTypes = {
    onSelect: PropTypes.func,
    isSelected: PropTypes.bool,
};

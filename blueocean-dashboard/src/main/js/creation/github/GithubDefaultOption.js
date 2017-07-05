import React, { PropTypes } from 'react';
import { IconButton } from '@jenkins-cd/design-language';

import { GithubIcon } from './GithubIcon';

export default function GitHubDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }

    const className = `${props.className} monochrome ${props.isSelected ? 'active' : ''}`;

    return (
        <IconButton className={className} label={props.label} onClick={onSelect}>
            <GithubIcon />
        </IconButton>
    );
}

GitHubDefaultOption.propTypes = {
    className: PropTypes.string,
    label: PropTypes.string,
    onSelect: PropTypes.func,
    isSelected: PropTypes.bool,
};

GitHubDefaultOption.defaultProps = {
    className: 'github-creation',
    label: 'GitHub',
};

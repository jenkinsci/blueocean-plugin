/**
 * Created by cmeyers on 10/21/16.
 */
import React, { PropTypes } from 'react';


export default function GitHubDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }

    return (
        <button onClick={onSelect}>
            Github
        </button>
    );
}

GitHubDefaultOption.propTypes = {
    onSelect: PropTypes.func,
};

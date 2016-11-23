/**
 * Created by cmeyers on 10/21/16.
 */
import React, { PropTypes } from 'react';

export default function GitDefaultOption(props) {
    function onSelect() {
        if (props.onSelect) {
            props.onSelect();
        }
    }

    return (
        <button onClick={onSelect}>
            Git
        </button>
    );
}

GitDefaultOption.propTypes = {
    onSelect: PropTypes.func,
};

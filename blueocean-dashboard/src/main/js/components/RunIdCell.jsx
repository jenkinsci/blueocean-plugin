import React, { PropTypes } from 'react';

function RunIdCell({ run }) {
    const identifier = run.name ? run.name : run.id;
    return <span title={identifier}>{identifier}</span>;
}

RunIdCell.propTypes = {
    run: PropTypes.object,
};

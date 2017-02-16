import React, { PropTypes } from 'react';

import i18nTranslator from '../i18n/i18n';

const translate = i18nTranslator('blueocean-web');

const buttonText = (pager) => {
    if (pager.pending) {
        return translate('common.pager.loading', { defaultValue: 'Loading...' });
    }
    
    return translate('common.pager.more', { defaultValue: 'Show more' });
};

export const ShowMoreButton = (props) => {
    const { pager } = props;
    if (!pager || !pager.hasMore) {
        return null;
    }
    return (
        <button
            className="btn-show-more btn-secondary"
            onClick={ () => pager.fetchNextPage() }
        >
            { buttonText(pager) }
        </button>);
};

ShowMoreButton.propTypes = {
    pager: PropTypes.object,
};

import React, { PropTypes } from 'react';
import { PlaceholderTable } from '@jenkins-cd/design-language';
import { Link } from 'react-router';

import { PlaceholderContent } from './PlaceholderContent';
import { PlaceholderDialog } from './PlaceholderDialog';
import Icon from './Icon';


export function DashboardPlaceholder(props) {
    const { t } = props;

    const columns = [
        { width: 750, isFlexible: true, head: { text: 40 }, cell: { text: 150 } },
        { width: 50, head: { text: 40 }, cell: { icon: 30 } },
        { width: 50, head: { text: 50 }, cell: { text: 50 } },
        { width: 50, head: { text: 50 }, cell: { text: 50 } },
        { width: 30, head: {}, cell: { icon: 20 } },
    ];

    const content = {
        icon: Icon.PIPELINE_RUNNING,
        title: t('home.placeholder.title'),
        message: t('home.placeholder.message'),
        linkElement: <Link className="btn" to="/create-pipeline">{t('home.placeholder.linktext')}</Link>,
    };

    return (
        <PlaceholderContent className="HomePlaceholder u-fill u-fade-bottom" style={{ top: 72 }}>
            <PlaceholderTable columns={columns} rowCount={20} />
            <PlaceholderDialog width={375} content={content} />
        </PlaceholderContent>
    );
}

DashboardPlaceholder.propTypes = {
    t: PropTypes.func,
};

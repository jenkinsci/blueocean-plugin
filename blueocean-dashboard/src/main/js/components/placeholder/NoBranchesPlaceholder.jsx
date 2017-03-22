import React, { PropTypes } from 'react';
import { PlaceholderTable } from '@jenkins-cd/design-language';

import { PlaceholderContent } from './PlaceholderContent';
import { PlaceholderDialog } from './PlaceholderDialog';


export function NoBranchesPlaceholder(props) {
    const { t } = props;

    const columns = [
        { width: 50, head: { text: 40 }, cell: { icon: 20 } },
        { width: 50, head: { text: 40 }, cell: { text: 30 } },
        { width: 50, head: { text: 50 }, cell: { text: 80 } },
        { width: 50, head: { text: 50 }, cell: { text: 60 } },
        { width: 200, head: { text: 60 }, cell: { text: 150 } },
        { width: 100, head: { text: 60 }, cell: { text: 60 } },
        { width: 100, head: { text: 60 }, cell: { text: 60 } },
        { width: 50, head: {}, cell: { icon: 20 } },
    ];

    return (
        <PlaceholderContent>
            <PlaceholderTable columns={columns} rowCount={15} />
            <PlaceholderDialog width={375} content={{
                icon: <svg className="icon" />,
                title: t('pipelinedetail.placeholder.nobranches.title'),
                message: t('pipelinedetail.placeholder.nobranches.message'),
                linkText: t('pipelinedetail.placeholder.nobranches.linktext'),
                linkHref: t('pipelinedetail.placeholder.nobranches.linkhref'),
            }}
            >
                {/*
                 <svg className="icon" />
                 <h1 className="title">You don't have any branches that contain a Jenkinsfile</h1>
                 <p className="message">A Jenkinsfile is defined in your repository and describes how your pipeline will work.</p>
                 <a className="btn">Learn more</a>
                 */ }
            </PlaceholderDialog>
        </PlaceholderContent>
    );
}

NoBranchesPlaceholder.propTypes = {
    t: PropTypes.func,
};

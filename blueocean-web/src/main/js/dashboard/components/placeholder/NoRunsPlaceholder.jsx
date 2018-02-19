import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import { PlaceholderTable } from '@jenkins-cd/design-language';

import { PlaceholderContent } from './PlaceholderContent';
import { PlaceholderDialog } from './PlaceholderDialog';
import Icon from './Icon';


const columns = [
    { width: 40, head: { text: 40 }, cell: { icon: 20 } },
    { width: 50, head: { text: 40 }, cell: { text: 30 } },
    { width: 50, head: { text: 50 }, cell: { text: 80 } },
    { width: 50, head: { text: 50 }, cell: { text: 60 } },
    { width: 200, isFlexible: true, head: { text: 60 }, cell: { text: 150 } },
    { width: 100, head: { text: 60 }, cell: { text: 60 } },
    { width: 100, head: { text: 60 }, cell: { text: 60 } },
    { width: 20, head: {}, cell: { icon: 20 } },
];


export function NoRunsMultibranchPlaceholder(props) {
    const { branchesUrl, t } = props;

    const title = t('pipelinedetail.placeholder.noruns.multibranch.branches_title');
    const linkElement = (
        <Link className="btn" to={branchesUrl}>
            {t('pipelinedetail.placeholder.noruns.multibranch.branches_linktext')}
        </Link>
    );

    const content = {
        icon: Icon.PIPELINE_EMPTY,
        title,
        linkElement,
    };

    return (
        <PlaceholderContent className="NoRuns u-fill u-fade-bottom" style={{ top: 72 }}>
            <PlaceholderTable columns={columns} rowCount={20} />
            <PlaceholderDialog width={375} content={content} />
        </PlaceholderContent>
    );
}

NoRunsMultibranchPlaceholder.propTypes = {
    t: PropTypes.func,
    branchesUrl: PropTypes.string,
};


export function NoRunsDefaultPlaceholder(props) {
    const { t, runButton } = props;

    const content = {
        icon: Icon.PIPELINE_EMPTY,
        title: t('pipelinedetail.placeholder.noruns.default.title'),
        linkElement: React.cloneElement(runButton, { innerButtonClasses: 'btn' }),
    };

    return (
        <PlaceholderContent className="NoRuns u-fill u-fade-bottom" style={{ top: 72 }}>
            { runButton }
            <PlaceholderTable columns={columns} rowCount={20} />
            <PlaceholderDialog width={375} content={content} />
        </PlaceholderContent>
    );
}

NoRunsDefaultPlaceholder.propTypes = {
    t: PropTypes.func,
    runButton: PropTypes.element,
};


export function NoRunsForBranchPlaceholder(props) {
    const { branchName, t } = props;

    const content = {
        title: t('pipelinedetail.placeholder.noruns.multibranch.noruns_title', { 0: branchName }),
    };

    return (
        <div className="NoRunsForBranch">
            <PlaceholderDialog content={content} />
        </div>
    );
}

NoRunsForBranchPlaceholder.propTypes = {
    t: PropTypes.func,
    branchName: PropTypes.string,
};

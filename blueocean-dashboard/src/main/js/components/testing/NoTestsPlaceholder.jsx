import React, { Component, PropTypes } from 'react';
import { PlaceholderTable } from '@jenkins-cd/design-language';
import Icon from '../placeholder/Icon';
import { PlaceholderDialog } from '../placeholder/PlaceholderDialog';

export default class NoTestsPlaceholder extends Component {
    propTypes = {
        t: PropTypes.func,
    };

    render() {
        const t = this.props.t;
        const columns = [
            { width: 30, head: { text: 30 }, cell: { icon: 20 } },
            { width: 750, isFlexible: true, head: { text: 40 }, cell: { text: 200 } },
            { width: 80, head: {}, cell: { text: 50 } },
        ];

        const content = {
            icon: Icon.NOT_INTERESTED,
            title: t('rundetail.tests.results.empty.title'),
            linkText: t('rundetail.tests.results.empty.linktext'),
            linkHref: t('rundetail.tests.results.empty.linkhref'),
        };

        return (
            <div className="RunDetailsEmpty NoTests">
                <PlaceholderTable columns={columns} />
                <PlaceholderDialog width={300} content={content} />
            </div>
        );
    }
}

import React, { Component } from 'react';
import { Link } from 'react-router';
import { observer } from 'mobx-react';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import dashboardNavState from './DashboardNavState';

const t = i18nTranslator('blueocean-web');

@observer
export default class TopPipelinesLink extends Component {
    render() {
        const className = dashboardNavState.isActive ? 'selected' : '';
        return (
            <Link className={className} to="/pipelines">
                {t('pipelines', {defaultValue: 'Pipelines'})}
            </Link>
        );
    }
}

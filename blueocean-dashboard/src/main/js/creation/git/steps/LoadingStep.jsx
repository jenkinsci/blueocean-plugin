import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import FlowStep from '../../flow2/FlowStep';

const t = i18nTranslator('blueocean-dashboard');

@observer
export class LoadingStep extends React.Component {
    render() {
        return <FlowStep {...this.props} title={t('common.pager.loading', { defaultValue: 'Loading...' })} loading scrollOnActive={false} />;
    }
}

LoadingStep.propTypes = {
    flowManager: PropTypes.object,
};

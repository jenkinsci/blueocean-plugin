import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

@observer
export default class BbUnknownErrorStep extends React.Component {
    render() {
        return (
            <FlowStep {...this.props} title="Unknown Error" error>
                <div className="instructions">{t('creation.core.error.unexpected.try_again')}</div>

                <p className="instructions">Message: {this.props.message}</p>
            </FlowStep>
        );
    }
}

BbUnknownErrorStep.propTypes = {
    flowManager: PropTypes.object,
    message: PropTypes.string,
};

import React, { Component, PropTypes } from 'react';
import { Alerts } from '@jenkins-cd/design-language';

export default class RunDescription extends Component {
    propTypes = {
        run: PropTypes.object,
        t: PropTypes.object,
    };

    render() {
        if (!this.props.run || !this.props.run.description) {
            return null;
        }
        return (
            <div className="RunDetails-Description">
                <Alerts title={this.props.t('rundetail.pipeline.description')} message={this.props.run.description} />
            </div>
        );
    };
}

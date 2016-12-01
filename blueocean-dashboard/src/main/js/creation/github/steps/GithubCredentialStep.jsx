/**
 * Created by cmeyers on 11/30/16.
 */
import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubCredentialsStep extends React.Component {
    render() {
        return (
            <FlowStep {...this.props} title="Connect to Github">
                <p>Some text about Github and a link to access token how-to.</p>

                <TextInput placeholder="123456abcdef" />
                <button>Connect</button>
            </FlowStep>
        );
    }
}

GithubCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
};

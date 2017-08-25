import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { FormElement, PasswordInput } from '@jenkins-cd/design-language';
import { Fetch, AppConfig } from '@jenkins-cd/blueocean-core-js';

import { Button } from '../../creation/github/Button';

function copySelectionText() {
    var copysuccess // var to check whether execCommand successfully executed
    try{
        copysuccess = document.execCommand("copy") // run command to copy selected text to clipboard
    } catch(e){
        copysuccess = false
    }
    return copysuccess
}

@observer
class GitCredentialsPicker extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentWillMount() {
        const { onStatus, dialog, onComplete } = this.props;
        if (onStatus) {
            onStatus('promptLoading');
        }
        Fetch.fetchJSON(AppConfig.getRestRoot() + '/organizations/' + AppConfig.getOrganizationName() + "/user/publickey/")
            .then(credential => {
                this.setState({ publicKey: credential.publickey });
                if (onStatus) {
                    onStatus('promptReady');
                }
                if (!dialog) {
                    onComplete(credential);
                }
            });
    }

    componentWillUpdate() {
        const textBox = this.refs.publicKey;
        if (textBox) {
            textBox.onfocus = function () {
                textBox.select();
                copySelectionText();

                // Work around Chrome's little problem
                textBox.onmouseup = function () {
                    // Prevent further mouseup intervention
                    textBox.onmouseup = null;
                    return false;
                };
            }
        }
    }

    closeDialog() {
        this.props.onComplete();
    }

    render() {
        if (!this.state.publicKey) {
            return null;
        }
        return (
            <div>
                <FormElement title={""} errorMessage={undefined}>
                    <div className="credentials-picker-git">
                        <p className="instructions">
                            This is your personal Jenkins key, please
                            copy and paste it in your git repository's list
                            of authorized users to continue.
                        </p>
                        <textarea className="TextArea-control" ref="publicKey" onChange={e => null} value={this.state.publicKey} />
                    </div>
                </FormElement>
                {this.props.dialog && <Button onClick={() => this.closeDialog()}>Ok</Button>}
            </div>
        );
    }
}

GitCredentialsPicker.propTypes = {
    onStatus: PropTypes.func,
    onComplete: PropTypes.func,
    scmId: PropTypes.string,
    dialog: PropTypes.bool,
};

export default GitCredentialsPicker;

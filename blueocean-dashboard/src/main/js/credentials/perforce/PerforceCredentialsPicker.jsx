import React, {PropTypes} from 'react';
import {observer} from "mobx-react";

import PerforceCredentialsManager from './PerforceCredentialsManager';
import {Dropdown, FormElement, PasswordInput, TextInput} from "@jenkins-cd/design-language";
import {Button} from "../../creation/github/Button";
import PerforceFlowManager from "../../creation/perforce/PerforceFlowManager";


/*
***********************************************************************
    THIS FILE IS NO LONGED USED. DELETE IT.
***********************************************************************
 */
@observer
class PerforceCredentialsPicker extends React.Component {
    constructor(props) {
        super(props);
        this.authManager = new PerforceCredentialsManager();

        this.state = {
            loading: false,
        };
        console.log("PerforceCredentialsPicker constructor");
    }

    componentWillMount() {
        this.setState({
            loading: true,
        });

        if (this.props.onStatus) {
            this.props.onStatus('promptLoading');
        }
        console.log("PerforceCredentialsPicker componentWillMount");
    }

    componentDidMount() {
        this._configure(this.props);
        console.log("PerforceCredentialsPicker componentDidMount");
        this.myUsers = ['User1', 'User2', 'User3', 'User4'];
        this.authManager.findExistingCredential().then(credential => this._findExistingCredentialComplete(credential));

    }

    _configure(props) {
        this.authManager.configure(props.scmId, props.apiUrl);
    }

    _findExistingCredentialComplete(credential) {
        console.log("PerforceCredentialsPicker _findExistingCredentialComplete:: credential" + credential);
        this.setState({
            loading: false,
        });

        if (credential && this.props.onComplete) {
            this.props.onComplete(credential, 'autoSelected');
        } else if (this.props.onStatus) {
            this.props.onStatus('promptReady');
        }
    }

    _onChangeCredDropdown(option) {
        console.log("PerforceCredentialsPicker._onChangeCredDropdown: " + option);
        this.setState({
            selectedCred: option,
        });
    }

    render() {
        console.log("PerforceCredentialsPicker: render()");
        const errorMessage = "Sample error msg";
        /*const tokenUrl = getCreateTokenUrl(this.props.apiUrl);

        let result = null;

        if (this.authManager.pendingValidation) {
            result = 'running';
        } else if (this.authManager.stateId === GithubCredentialsState.SAVE_SUCCESS) {
            result = 'success';
        }
*/
        let result = null;
        result = 'success';

        const status = {
            result,
        };
        //const myUsers = ['User1', 'User2', 'User3', 'User4'];
        //const myResp = [{"loginName":"cbopardikar","email":"cbopardikar@cbopardikar","fullName":"cbopardikar","access":"Oct 11, 2018 2:36:55 PM","update":"Oct 10, 2018 4:05:24 PM","type":"STANDARD","refreshable":false,"updateable":false},{"loginName":"p4testservice","email":"abc@nothing.com","fullName":"Test Service User","access":"Oct 10, 2018 4:23:46 PM","update":"Oct 10, 2018 4:23:46 PM","type":"STANDARD","refreshable":false,"updateable":false},{"loginName":"p4testsuper","email":"abc@nothing.com","fullName":"Test Super User","access":"Oct 10, 2018 4:19:43 PM","update":"Oct 10, 2018 4:19:43 PM","type":"STANDARD","refreshable":false,"updateable":false},{"loginName":"p4testuser","email":"abc@nothing.com","fullName":"Test User","access":"Oct 11, 2018 2:38:23 PM","update":"Oct 11, 2018 2:38:23 PM","type":"STANDARD","refreshable":false,"updateable":false}];
    //labelField="loginName"
        return (
            (
                <div className="credentials-picker-github">
                    <p className="instructions">
                        Select credentials to connect to Perforce. <br />
                    </p>

                    <FormElement>
                        <Dropdown
                            options={this.myUsers}
                            onChange={option => this._onChangeCredDropdown(option)}
                            onComplete={alert("Picker complete")}
                        />

                        <Button className="button-connect" status={status} >
                            New Cred
                        </Button>
                    </FormElement>
                </div>
            )
        );
    }
}

PerforceCredentialsPicker.propTypes = {
    myUsers: PropTypes.array,
    onStatus: PropTypes.func,
    onComplete: PropTypes.func,
    scmId: PropTypes.string,
    apiUrl: PropTypes.string,
};

export default PerforceCredentialsPicker;

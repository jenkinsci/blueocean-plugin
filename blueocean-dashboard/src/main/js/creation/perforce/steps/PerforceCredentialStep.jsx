import React, {PropTypes} from 'react';
import {observer} from 'mobx-react';
import {Dropdown, FormElement} from '@jenkins-cd/design-language';
import FlowStep from '../../flow2/FlowStep';


let t = null;

@observer
class PerforceCredentialsStep extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            selectedCredential: null,
            disableNext: true,
        };
        this.dropdown = null;
        this.credManager = props.flowManager.credManager;
    }

    componentWillMount() {
        t = this.props.flowManager.translate;
    }

    render() {
        const disableNext = this.state.disableNext;
        const {flowManager} = this.props;
        const {serverManager} = flowManager;
        const title = t('creation.p4.step1.title');
        //TODO Change the below github title
        return (
            <FlowStep {...this.props} className="credentials-picker-perforce" title={title}>
                <FormElement title={t('creation.p4.step1.instructions')}>
                    <Dropdown
                        ref={dropdown => {
                            this.dropdown = dropdown;
                        }}
                        options={this.credManager.credentials}
                        labelField="id"
                        onChange={option => this._onChangeDropdown(option)}
                    />
                </FormElement>
                <button className="button-next-step" disabled={disableNext} onClick={() => this._onClickNextButton()}>
                    {t('creation.p4.button_next')}
                </button>
            </FlowStep>
        );
        //return ("","","");
    }

    _onChangeDropdown(option) {
        const {flowManager} = this.props.flowManager;
        //TODO may want to do validation later
        //serverManager.validateVersion(option.id).then(success => this._onValidateVersion(success), error => this._onValidateVersion(error));
        this.setState({
            selectedCredential: option.id,
            disableNext: false,
        });
    }

    _onClickNextButton() {
        this.props.flowManager.selectCredential(this.state.selectedCredential);
    }
}

PerforceCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
};

export default PerforceCredentialsStep;

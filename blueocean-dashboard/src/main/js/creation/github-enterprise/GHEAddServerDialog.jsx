import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';

import { Dialog, FormElement, TextInput } from '@jenkins-cd/design-language';


let t = null;


@observer
class GHEAddServerDialog extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            creationPending: false,
            creationErrorMsg: null,
            nameValue: null,
            nameErrorMsg: null,
            urlValue: null,
            urlErrorMsg: null,
        };

        t = props.flowManager.translate;
    }

    _nameChange(value) {
        this.setState({
            nameValue: value,
        });

        this._updateNameErrorMsg();
    }

    _updateNameErrorMsg = debounce(() => {
        if (this.state.nameErrorMsg && this.state.nameValue) {
            this.setState({
                nameErrorMsg: null,
            });
        }
    }, 200);

    _urlChange(value) {
        this.setState({
            urlValue: value,
        });

        this._updateUrlErrorMsg();
    }

    _updateUrlErrorMsg = debounce(() => {
        if (this.state.urlErrorMsg && this.state.urlValue) {
            this.setState({
                urlErrorMsg: null,
            });
        }
    }, 200);

    _onCreateClick() {
        const valid = this._performValidation();

        if (!valid) {
            return;
        }

        const { serverManager } = this.props.flowManager;

        serverManager.createServer(this.state.nameValue, this.state.urlValue)
            .then(result => this._onCreateServerResult(result));

        this.setState({
            creationPending: true,
        });
    }

    _performValidation() {
        let result = true;

        if (!this.state.nameValue) {
            this.setState({
                nameErrorMsg: t('creation.githubent.add_server.text_name_error_required'),
            });

            result = false;
        }

        if (!this.state.urlValue) {
            this.setState({
                urlErrorMsg: t('creation.githubent.add_server.text_url_error_required'),
            });

            result = false;
        }

        return result;
    }

    _onCreateServerResult(result) {
        this.setState({
            creationPending: false,
        });

        if (result.success) {
            const { server } = result;

            this.setState({
                creationErrorMsg: null,
                creationPending: false,
            });

            this._onCloseClick(server);
        } else {
            const { duplicateName, duplicateUrl } = result;
            this.setState({
                nameErrorMsg: duplicateName ? t('creation.githubent.add_server.text_name_error_duplicate') : null,
                urlErrorMsg: duplicateUrl ? t('creation.githubent.add_server.text_url_error_duplicate') : null,
                creationErrorMsg: !duplicateName && !duplicateUrl ? t('creation.githubent.add_server.error_msg') : null,
            });
        }
    }

    _onCloseClick(credential) {
        if (this.props.onClose) {
            this.props.onClose(credential);
        }
    }

    render() {
        const disabled = this.state.creationPending;

        const buttons = [
            <button className="button-create-server" disabled={disabled} onClick={() => this._onCreateClick()}>
                {t('creation.githubent.add_server.button_create')}
            </button>,
            <button className="btn-secondary" disabled={disabled} onClick={() => this._onCloseClick()}>
                {t('creation.githubent.add_server.button_cancel')}
            </button>,
        ];

        return (
            <Dialog
                className="github-enterprise-add-server-dialog"
                title={t('creation.githubent.add_server.title')}
                buttons={buttons}
            >
                <FormElement
                    className="server-new"
                    title={t('creation.githubent.add_server.instructions')}
                    errorMessage={this.state.creationErrorMsg}
                    verticalLayout
                >
                    <FormElement title={t('creation.githubent.add_server.text_name_title')} errorMessage={this.state.nameErrorMsg}>
                        <TextInput className="text-name" placeholder={t('creation.githubent.add_server.text_name_placeholder')} onChange={val => this._nameChange(val)} />
                    </FormElement>

                    <FormElement title={t('creation.githubent.add_server.text_url_title')} errorMessage={this.state.urlErrorMsg}>
                        <TextInput className="text-url" placeholder={t('creation.githubent.add_server.text_url_placeholder')} onChange={val => this._urlChange(val)} />
                    </FormElement>
                </FormElement>
            </Dialog>
        );
    }

}

GHEAddServerDialog.propTypes = {
    flowManager: PropTypes.object,
    onClose: PropTypes.func,
};

export default GHEAddServerDialog;

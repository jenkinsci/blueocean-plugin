import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import { Dialog, FormElement, TextInput } from '@jenkins-cd/design-language';

import ServerErrorRenderer from './ServerErrorRenderer';


let t = null;


@observer
class BBAddServerDialog extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            pending: false,
            unknownError: null,
            urlValue: null,
            urlErrorMsg: null,
        };

        t = props.flowManager.translate;
    }

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

        serverManager.createServer(this.state.urlValue)
            .then(
                server => this._onCreateServerSuccess(server),
                error => this._onCreateServerFailure(error),
            );

        this.setState({
            pending: true,
        });
    }

    _performValidation() {
        let result = true;

        if (!this.state.urlValue) {
            this.setState({
                urlErrorMsg: t('creation.bbserver.add_server.text_url_error_required'),
            });

            result = false;
        }

        return result;
    }

    _onCreateServerSuccess(server) {
        this.setState({
            pending: false,
            unknownError: null,
        });

        this._onCloseClick(server);
    }

    _onCreateServerFailure(error) {
        const { duplicateName, duplicateUrl, invalidUrl } = error;

        const newState = {
            pending: false,
            unknownError: null,
            urlErrorMsg: null,
        };

        if (duplicateUrl) {
            newState.urlErrorMsg = t('creation.bbserver.add_server.text_url_error_duplicate');
        } else if (invalidUrl) {
            newState.urlErrorMsg = t('creation.bbserver.add_server.text_url_error_invalid');
        }

        if (!duplicateName && !duplicateUrl && !invalidUrl) {
            newState.unknownError = error;
        }

        this.setState(newState);
    }

    _onCloseClick(credential) {
        if (this.props.onClose) {
            this.props.onClose(credential);
        }
    }

    render() {
        const disabled = this.state.pending;

        const buttons = [
            <button className="button-create-server" disabled={disabled} onClick={() => this._onCreateClick()}>
                {t('creation.bbserver.add_server.button_create')}
            </button>,
            <button className="btn-secondary" disabled={disabled} onClick={() => this._onCloseClick()}>
                {t('creation.bbserver.add_server.button_cancel')}
            </button>,
        ];

        return (
            <Dialog
                className="github-enterprise-add-server-dialog"
                title={t('creation.bbserver.add_server.title')}
                buttons={buttons}
            >
                <FormElement
                    className="server-new"
                    title={t('creation.bbserver.add_server.instructions')}
                    verticalLayout
                >
                    { this.state.unknownError && <ServerErrorRenderer error={this.state.unknownError} /> }

                    <FormElement title={t('creation.bbserver.add_server.text_url_title')} errorMessage={this.state.urlErrorMsg}>
                        <TextInput className="text-url" placeholder={t('creation.bbserver.add_server.text_url_placeholder')} onChange={val => this._urlChange(val)} />
                    </FormElement>
                </FormElement>
            </Dialog>
        );
    }

}

BBAddServerDialog.propTypes = {
    flowManager: PropTypes.object,
    onClose: PropTypes.func,
};

export default BBAddServerDialog;

import React, { PropTypes } from 'react';
import { ErrorMessage } from '@jenkins-cd/design-language';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

const t = i18nTranslator('blueocean-dashboard');

export default function ServerErrorRenderer(props) {
    const { error, verbose } = props;

    return (
        <div className="server-error">
            <ErrorMessage>{t('servererror.title')}</ErrorMessage>

            <p>{t('servererror.message', { 0: error.message })}</p>

            {verbose &&
                error.errors &&
                error.errors.map(err => (
                    <p>
                        <div>{t('servererror.errors.field', { 0: err.field })}</div>
                        <div>{t('servererror.errors.code', { 0: err.code })}</div>
                        <div>{t('servererror.errors.message', { 0: err.message })}</div>
                    </p>
                ))}
        </div>
    );
}

ServerErrorRenderer.propTypes = {
    error: PropTypes.object,
    verbose: PropTypes.bool,
};

ServerErrorRenderer.defaultProps = {
    verbose: true,
};

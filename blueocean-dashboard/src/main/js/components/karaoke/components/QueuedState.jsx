import React, { PropTypes } from 'react';
import { Alerts } from '@jenkins-cd/design-language';

const messageOrDefault = (translation, messageKey, message) => (!message ? translation(messageKey) : message);

export const QueuedState = ({ translation, titleKey, messageKey, message }) => (
    <Alerts title={translation(titleKey)} message={messageOrDefault(translation, messageKey, message)} />
);

QueuedState.propTypes = {
    titleKey: PropTypes.string,
    messageKey: PropTypes.object,
    message: PropTypes.object,
    translation: PropTypes.func,
};

export const NoSteps = ({ translation, titleKey, messageKey, message }) => (
    <Alerts title={translation(titleKey)} message={messageOrDefault(translation, messageKey, message)} />
);

NoSteps.propTypes = {
    titleKey: PropTypes.string,
    messageKey: PropTypes.object,
    message: PropTypes.object,
    translation: PropTypes.func,
};

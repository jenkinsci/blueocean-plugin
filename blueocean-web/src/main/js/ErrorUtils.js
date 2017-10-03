import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';


let initialized = false;
let warningLogged = false;

function isGoogleChrome() {
    const isChromium = window.chrome,
        winNav = window.navigator,
        vendorName = winNav.vendor,
        isOpera = winNav.userAgent.indexOf("OPR") > -1,
        isIEedge = winNav.userAgent.indexOf("Edge") > -1,
        isIOSChrome = winNav.userAgent.match("CriOS");

    return isIOSChrome ||
        (isChromium !== null && typeof isChromium !== "undefined" && vendorName === "Google Inc." &&  !isOpera && !isIEedge);
}

function isFirefox() {
    return window.navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
}

function logApplicationError(messageOrEvent) {
    let message = null;

    if (messageOrEvent.error && messageOrEvent.error.stack) {
        message = messageOrEvent.error.stack;
    } else if (messageOrEvent.stack) {
        message = messageOrEvent.stack;
    } else {
        message = messageOrEvent;
    }

    console.error('Unhandled Error: ', JSON.stringify(message, null, 4));

    if (messageOrEvent.preventDefault) {
        messageOrEvent.preventDefault();
    }
}

function logUnhandledPromiseRejection(errorEvent) {
    let message = null;

    if (errorEvent.detail && errorEvent.detail.reason && errorEvent.detail.reason.stack) {
        message = errorEvent.detail.reason.stack;
    } else if (errorEvent.reason && errorEvent.reason.stack) {
        message = errorEvent.reason.stack;
    } else {
        message = errorEvent;
    }

    console.error('Unhandled Rejection: ', JSON.stringify(message, null, 4));

    if (errorEvent.preventDefault) {
        errorEvent.preventDefault();
    }
}

function initializeErrorHandling() {
    if (!initialized) {
        window.addEventListener("error", logApplicationError);
        window.addEventListener("unhandledrejection", logUnhandledPromiseRejection);
        initialized = true;
    }
}

function logBrowserConsoleWarning() {
    if (!warningLogged) {
        const translate = i18nTranslator('blueocean-web');

        if (isGoogleChrome()) {
            console.log(translate('common.logging.warning.chrome'));
        } else if (isFirefox()) {
            console.log(translate('common.logging.warning.firefox'));
        }

        warningLogged = true;
    }
}

export default {
    initializeErrorHandling,
    logBrowserConsoleWarning,
};

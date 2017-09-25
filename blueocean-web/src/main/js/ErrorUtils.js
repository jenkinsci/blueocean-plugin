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
    const message = messageOrEvent.error || messageOrEvent;

    if (message && message.stack) {
        console.error('Unhandled Error: ', JSON.stringify(message.stack, null, 4));

        if (messageOrEvent.preventDefault) {
            messageOrEvent.preventDefault();
        }
    }
    // otherwise fall back to any default behavior
}

function logUnhandledPromiseRejection(errorEvent) {
    const { reason } = errorEvent.detail || errorEvent;

    if (reason && reason.stack) {
        console.error('Unhandled Rejection: ', JSON.stringify(reason.stack, null, 4));
        errorEvent.preventDefault();
    }
    // otherwise we'll fall back to the default rejection handler
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

let initialized = false;

function logApplicationError(messageOrEvent) {
    const message = messageOrEvent.error || messageOrEvent;
    console.error('unhandled error: ', message);

    if (messageOrEvent.preventDefault) {
        messageOrEvent.preventDefault();
    }
}

function logUnhandledPromiseRejection(errorEvent) {
    const { reason } = errorEvent.detail || errorEvent;

    if (reason) {
        console.error('unhandled rejection: ', reason);
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

export default {
    initializeErrorHandling,
};

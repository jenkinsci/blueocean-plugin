/**
 * Simple git utilties
 */

// FIXME centralize this, move credential creation into jenkins.credential.selection for git
export function isSshRepositoryUrl(url) {
    if (!url || url.trim() === '') {
        return false;
    }

    if (/ssh:\/\/.*/.test(url)) {
        return true;
    }

    if (/[^@:]+@.*/.test(url)) {
        return true;
    }

    return false;
}

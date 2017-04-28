import { AppConfig } from '@jenkins-cd/blueocean-core-js';

export function prefixIfNeeded(url) {
    return `${AppConfig.getJenkinsRootURL().replace(/\/$/, '')}${url}`;
}

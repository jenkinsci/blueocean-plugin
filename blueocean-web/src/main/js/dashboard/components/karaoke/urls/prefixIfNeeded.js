import { AppConfig } from '../../../../core/index.js';

// TODO: rename this as the name is misleading - it *always* adds a prefix
export function prefixIfNeeded(url) {
    return `${AppConfig.getJenkinsRootURL().replace(/\/$/, '')}${url}`;
}

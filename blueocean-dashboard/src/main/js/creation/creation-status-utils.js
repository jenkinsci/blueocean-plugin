import { User, Security } from '@jenkins-cd/blueocean-core-js';
import { Enum } from '../creation/flow2/Enum';

export const CreationStatus = new Enum({
    ENABLED: 'enabled',
    DISABLED: 'disabled',
    HIDDEN: 'hidden',
});

export default {
    getCreationStatus() {
        if (!Security.isSecurityEnabled()) {
            return CreationStatus.DISABLED;
        }

        const user = User.current();

        if (user && user.permissions.pipeline.create()) {
            return CreationStatus.ENABLED;
        }

        return CreationStatus.HIDDEN;
    },
    isEnabled() {
        return this.getCreationStatus() === CreationStatus.ENABLED;
    },
    isDisabled() {
        return this.getCreationStatus() === CreationStatus.DISABLED;
    },
    isHidden() {
        return this.getCreationStatus() === CreationStatus.HIDDEN;
    },
};

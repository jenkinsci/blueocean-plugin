import { User, Security } from '@jenkins-cd/blueocean-core-js';

export default {
    isCreationEnabled() {
        const user = User.current();

        if (!Security.isSecurityEnabled()) {
            return true;
        }

        if (!user) {
            return false;
        }

        return user.permissions.pipeline.create();
    },
};

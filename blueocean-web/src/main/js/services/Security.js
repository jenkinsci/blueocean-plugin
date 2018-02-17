import { User, Security } from '../core-js';

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

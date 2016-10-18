/**
 * Created by cmeyers on 10/17/16.
 */
import React from 'react';
import ScmProvider from '../ScmProvider';

export default class GitScmProvider extends ScmProvider {

    getDisplayName() {
        return 'Git';
    }

    getComponentName() {
        return 'GitScmSteps';
    }
}

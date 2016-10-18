/**
 * Created by cmeyers on 10/17/16.
 */
import React from 'react';
import ScmProvider from '../ScmProvider';

export default class GithubScmProvider extends ScmProvider {

    getDisplayName() {
        return 'Github';
    }

    getComponentName() {
        return 'GithubScmSteps';
    }
}

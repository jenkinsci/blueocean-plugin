/**
 * Created by cmeyers on 10/21/16.
 */
import React from 'react';
import ScmProvider from '../ScmProvider';
import GitDefaultOption from './GitDefaultOption';
import GitDefaultFlow from './GitDefaultFlow';

export default class GitScmProvider extends ScmProvider {

    getDefaultOption() {
        return <GitDefaultOption />;
    }

    getDefaultFlow() {
        return <GitDefaultFlow />;
    }
}

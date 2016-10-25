/**
 * Created by cmeyers on 10/21/16.
 */
import React from 'react';
import ScmProvider from '../ScmProvider';
import GitDefaultOption from './GitDefaultOption';
import GitCreationFlow from './GitCreationFlow';

export default class GitScmProvider extends ScmProvider {

    getDefaultOption() {
        return <GitDefaultOption />;
    }

    getCreationFlow() {
        return <GitCreationFlow />;
    }
}

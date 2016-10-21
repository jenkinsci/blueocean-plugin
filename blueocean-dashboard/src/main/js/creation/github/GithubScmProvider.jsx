/**
 * Created by cmeyers on 10/17/16.
 */
import React from 'react';
import ScmProvider from '../ScmProvider';
import GithubDefaultOption from './GithubDefaultOption';
import GithubDefaultFlow from './GithubDefaultFlow';

export default class GithubScmProvider extends ScmProvider {

    getDefaultOption() {
        return <GithubDefaultOption />;
    }

    getDefaultFlow() {
        return <GithubDefaultFlow />;
    }
}

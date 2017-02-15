import React, { Component } from 'react';
import moment from 'moment';
// The following import target will be generate on build time, do not edit
import revisionInfo from '../../../target/classes/io/jenkins/blueocean/revisionInfo';
import { AppConfig } from '@jenkins-cd/blueocean-core-js';

export class DevelopmentFooter extends Component {
    render() {
        // testing basic integrity
        if (!revisionInfo || !revisionInfo.sha) {
            // TODO: At minimum we should return Jenkins version. Jenkins version is always present
             // in X-Hudson HTTP header. Something to be handled elsewhere during load time by
             // inspecting HTTP response headers
            return null;
        }

        let includeBranch = true;

        if(!revisionInfo.branch.includes('(no branch)')) {
            includeBranch = false;
        }
        return (
          <div className="development-footer">
              <span> {AppConfig.getConfig().version}&nbsp;</span>
              <span> &#183; Core {AppConfig.getJenkinsConfig().version}&nbsp;</span>
              <span> &#183; {revisionInfo.sha.substring(0,7)}&nbsp; </span>
              { includeBranch && <span> &#183; {revisionInfo.branch}&nbsp;</span> }
              <span> &#183; {moment(revisionInfo.timestamp).format('Do MMMM YYYY hh:mm A')}</span>
              
          </div>
        );
    }
}

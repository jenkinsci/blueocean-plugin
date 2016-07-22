import React, { Component } from 'react';
import moment from 'moment';
// The following import target will be generate on build time, do not edit
import revisionInfo from '../../../target/classes/io/jenkins/blueocean/revisionInfo';

export class DevelopmentFooter extends Component {
    render() {
        if (!revisionInfo || !revisionInfo.name) {
            // TODO: At minimum we should return Jenkins version. Jenkins version is always present
             // in X-Hudson HTTP header. Something to be handled elsewhere during load time by
             // inspecting HTTP response headers
            return null;
        }
        return (
          <div className="development-footer">
              <span>Built at {moment(revisionInfo.timestamp).format('Do MMMM YYYY hh:mm A')}&nbsp;</span>
              <span> &#183; {revisionInfo.branch}&nbsp;</span>
              <span> &#183; {revisionInfo.sha.substring(0,7)} </span>
          </div>
        );
    }
}

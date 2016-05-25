import React, { Component } from 'react';
import moment from 'moment';
// The following import target will be generate on build time, do not edit
import revisionInfo from '../../../target/classes/io/jenkins/blueocean/revisionInfo';

export class DevelopmentFooter extends Component {
    render() {
        if (!revisionInfo.name) {
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

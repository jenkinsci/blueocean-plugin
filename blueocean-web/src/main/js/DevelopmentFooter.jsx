import React, { Component } from 'react';
import moment from 'moment';
// The following import target will be generate on build time, do not edit
import revisionInfo from '../../../target/classes/io/jenkins/blueocean/revisionInfo';

export class DevelopmentFooter extends Component {
    render() {
        if (!revisionInfo.branch) {
            return null;
        }
        const styles = {
            footer: {
                width: '100%',
                zIndex: 3,
                position: 'fixed',
                bottom: 20,
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                color: '#777',
            },
        };
        return (
          <div id="development-footer" style={styles.footer} >
              <span>Built at {moment(revisionInfo.timeStamp).format('Do MMMM YYYY hh:mm A')}&nbsp;</span>
              <span> - {revisionInfo.branch}&nbsp;</span>
              <span> - {revisionInfo.sha.substring(0,7)} </span>
          </div>
        );
    }
}

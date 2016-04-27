import React, { Component } from 'react';
// The following import target will be generate on build time, do not edit
import revisionInfo from '../../../target/classes/io/jenkins/blueocean/revisionInfo';

export class DevelopmentFooter extends Component {
    render() {
        const styles = {
            footer: {
                zIndex: 3,
                backgroundColor: 'rgba(0, 0, 0, 0.2)',
                position: 'fixed',
                bottom: 0,
                right: 0,
                display: 'block'
            },
            span: {
                display: 'inline-block',
                marginRight: 4
            }
        };
        return (
          <div id="development-footer" style={styles.footer} >
              <span style={styles.span}>{revisionInfo.timestamp}, </span>
              <span style={styles.span}>{revisionInfo.branch}, </span>
              <span style={styles.span}>{revisionInfo.sha.substring(0,7)}, </span>
              <span style={styles.span}>{revisionInfo.author},</span>
              <span style={styles.span}>{revisionInfo.tag || 'development'}</span>
          </div>
        );
    }
}

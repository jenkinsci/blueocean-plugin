import env from './env';
import moment from 'moment'
import React, { Component } from 'react'

const revisionInfo = env.revisionInfo;

const styles = {
  footer: {
    zIndex: 3,
    backgroundColor: 'white',
    position: 'fixed',
    bottom: 0,
    display: 'block'
  },
  span: {
    display: 'inline-block',
    marginRight: 4
  }
};

export default class DevelopmentFooter extends Component {

  renderJenkinsBuildLink(revisionInfo) {
    if (revisionInfo.jenkinsUrl) {
      return <a href={revisionInfo.jenkinsUrl} target="_blank">{revisionInfo.jenkinsTag}</a>;
    } else {
      return revisionInfo.jenkinsTag;
    }
  };

  render() {
    if (env.NODE_ENV === 'production') return null;

    const buildTag = this.renderJenkinsBuildLink(revisionInfo);

    return (
      <div id="development-footer" style={styles.footer} >
        <span style={styles.span}>{revisionInfo.version}, </span>
        <span style={styles.span}>{moment(revisionInfo.timeStamp).format('DD.MM.YYYY HH:mm')}, </span>
        <span style={styles.span}>{revisionInfo.revision}, </span>
        <span style={styles.span}>{revisionInfo.branch}, </span>
        <span style={styles.span}>{buildTag}</span>
      </div>
    );
  }
};


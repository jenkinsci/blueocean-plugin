import React, { Component, PropTypes } from 'react';
import { Progress } from '@jenkins-cd/design-language';
import { logging } from '@jenkins-cd/blueocean-core-js';

import { scrollHelper } from '../../ScrollHelper';

const INITIAL_RENDER_CHUNK_SIZE = 100;
const INITIAL_RENDER_DELAY = 300;
const RENDER_CHUNK_SIZE = 500;
const RERENDER_DELAY = 17;

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.LogConsole');

export class LogConsole extends Component {

    constructor(props) {
        super(props);
        this.queuedLines = [];
        this.state = {
            lines: [],
            isLoading: false,
        };
        // we have different timeouts in this component, each will take its own workspace
        this.timeouts = {};
    }

    componentWillMount() {
        // We need a shallow copy of the ObservableArray to "cast" it down to normal array
        this._processLines(this.props.logArray);
        logger.warn('isArray', Array.isArray(this.props.logArray));
    }

    // componentWillReceiveProps does not return anything and return null is an early out, so disable lint complaining
    componentWillReceiveProps(nextProps) { // eslint-disable-line
        logger.warn('newProps isArray', Array.isArray(nextProps.logArray));
        // We need a shallow copy of the ObservableArray to "cast" it down to normal array
        const newArray = !Array.isArray(nextProps.logArray) ? nextProps.logArray.slice() : nextProps.logArray;
        const oldArray = !Array.isArray(this.props.logArray) ? this.props.logArray.slice() : this.props.logArray;
        // const newLines = newArray.filter((item) => !oldArray.has(item));
        // if have a new logArray, simply add it to the queue and wait for next tick
        this.queuedLines = this.queuedLines.concat(newArray.slice(oldArray.length));
        clearTimeout(this.timeouts.render);
        this.timeouts.render = setTimeout(() => {
            this._processNextLines();
        }, INITIAL_RENDER_DELAY);
    }

    componentWillUnmount() {
        this.clearThisTimeout();
    }

    clearThisTimeout() {
        clearTimeout(this.timeouts.scroll);
        clearTimeout(this.timeouts.render);
    }

    // initial method to create lines to render
    _processLines(lines) {
        this.setState({ isLoading: true });
        let newLines = lines;
        if (newLines && newLines.length > INITIAL_RENDER_CHUNK_SIZE) {
            // queue up all the lines and grab just the beginning to render for now
            this.queuedLines = this.queuedLines.concat(newLines);
            newLines = this.queuedLines.splice(0, INITIAL_RENDER_CHUNK_SIZE);
            clearTimeout(this.timeouts.render);
            this.timeouts.render = setTimeout(() => {
                this._processNextLines();
            }, INITIAL_RENDER_DELAY);
        } else {
            this.scroll();
            this.setState({ isLoading: false });
        }

        this.setState({
            lines: newLines,
        });
    }

    // generic method to render more lines if so
    _processNextLines() {
        // grab the next batch of lines and add them to what's already rendered, then re-render
        const renderedLines = this.state.lines || [];
        const nextLines = this.queuedLines.splice(0, RENDER_CHUNK_SIZE);
        const newLines = renderedLines.concat(nextLines);

        this.setState({
            lines: newLines,
        });

        // if more lines are queued, render again
        if (this.queuedLines.length) {
            clearTimeout(this.timeouts.render);
            this.timeouts.render = setTimeout(() => {
                this._processNextLines();
            }, RERENDER_DELAY);
        } else {
            this.setState({ isLoading: false });
        }
        this.scroll();
    }

    scroll() {
        const anchorName = window.location.hash;
        const stepReg = /log-([0-9]{1,})$/;
        const match = stepReg.exec(anchorName);
        /*
         * This will scroll to the bottom of the console diff
         * React needs the timeout to have the dom ready
         */
        if (this.props.scrollToBottom && !match) {
            this.timeouts.scroll = setTimeout(() => this.props.scrollBottom(), RERENDER_DELAY + 1);
        } else if (match) {
            // we need to scroll to a certain line now
            this.timeouts.scroll = this.props.scrollToAnchorTimeOut(RERENDER_DELAY + 1);
        }
    }
    render() {
        const { isLoading, lines } = this.state;
        const { prefix = '', hasMore = false, router, location, t } = this.props; // if hasMore true then show link to full log
        if (!lines) {
            logger.debug('no lines passed');
            return null;
        }
        logger.warn('render lines length', lines.length);
        // JENKINS-37925 - show more button should open log in new window
        // const logUrl = url && url.includes(suffix) ? url : `${url}${suffix}`;
        // JENKINS-41717 reverts above again
        // fulllog within steps are triggered by
        const logUrl = `#${prefix || ''}log-${0}`;

        return (<div className="log-wrapper">
            { isLoading && <div className="loadingContainer" id={`${prefix}log-${0}`}>
                <Progress />
            </div>}


            { !isLoading && <div className="log-body"><pre>
                { hasMore && <div key={0} id={`${prefix}log-${0}`} className="fullLog">
                    <a
                      className="btn-link inverse"
                      key={0}
                      onClick={() => {
                          logger.debug('location', { location, logUrl });
                          location.query.start = 0;
                          location.hash = logUrl;
                          router.push(location);
                      }}
                    >
                        {t('Show.complete.logs')}
                    </a>
                </div>}
                { !isLoading && lines.map((line, index) => <p key={index + 1} id={`${prefix}log-${index + 1}`}>
                    <div className="log-boxes">
                        <a
                          className="linenumber"
                          key={index + 1}
                          href={`#${prefix || ''}log-${index + 1}`}
                          name={`${prefix}log-${index + 1}`}
                          onClick={() => {
                              location.hash = `#${prefix || ''}log-${index + 1}`;
                              router.push(location);
                          }}
                        >
                        </a>
                        <span className="line">{line}</span>
                    </div>
                </p>)}
            </pre></div> }

        </div>);
    }
}

const { array, bool, string, func, shape } = PropTypes;
LogConsole.propTypes = {
    scrollToBottom: bool, // in case of long logs you can scroll to the bottom
    logArray: array,
    scrollToAnchorTimeOut: func,
    scrollBottom: func,
    prefix: string,
    hasMore: bool,
    router: shape,
    location: shape,
    url: string,
    t: func,
};

export default scrollHelper(LogConsole);

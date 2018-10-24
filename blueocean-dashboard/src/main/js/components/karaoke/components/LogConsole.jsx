import React, { Component, PureComponent, PropTypes } from 'react';
import { Progress, Linkify } from '@jenkins-cd/design-language';
import { logging } from '@jenkins-cd/blueocean-core-js';

import { scrollHelper } from '../../ScrollHelper';

import { makeReactChildren, tokenizeANSIString } from '../../../util/ansi';

const INITIAL_RENDER_CHUNK_SIZE = 100;
const INITIAL_RENDER_DELAY = 300;
const RENDER_CHUNK_SIZE = 500;
const RERENDER_DELAY = 17;

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.LogConsole');

class LogLine extends PureComponent {
    onClick = () => {
        const { prefix, index, router, location } = this.props;

        const loc2 = location; // For eslint 🙄
        loc2.hash = `#${prefix || ''}log-${index + 1}`;
        router.push(loc2);
    };

    render() {
        const { prefix, line, index } = this.props;
        const tokenized = tokenizeANSIString(line);
        const lineChunks = makeReactChildren(tokenized);

        return (
            <p id={`${prefix}log-${index + 1}`}>
                <div className="log-boxes">
                    <a
                        className="linenumber"
                        key={index + 1}
                        href={`#${prefix || ''}log-${index + 1}`}
                        name={`${prefix}log-${index + 1}`}
                        onClick={this.onClick}
                    />
                    {React.createElement(Linkify, { className: 'line ansi-color' }, ...lineChunks)}
                </div>
            </p>
        );
    }
}

LogLine.propTypes = {
    prefix: PropTypes.string,
    line: PropTypes.string,
    index: PropTypes.number,
    router: PropTypes.object,
    location: PropTypes.object,
};

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
        const lineArray = this.props.logArray !== undefined && this.props.logArray.slice();
        logger.debug('isArray props', Array.isArray(this.props.logArray), 'isArray after', Array.isArray(lineArray));
        this._processLines(lineArray);
    }

    // componentWillReceiveProps does not return anything and return null is an early out, so disable lint complaining
    componentWillReceiveProps(nextProps) {
        // eslint-disable-line
        logger.debug('newProps isArray', Array.isArray(nextProps.logArray));
        if (nextProps.logArray === undefined) {
            return;
        }

        // We need a shallow copy of the ObservableArray to "cast" it down to normal array
        const newArray = nextProps.logArray.slice();
        const oldLength = this.props.logArray && this.props.logArray.length || 0;
        // if have a new logArray, simply add it to the queue and wait for next tick
        this.queuedLines = this.queuedLines.concat(newArray.slice(oldLength));
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
        const { prefix = '', hasMore = false, router, location, t, currentLogUrl } = this.props; // if hasMore true then show link to full log
        if (!lines) {
            logger.debug('no lines passed');
            return null;
        }
        logger.debug('render lines length', lines.length);

        return (
            <div className="log-wrapper">
                {isLoading && (
                    <div className="loadingContainer" id={`${prefix}log-${0}`}>
                        <Progress />
                    </div>
                )}

                {!isLoading && (
                    <div className="log-body">
                        <pre>
                            {hasMore && (
                                <div id={`${prefix}log-${0}`} className="fullLog">
                                    <a className="btn-link inverse" key={0} target="_blank" href={`${currentLogUrl}?start=0`}>
                                        {t('Show.complete.logs')}
                                    </a>
                                </div>
                            )}
                            {!isLoading &&
                                lines.map((line, index) => (
                                    <LogLine key={index} prefix={prefix} index={index} line={line} router={router} location={location} />
                                ))}
                        </pre>
                    </div>
                )}
            </div>
        );
    }
}

const { array, bool, string, func, shape } = PropTypes;
LogConsole.propTypes = {
    scrollToBottom: bool, // in case of long logs you can scroll to the bottom
    logArray: array,
    currentLogUrl: string,
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

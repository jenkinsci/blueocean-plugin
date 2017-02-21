import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import Extensions from '@jenkins-cd/js-extensions';
import { Augmenter } from './karaoke/services/Augmenter';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.RunDetailsPipeline');

@observer
export class RunDetailsPipeline extends Component {

    constructor(props) {
        super(props);
        // we do not want to follow any builds that are finished
        this.state = { followAlong: props && props.result && props.result.state !== 'FINISHED' || false };
        this._handleKeys = this._handleKeys.bind(this);
        this._onScrollHandler = this._onScrollHandler.bind(this);
    }

    componentWillMount() {
        if (this.props.params) {
            this.augment(this.props);
        }
    }
    componentDidMount() {
        const { result } = this.props;
         if (!result.isQueued()) {
             // determine scroll area
            const domNode = ReactDOM.findDOMNode(this.refs.scrollArea);
            // add both listener, one to the scroll area and another to the whole document
            if (domNode) {
                domNode.addEventListener('wheel', this._onScrollHandler, false);
            }
            document.addEventListener('keydown', this._handleKeys, false);
         }
    }
    componentWillUnmount() {
        const domNode = ReactDOM.findDOMNode(this.refs.scrollArea);
        if (domNode) {
            domNode.removeEventListener('wheel', this._onScrollHandler);
        }
        document.removeEventListener('keydown', this._handleKeys);
    }
 // we bail out on arrow_up key
    _handleKeys(event) {
        if (event.keyCode === 38 && this.state.followAlong) {
            logger.debug('stop follow along by key up');
            this.setState({ followAlong: false });
        }
    }
    // need to register handler to step out of karaoke mode
    // we bail out on scroll up
    _onScrollHandler(elem) {
        if (elem.deltaY < 0 && this.state.followAlong) {
            logger.debug('stop follow along by scroll up');
            this.setState({ followAlong: false });
        }
    }
    augment(props) {
        const { result: run, pipeline, params: { branch } } = props;
        this.augmenter = new Augmenter(pipeline, branch, run);
    }

    render() {
        const { result: run, pipeline, params: { branch }, t } = this.props;
        const { router, location } = this.context;
        const commonProps = {
            scrollToBottom: this.state.followAlong || (run && run.result === 'FAILURE'),
            augmenter: this.augmenter,
            followAlong: this.state.followAlong,
            t,
            run,
            pipeline,
            branch,
            router,
            location,
        };
        logger.warn('xxx', this.props, commonProps, this.augmenter.run.isCompleted());
        if (this.augmenter.isFreeStyle) {
            return (<div ref="scrollArea">
                <Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.freestyle.provider',
                        ...commonProps,
                    }
                }
                />
            </div>);
        }
        if (this.augmenter.isPipeline) {
            return (<div ref="scrollArea">
                <Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.pipeline.provider',
                        ...commonProps,
                    }
                }
                />
            </div>);
        }
        return (
                <div ref="scrollArea">
                    DOH type not supported
                </div>
            );
    }
}

RunDetailsPipeline.propTypes = {
    pipeline: PropTypes.object,
    result: PropTypes.object,
    params: PropTypes.object,
    t: PropTypes.func,
};

RunDetailsPipeline.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object.isRequired, // From react-router
};

export default RunDetailsPipeline;

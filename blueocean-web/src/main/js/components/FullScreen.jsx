import React, { Component, PropTypes } from 'react';

import ReactCSSTransitionGroup from 'react-addons-css-transition-group';

const transitionClass = 'expand-in';
const transitionDuration = 150;

// FIXME: Move this component to JDL, and replace all instances of the *other* fullscreen component with this.

export class FullScreen extends Component {

    constructor(props) {
        super(props);
        this.transitionTimeout = undefined;
    }

    componentDidMount() {
        document.addEventListener('keyup', this.keyPressed, false);
    }

    componentWillReceiveProps(newProps) {
        const { isVisible } = newProps;

        if (isVisible !== this.props.isVisible) {
            clearTimeout(this.transitionTimeout);

            this.transitionTimeout = setTimeout(() => {
                this.transitionTimeout = undefined;
                this.forceUpdate();
                if (this.props.afterClose) {
                    this.props.afterClose();
                }
            }, transitionDuration);
        }
    }

    componentWillUnmount() {
        document.removeEventListener('keyup', this.keyPressed, false);
        if (this.transitionTimeout) {
            clearTimeout(this.transitionTimeout);
            this.transitionTimeout = undefined;
        }
    }

    keyPressed = (event) => {
        const { onDismiss } = this.props;

        if (onDismiss && event.keyCode === 27) {
            onDismiss();
        }
    };

    render() {
        /*
         The top div (FullScreen) escapes the containing document flow, the inner one (FullScreen-contents)
         wraps props.children in a single node for the sake of the animation, and defaults to width/height: 100%
         and background: white
         */

        const { children, style, isVisible } = this.props;

        // If transitionTimeout not null, we're still fading in/out
        if (!isVisible && !this.transitionTimeout) {
            return null;
        }

        const wrappedChildren = isVisible && (
                <div className="FullScreen-contents" style={style}>
                    {children}
                </div>
            );

        return (
            <div className="FullScreen">
                <ReactCSSTransitionGroup
                    transitionName={transitionClass}
                    transitionAppear
                    transitionAppearTimeout={transitionDuration}
                    transitionEnterTimeout={transitionDuration}
                    transitionLeaveTimeout={transitionDuration}
                >
                    { wrappedChildren }
                </ReactCSSTransitionGroup>
            </div>
        );
    }
}

FullScreen.propTypes = {
    isVisible: PropTypes.bool,
    children: PropTypes.node,
    style: PropTypes.object,
    afterClose: PropTypes.func, // Animation has finished, after owner sets visible false
    onDismiss: PropTypes.func, // Currently means "user pressed esc"
};

FullScreen.defaultProps = {
    isVisible: true,
};

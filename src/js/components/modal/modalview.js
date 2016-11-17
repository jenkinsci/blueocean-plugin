// @flow

import React, {Component, PropTypes} from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import defaultStyles from './styles';
import Header from './header';
import Body from './body';

// Typedefs
type Props = {
    afterClose?: () => void,
    afterOpen?: () => void,
    transitionClass?: string,
    transitionDuration?: number,
    beforeClose?: () => void,
    beforeOpen?: () => void,
    body?: string,
    children?: any, // We can refine this once the Flow team does :)
    hideOnOverlayClicked?: boolean,
    isVisible?: boolean,
    onOverlayClicked?: () => void,
    showOverlay?: boolean,
    styles?:
        {
            closeButtonStyle: Object,
            dialogStyles: Object,
            overlayStyles: Object,
            titleStyle: Object
        } | boolean,
    result?: string,
    title?: string,
    ignoreEscapeKey?: boolean
};

type State = {
    dismissing: boolean,
    isVisible: boolean
};

class ModalView extends Component {

    state: State;

    static defaultProps: Props = {
        styles: false,
        showOverlay: true,
        hideOnOverlayClicked: false,
        ignoreEscapeKey: false
    };

    constructor(props: Props) {
        super(props);
        this.state = {
            dismissing: false,
            isVisible: props.isVisible || false
        };
    }

    componentWillMount() {
        document.addEventListener("keydown", this._handleKeys, false);
    }

    componentWillUnmount() {
        document.removeEventListener("keydown", this._handleKeys, false);
    }

    _handleKeys:Function = (event) => {
        const { ignoreEscapeKey } = this.props;

        if (!ignoreEscapeKey && event.keyCode == 27) {
            this.hide();
        }
    };

    componentWillUpdate(nextProps: Props, nextState: State) {

        const {isVisible} = this.state;
        const {beforeOpen, beforeClose} = this.props;

        if (nextState.isVisible && !isVisible && beforeOpen) {
            beforeOpen();
        }

        if (!nextState.isVisible && isVisible && beforeClose) {
            beforeClose();
        }
    }

    componentDidUpdate(prevProps: Props, prevState: State) {

        const {isVisible} = this.state;
        const {afterOpen, afterClose} = this.props;

        if (!prevState.isVisible && isVisible && afterOpen) {
            afterOpen();
        }

        if (prevState.isVisible && !isVisible && afterClose) {
            afterClose();
        }
    }

    show() {
        this.setState({isVisible: true});
    }

    hide() {
        if (this.props.transitionClass) {
            this.setState({dismissing: true});
            // after transition finishes, "destroy" the component
            setTimeout(() => this.setState({isVisible: false}), this.props.transitionDuration);
        } else {
            this.setState({isVisible: false});
        }
    }

    onOverlayClicked() {
        const { hideOnOverlayClicked, onOverlayClicked } = this.props;

        if (hideOnOverlayClicked) {
            this.hide();
        }

        if (onOverlayClicked) {
            onOverlayClicked();
        }
    }

    getStyles() {
        const styleArray = [
            'dialogStyles',
            'overlayStyles',
            'closeButtonStyle',
            'titleStyle',
            'headerStyle',
            'contentStyle'
        ];
        const {styles = {}} = this.props;
        const noStyle = !styles;

        const returnObject = {};

        styleArray.map((item) => {
            returnObject[item] = !noStyle ? Object.assign({}, defaultStyles[item], styles[item]) : {};
        });

        return returnObject;

    }

    getParts(children: Array<any>, part: string) {
        return React.Children.map(children, (child) => {
            if (child.type instanceof Function && child.type.name === part) {
                return child;
            }
        });
    }

    renderDialog() {
        const {
            dialogStyles,
            closeButtonStyle,
            titleStyle,
            contentStyle,
            headerStyle,
        } =  this.getStyles();

        const {
            children,
            result = 'info',
            ...rest
        } = this.props;

        const head = this.getParts(children, 'Header');
        const body = this.getParts(children, 'Body');

        return (
            <div className="dialog" style={dialogStyles}>
                <div className={`header ${result.toLowerCase()}`} style={headerStyle}>
                    <a onClick={() => this.hide()}
                       role="button"
                       className="closeButton"
                       style={closeButtonStyle}>&times;</a>
                    <div className="header-content">
                    {
                        head && head[0] ? head : <Header {...titleStyle} {...rest}/>
                    }
                    </div>
                </div>
                <div className="content" style={contentStyle}>
                    {
                        body ? body[0] : <Body {...rest}>{children}</Body>
                    }
                </div>
            </div>
        );
    }

    render() {
        const {isVisible} = this.state;
        //early out
        if (!isVisible) {
            return null;
        }

        let overlay;
        let { transitionDuration } = this.props;

        const { overlayStyles } =  this.getStyles();
        const {
            transitionClass,
            ...rest
        } = this.props;

        if (isNaN(transitionDuration)) {
            transitionDuration = 300;
        }

        if (rest.showOverlay) {
            overlay = (<div
                className="overlayStyles"
                onClick={() => this.onOverlayClicked()}
                style={overlayStyles}
            />);
        }

        return (
            <section className="modalview">
                {overlay}

                { transitionClass ?
                    <ReactCSSTransitionGroup
                        transitionName={transitionClass}
                        transitionAppear
                        transitionAppearTimeout={transitionDuration}
                        transitionEnterTimeout={transitionDuration}
                        transitionLeaveTimeout={transitionDuration}
                    >
                        { !this.state.dismissing ?
                            this.renderDialog()
                        : null }
                    </ReactCSSTransitionGroup>
                :
                    this.renderDialog()
                }
            </section>
        );
    }
}

ModalView.displayName = 'ModalView';

ModalView.propTypes = {
    afterClose: PropTypes.func,
    afterOpen: PropTypes.func,
    beforeClose: PropTypes.func,
    beforeOpen: PropTypes.func,
    body: PropTypes.string,
    children: PropTypes.node,
    hideOnOverlayClicked: PropTypes.bool,
    isVisible: PropTypes.bool,
    onOverlayClicked: PropTypes.func,
    showOverlay: PropTypes.bool,
    styles: React.PropTypes.oneOfType([
        PropTypes.shape({
            closeButtonStyle: PropTypes.object,
            dialogStyles: PropTypes.object,
            overlayStyles: PropTypes.object,
            titleStyle: PropTypes.object
        }),
        PropTypes.bool,
    ]),
    transitionClass: PropTypes.string,
    transitionDuration: PropTypes.number,
    result: PropTypes.string,
    title: PropTypes.string,
    ignoreEscapeKey: PropTypes.bool
};

export {
    Body as ModalBody,
    defaultStyles as ModalStyles,
    ModalView,
    Header as ModalHeader,
};

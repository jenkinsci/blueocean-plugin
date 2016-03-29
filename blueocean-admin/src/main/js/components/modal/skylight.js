import React from 'react';
import styles from './styles';
import Header from './header';

class SkyLight extends React.Component {

    constructor(props) {
        super(props);
        this.state = {isVisible: false};
    }

    componentWillUpdate(nextProps, nextState) {

        const {isVisible} = this.state;
        const {beforeOpen, beforeClose} = this.props;

        if (nextState.isVisible && !isVisible && beforeOpen) {
            beforeOpen();
        }

        if (!nextState.isVisible && isVisible && beforeClose) {
            beforeClose();
        }
    }

    componentDidUpdate(prevProps, prevState) {

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
        this.setState({isVisible: false});
    }

    onOverlayClicked() {
        const {hideOnOverlayClicked, onOverlayClicked } = this.props;
        if (hideOnOverlayClicked) {
            this.hide();
            if (onOverlayClicked) {
                onOverlayClicked();
            }
        }

        if (onOverlayClicked) {
            onOverlayClicked();
        }
    }

    getStyles() {
        const styleArray = ['dialogStyles', 'overlayStyles', 'closeButtonStyle', 'titleStyle', 'headerStyle', 'contentStyle'];
        let returnObject = {};
        styleArray.map((item) =>{
            returnObject[item] = Object.assign({}, styles[item], this.props[item]);
        });
        return returnObject;

    }

    getParts(children, part) {
        return React.Children.map(children, (child) => {
            if (child.type instanceof Function && child.type.name === part) {
                return child;
            }
        });
    }

    render() {
        let overlay;

        const {
            dialogStyles,
            overlayStyles,
            closeButtonStyle,
            titleStyle,
            contentStyle,
            headerStyle,
        } = this.getStyles();


        if (this.state.isVisible) {
            overlayStyles.display = 'block';
            dialogStyles.display = 'block';
        } else {
            overlayStyles.display = 'none';
            dialogStyles.display = 'none';
        }

        if (this.props.showOverlay) {
            overlay = (<div onClick={() => this.onOverlayClicked()} style={overlayStyles}></div>);
        }

        const {
            props: {
                children,
                ...rest,
                },
            } = this;

        const head = this.getParts(children, 'Header');
        const body = this.getParts(children, 'Body');

        return (
            <section className="skylight-wrapper">
                {overlay}
                <div style={dialogStyles}>
                    <div style={headerStyle}>
                        <a onClick={() => this.hide()} role="button" style={closeButtonStyle}>&times;</a>
                        {
                            head ? head : <Header {...titleStyle} {...rest} close={() => this.hide()}/>
                        }
                    </div>
                    <div style={contentStyle}>
                        {
                            body ? body : <span>no body</span>
                        }
                    </div>

                </div>
            </section>
        );
    }
}

SkyLight.displayName = 'SkyLight';

SkyLight.propTypes = {
    afterClose: React.PropTypes.func,
    afterOpen: React.PropTypes.func,
    beforeClose: React.PropTypes.func,
    beforeOpen: React.PropTypes.func,
    closeButtonStyle: React.PropTypes.object,
    dialogStyles: React.PropTypes.object,
    hideOnOverlayClicked: React.PropTypes.bool,
    onOverlayClicked: React.PropTypes.func,
    overlayStyles: React.PropTypes.object,
    showOverlay: React.PropTypes.bool,
    title: React.PropTypes.string,
    titleStyle: React.PropTypes.object
};

SkyLight.defaultProps = {
    title: '',
    showOverlay: true,
    overlayStyles: styles.overlayStyles,
    dialogStyles: styles.dialogStyles,
    closeButtonStyle: styles.closeButtonStyle,
    hideOnOverlayClicked: false
};

export default SkyLight;

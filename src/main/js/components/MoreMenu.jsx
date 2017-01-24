import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import { Icon } from "@jenkins-cd/react-material-icons";

const transitionDuration = 100;

export class MoreMenu extends React.Component {
    constructor() {
        super();
        this.state = { showDropdown: false };
    }
    componentDidMount() {
        document.addEventListener('keydown', this.escapeListener = e => {
            if (this.state.showDropdown) {
                e = e || window.event;
                if (e.keyCode == 27) {
                    this.closePopover();
                    e.stopPropagation();
                }
            }
        });
        document.addEventListener('mousedown', this.clickListener = e => {
            if (this.state.showDropdown) {
                e = e || window.event;
                const parent = this.refs.dropdown;
                let elem = e.target;
                while (elem) {
                    if (elem === parent) {
                        return true; // continue
                    }
                    elem = elem.parentElement;
                }
                this.closePopover();
            }
        });
    }
    componentWillUnmount() {
        document.removeEventListener('keydown', this.escapeListener);
        document.removeEventListener('mousedown', this.clickListener);
    }
    openPopover() {
        this.setState({showDropdown: true});
    }
    closePopover() {
        this.setState({showDropdown: false});
    }
    render() {
        const children = [];
        if (this.state.showDropdown) {
            children.push(<div className="menu-dropdown" ref="dropdown">
                {this.props.children}
            </div>);
        }
        return (
            <div className="more-menu">
                <span onClick={e => this.state.showDropDown ? this.closePopover() : this.openPopover()}>
                    <Icon icon="more_horiz" size={22} />
                </span>
                <ReactCSSTransitionGroup
                    transitionName="more-menu"
                    transitionAppear
                    transitionAppearTimeout={transitionDuration}
                    transitionEnterTimeout={transitionDuration}
                    transitionLeaveTimeout={transitionDuration}
                >
                    {children}
                </ReactCSSTransitionGroup>
            </div>
        );
    }
}

MoreMenu.propTypes = {
    children: React.PropTypes.any,
};

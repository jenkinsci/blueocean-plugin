import React, { PropTypes } from 'react';
import debounce from 'lodash.debounce';
import { Icon } from '../Icon';

import {FloatingElement} from '../FloatingElement';
import KeyCodes from '../../KeyCodes';

const POSITION = {
    FIRST: 'first',
    PREV: 'prev',
    NEXT: 'next',
    LAST: 'last',

    values: () => {
        return [POSITION.FIRST, POSITION.PREV, POSITION.NEXT, POSITION.LAST];
    }
};

export class Dropdown extends React.Component {

    constructor(props) {
        super(props);

        this.buttonRef = null;
        this.thumbRef = null;
        this.menuRef = null;
        this.lastScrollTop = 0;

        this.state = {
            menuOpen: false,
            selectedOption: null,
        };
    }

    componentWillMount() {
        this._defaultSelection(this.props);
    }

    componentDidMount() {
        document.addEventListener('keydown', this._handleKeyEvent);
        document.addEventListener('mousedown', this._handleMouseEvent);
    }

    componentDidUpdate(prevProps, prevState) {
        if (this.state.menuOpen && !prevState.menuOpen) {
            this._setInitialFocus();
        }
    }

    componentWillUnmount() {
        document.removeEventListener('keydown', this._handleKeyEvent);
        document.removeEventListener('mousedown', this._handleMouseEvent);
    }

    get selectedOption() {
        return this.state.selectedOption;
    }

    _defaultSelection(props) {
        if (!this.state.selectedOption && props.defaultOption) {
            this.setState({
                selectedOption: props.defaultOption,
            });
        }
    }

    _toggleDropdownMenu() {
        if (this.state.menuOpen) {
            this._closeDropdownMenu();
        } else {
            this._openDropdownMenu();
        }
    }

    _openDropdownMenu() {
        this.setState({
            menuOpen: true,
        });
    }

    _closeDropdownMenu() {
        this.setState({
            menuOpen: false,
        });
    }

    // (note: also triggered via spacebar press when button has focus)
    _onDropdownMouseEvent = (event) => {
        // console.log('_onDropdownMouseEvent');
        // prevent navigation if anchor was clicked
        event.preventDefault();
        this._toggleDropdownMenu();
    };

    _handleKeyEvent = (event) => {
        // console.log('_handleKeyEvent', this.state.menuOpen);
        if (!this.state.menuOpen) {
            return;
        }

        const { keyCode } = event;

        switch (keyCode) {
            case KeyCodes.TAB:
                // tabbing while open will advance to the next element after this Dropdown
                this._closeDropdownMenu();
                break;
            case KeyCodes.ESC:
                this._closeDropdownMenu();
                break;
            // don't let arrow keys scroll the content; focus change will do that for us
            case KeyCodes.ARROW_DOWN:
                event.preventDefault();
                this._changeFocusPosition(POSITION.NEXT);
                break;
            case KeyCodes.ARROW_UP:
                event.preventDefault();
                this._changeFocusPosition(POSITION.PREV);
                break;
            // page/up down scrolls as normal but applies focus
            case KeyCodes.PAGE_DOWN:
            case KeyCodes.PAGE_UP:
                this._syncFocusAfterScroll();
                break;
            case KeyCodes.HOME:
                this._changeFocusPosition(POSITION.FIRST);
                break;
            case KeyCodes.END:
                this._changeFocusPosition(POSITION.LAST);
                break;
            case KeyCodes.SPACEBAR:
            case KeyCodes.ENTER:
                event.preventDefault();
                this._selectFocusedItem();
                break;
            default:
                break;
        }
    };

    _handleMouseEvent = (event) => {
        // console.log("_handleMouseEvent");
        const { clientX, clientY } = event;

        if (this.state.menuOpen) {
            const element = document.elementFromPoint(clientX, clientY);

            // close the dropdown only if the user clicked "outside" of it
            // (only if the button, thumb and menu was not clicked)
            // clicking those elements will actually close the it via different means
            const clickedOutsideDropdown = !this.buttonRef.contains(element) &&
                !this.thumbRef.contains(element) &&
                !this.wrapperRef.contains(element) &&
                !this.menuRef.contains(element);

            if (clickedOutsideDropdown) {
                this._closeDropdownMenu();
            }
        }
    };

    _onMenuScrollEvent = () => {
        this._syncFocusAfterScroll();
    };

    _syncFocusAfterScroll = debounce(() => {
        if (this.menuRef.scrollTop === this.lastScrollTop) {
            return;
        }

        const scrollDown = this.menuRef.scrollTop > this.lastScrollTop;
        this.lastScrollTop = this.menuRef.scrollTop;
        const rect = this.menuRef.getBoundingClientRect();
        const nextFocusItem = scrollDown ?
            document.elementFromPoint(rect.left + 1, rect.top + rect.height - 2) :
            document.elementFromPoint(rect.left + 1, rect.top + 1);

        this._focusListItem(nextFocusItem.parentNode);
    }, 200);

    _setInitialFocus() {
        // console.log('_setInitialFocus');
        if (this.state.selectedOption) {
            const selectedIndex = this.props.options.indexOf(this.state.selectedOption);
            const selectedListItem = this.menuRef.children[selectedIndex];
            this._focusListItem(selectedListItem);
        } else {
            this._changeFocusPosition(POSITION.FIRST);
        }
    }

    _changeFocusPosition(position) {
        if (POSITION.values().indexOf(position) === -1) {
            return;
        }

        if (position === POSITION.FIRST || !this.menuRef.contains(document.activeElement)) {
            const listItem = this.menuRef.children[0];
            this._focusListItem(listItem);
            return;
        }

        const allListItems = [].slice.call(this.menuRef.children);

        if (position === POSITION.NEXT || position === POSITION.PREV) {
            const focusedListItem = document.activeElement.parentNode;
            const focusedIndex = allListItems.indexOf(focusedListItem);
            const nextFocusIndex = focusedIndex + (position === POSITION.NEXT ? 1 : -1);

            if (0 <= nextFocusIndex && (nextFocusIndex <= allListItems.length - 1)) {
                const nextListItem = allListItems[nextFocusIndex];
                this._focusListItem(nextListItem);
            }
        } else if (position === POSITION.LAST) {
            this._focusListItem(allListItems[allListItems.length - 1]);
        }
    }

    _focusListItem(listItemNode) {
        // console.log('_focusListItem', listItemNode);
        if (this.menuRef.contains(listItemNode)) {
            // need to delay ~1 frame for the focus and scroll to be reliable
            setTimeout(() => {
                listItemNode.children[0].focus();

                const listItemRect = listItemNode.getBoundingClientRect();
                const menuRect = this.menuRef.getBoundingClientRect();

                // make the focused item "stick" to top or bottom edge
                if (listItemRect.top < menuRect.top) {
                    this.menuRef.scrollTop = listItemNode.offsetTop;
                } else if (listItemRect.bottom > menuRect.bottom) {
                    this.menuRef.scrollTop += listItemRect.bottom - menuRect.bottom;
                }
            }, 1000/60);
        }
    }

    /**
     * Updates the dropdown's state such that its selectedOption corresponds to the item which currently has focus.
     * @private
     */
    _selectFocusedItem() {
        if (this.menuRef.contains(document.activeElement)) {
            const allListItems = [].slice.call(this.menuRef.children);
            const focusedListItem = document.activeElement.parentNode;
            const focusedIndex = allListItems.indexOf(focusedListItem);
            const selectedOption = this.props.options[focusedIndex];
            this._applySelection(selectedOption, focusedIndex);
        }
    }

    _onMenuItemClick(event, option, index) {
        // prevent any navigation resulting from click
        event.preventDefault();
        this._applySelection(option, index);
    }

    _applySelection(option, index) {
        this.setState({
            selectedOption: option,
            menuOpen: false,
        });

        if (this.props.onChange) {
            this.props.onChange(option, index);
        }

        //restore the focus on the button element
        if (this.buttonRef && this.buttonRef.focus) {
            this.buttonRef.focus();
        }
    }

    _optionToLabel(option) {
        if (!option) {
            return '';
        }

        if (this.props.labelField) {
            return option[this.props.labelField];
        } else if (this.props.labelFunction) {
            return this.props.labelFunction(option);
        } else {
            return option.toString();
        }
    }

    render() {
        // console.log('render', this.state.menuOpen);
        const { disabled, options, style, title, footer = undefined } = this.props;
        const extraClass = this.props.className || '';
        const openClass = this.state.menuOpen ? 'Dropdown-menu-open' : 'Dropdown-menu-closed';
        const promptClass = !this.state.selectedOption ? 'Dropdown-placeholder' : '';

        const noOptions = !options || !options.length;
        const buttonDisabled = disabled || noOptions;
        const buttonLabel = this._optionToLabel(this.state.selectedOption) || this.props.placeholder;
        const buttonTitle = title || buttonLabel;
        const menuWidth = this.buttonRef && this.buttonRef.offsetWidth || 0;

        return (
            <div className={`Dropdown ${openClass} ${extraClass}`} style={style}>
                <button ref={button => { this.buttonRef = button; }}
                    className={`Dropdown-button ${promptClass}`}
                    disabled={buttonDisabled}
                    title={buttonTitle}
                    onClick={this._onDropdownMouseEvent}
                >
                    {buttonLabel}
                </button>

                <a ref={thumb => { this.thumbRef = thumb; }}
                   className="Dropdown-thumb"
                   onClick={this._onDropdownMouseEvent}
                >
                    <Icon icon="HardwareKeyboardArrowDown" size={16} />
                </a>

                { this.state.menuOpen &&
                <FloatingElement
                    targetElement={this.buttonRef}
                    positionFunction={positionMenu}
                    style={{width: menuWidth}}
                >
                    <div ref={wrapper => this.wrapperRef = wrapper}>
                        <ul
                            ref={list => { this.menuRef = list; }}
                            className="Dropdown-menu"
                            onWheel={this._onMenuScrollEvent}
                        >
                            { options && options.map((option, index) => {
                                const selectedClass = this.state.selectedOption === option ? 'Dropdown-menu-item-selected' : '';
                                const optionLabel = this._optionToLabel(option);

                                return (
                                    <li key={index} data-position={index} className={`${selectedClass}`}>
                                        <a className={`Dropdown-menu-item ${selectedClass}`}
                                           href="#"
                                           onClick={event => this._onMenuItemClick(event, option, index)}
                                        >
                                            {optionLabel}
                                        </a>
                                    </li>
                                );
                            })}
                        </ul>
                        { footer }
                    </div>
                </FloatingElement>
                }
            </div>
        );
    }

}

const BORDER_OFFSET:number = 2;

// eslint-disable-next-line max-len, no-unused-vars
function positionMenu(selfWidth:number, selfHeight:number, targetWidth:number, targetHeight:number, targetLeft:number, targetTop:number, viewportWidth:number, viewportHeight:number) {
    let newTop = targetTop + targetHeight + BORDER_OFFSET;
    if (selfHeight + BORDER_OFFSET > viewportHeight) {
        newTop = 0;
    } else if (newTop + selfHeight + BORDER_OFFSET > viewportHeight) {
        newTop = viewportHeight - (selfHeight + BORDER_OFFSET);
    }
    return {
        newLeft: targetLeft,
        newTop: newTop,
    };
}

Dropdown.propTypes = {
    className: PropTypes.string,
    style: PropTypes.object,
    placeholder: PropTypes.string,
    options: PropTypes.array,
    defaultOption: PropTypes.oneOfType([
        PropTypes.node,
        PropTypes.object,
    ]),
    title: PropTypes.string,
    labelField: PropTypes.string,
    labelFunction: PropTypes.func,
    disabled: PropTypes.bool,
    onChange: PropTypes.func,
    footer: PropTypes.element,
};

Dropdown.defaultProps = {
    placeholder: 'Select an option',
};

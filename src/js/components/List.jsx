// @flow

import React, { PropTypes } from 'react';
import Utils from '../Utils';

type Props = {
    className?: string,
    children?: ReactChildren,
    style: Object,
    data?: Array<Object>,
    labelFunction?: Function,
    disabled?: bool,
    keyFunction?: Function,
    defaultStyles: bool,
    defaultSelection?: Object,
    onItemSelect?: Function,
}

type State = {
    selectedItem?: Object,
};


/**
 * Checks whether the supplied child is scrolled above or below the parent's top/bottom edges.
 * @param parent
 * @param child
 * @returns {{above: boolean, below: boolean}}
 */
function isScrolledAboveOrBelow(parent, child) {
    const result = {
        above: false,
        below: false,
    };

    const childRect = child.getBoundingClientRect();
    const parentRect = parent.getBoundingClientRect();

    if (childRect.top < parentRect.top) {
        result.above = true;
    } else if (childRect.bottom > parentRect.bottom) {
        result.below = true;
    }

    return result;
}

/**
 * Control that displays a List of items and allows for selection.
 * Functions like a list of radio buttons from a keyboard accessibility standpoint.
 * By default it uses a simple renderer that converts each object to a string.
 * A custom renderer can be provided as a single React child to the element, e.g.
 *      <List data={data}>
 *          <MyRenderer />
 *      </List>
 *
 *      function MyRenderer(props) {
 *          return (
 *              <div>{props.listIndex} {props.listItem}</div>
 *          );
 *      }
 * Custom render will receive three props: listIndex, listItem and labelFunction (from parent)
 * A keyFunction is encouraged to generate a React key for each row. Default is to use listIndex.
 *
 * @property {string} className additional "class" to add to outermost element (alongside "List")
 * @property {array} children React children
 * @property {Object} style React style object
 * @property {array} [data] data to render in the list.
 * @property {Function} [labelFunction] converts each object to a string in the default renderer.
 * @property {Function} [keyFunction]
 * @property {boolean} [defaultStyles] set "false" to remove all default styling from the List.
 * @property {Object} [defaultSelection] item to select in the list by default
 * @property {Function} [onItemSelect] callback when an item is selected, receiving listIndex and listItem.
 */
export class List extends React.Component {

    props: Props;
    state: State;
    groupId: string;
    selfNode: Element;

    static defaultProps: Props = {
        style: {},
        defaultStyles: true,
        labelFunction: itemToLabel,
    };

    constructor(props:Props) {
        super(props);

        this.state = {};

        this.groupId = Utils.randomId('List');
    }

    componentWillReceiveProps(nextProps:Props) {
        const { selectedItem } = this.state;
        const { data } = nextProps;

        // if the selectedItem is not found in new data, discard it
        if (selectedItem && (!data || data.indexOf(selectedItem) === -1)) {
            this.setState({
                selectedItem: undefined,
            });
        }

        this._defaultSelection(nextProps);
    }

    componentDidMount() {
        this._defaultSelection(this.props);
    }

    get selectedIndex():number {
        const { selectedItem } = this.state;
        const { data } = this.props;

        return selectedItem && data ? data.indexOf(selectedItem) : -1;
    }

    get selectedItem():?Object {
        return this.state.selectedItem;
    }

    _storeSelfNode(node:Element) {
        this.selfNode = node;
    }

    _defaultSelection(nextProps:Props) {
        if (!this.state.selectedItem && nextProps.defaultSelection) {
            this.setState({
                selectedItem: nextProps.defaultSelection,
            });
        }
    }

    _onChangeSelection(event:Event, index:number, item:Object) {
        // Firefox and Safari don't apply focus after "change" so force it
        if (document.activeElement !== event.currentTarget && event.currentTarget instanceof HTMLElement) {
            event.currentTarget.focus();
        }

        this.setState({
            selectedItem: item
        });

        if (this.props.onItemSelect) {
            this.props.onItemSelect(index, item);
        }
    }

    _scrollFocusedItemIntoView(event:Event) {
        if (event.currentTarget instanceof Element && event.currentTarget.parentElement) {
            // get the .List-Item associated w/ the focused input[type="radio"]
            const parentElement:Element = event.currentTarget.parentElement;
            const items = parentElement.getElementsByClassName('List-Item');
            this._scrollNodeIntoView(items[0]);
        }
    }

    _scrollNodeIntoView(targetNode:HTMLElement) {
        const position = isScrolledAboveOrBelow(this.selfNode, targetNode);

        if (position.above || position.below) {
            targetNode.scrollIntoView(position.above);
        }
    }

    render() {
        const { children, data, disabled, keyFunction, labelFunction } = this.props;

        const childCount = React.Children.count(children);

        let childTemplate:React$Element<*>;

        if (childCount === 0) {
            childTemplate = <DefaultRenderer />;
        } else if (childCount === 1) {
            childTemplate = React.Children.only(children);
        } else {
            console.error(`'children' supplied to List must be zero or one elements`);
            return null;
        }

        const listClass = this.props.className || '';
        const selectedClass = this.state.selectedItem ? 'List-selected' : '';
        const defaultClass = this.props.defaultStyles ? 'u-default-list-container' : '';
        const disabledClass = disabled ? 'disabled' : '';

        return (
                <div
                    ref={node => this._storeSelfNode(node)}
                    className={`List ${selectedClass} ${listClass} ${defaultClass} ${disabledClass}`}
                    style={this.props.style}
                >
                { data && data.map((item, index) => {
                    const itemSelectedClass = item === this.state.selectedItem ? 'List-Item-selected' : '';
                    const keyValue = keyFunction ? keyFunction(item) : index;

                    return (
                        <label className="List-Row" key={keyValue}>
                            <input
                                type="radio"
                                name={this.groupId}
                                className="List-Radio cloak"
                                onChange={(event) => this._onChangeSelection(event, index, item)}
                                onFocus={event => this._scrollFocusedItemIntoView(event)}
                                disabled={disabled}
                                checked={!!itemSelectedClass}
                            />

                            <div className={`List-Item ${itemSelectedClass}`}>
                                {React.cloneElement(childTemplate, {
                                    listIndex: index,
                                    listItem: item,
                                    labelFunction,
                                })}
                            </div>
                        </label>
                    );
                })}
                </div>
        );
    }
}

List.propTypes = {
    className: PropTypes.string,
    children: PropTypes.element,
    style: PropTypes.object,
    data: PropTypes.array,
    disabled: PropTypes.bool,
    labelFunction: PropTypes.func,
    keyFunction: PropTypes.func,
    defaultStyles: PropTypes.bool,
    defaultSelection: PropTypes.any,
    onItemSelect: PropTypes.func,
};

function itemToLabel(item) {
    if (typeof item === 'string') {
        return item;
    } else if (item.toString) {
        return item.toString();
    }

    return '';
}


function DefaultRenderer(props) {
    return (
        <div className="List-DefaultRenderer">
            <span className="List-DefaultRenderer-text">{props.labelFunction(props.listItem)}</span>
        </div>
    );
}

DefaultRenderer.propTypes = {
    listIndex: PropTypes.number,
    listItem: PropTypes.oneOfType([PropTypes.string, PropTypes.object]),
    labelFunction: PropTypes.func,
};

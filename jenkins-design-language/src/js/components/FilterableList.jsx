// @flow

import React, { PropTypes } from 'react';
import debounce from 'lodash.debounce';

import { List } from './List';
import { TextInput } from './forms/TextInput';

type Props = {
    className?: string,
    children?: ReactChildren,
    style: Object,
    data?: Array<Object>,
    labelFunction?: Function,
    disabled?: boolean,
    keyFunction?: Function,
    defaultStyles?: boolean,
    defaultSelection?: Object,
    onItemSelect?: Function,

    listStyle: Object,
    filterFunction: Function,
    placeholder: string,
    emptyText: string,
};

type State = {
    text?: string,
};

function defaultFilterFunction(text, item) {
    return item && item.toString().indexOf(text) >= 0;
}

/**
 * Control that displays a List of items and allows for selection.
 * Includes a text field above for quick filtering of items in the list.
 * Provide a filterFunction that determines whether item should be displayed with text.
 * See "List" component for more details.
 *
 * @property {string} className additional "class" to add to outermost element (alongside "FilterableList")
 * @property {array} children React children
 * @property {Object} style React style object to be applied to outer element.
 * @property {array} [data] data to render in the list.
 * @property {Function} [labelFunction] converts each object to a string in the default renderer.
 * @property {Function} [keyFunction]
 * @property {boolean} [defaultStyles] set "false" to remove all default styling from the List.
 * @property {Object} [defaultSelection] item to select in the list by default
 * @property {Function} [onItemSelect] callback when an item is selected, receiving listIndex and listItem.
 *
 * @property {Object} listStyle React style object to be applied to child List.
 * @property {Function} filterFunction receives text, item; return true/false to show each item
 * @property {string} placeholder text to display in filter input when empty
 * @property {string} emptyText text to display when no matches occur
 */
export class FilterableList extends React.Component {
    props: Props;
    state: State;

    static defaultProps: Props = {
        style: {},
        listStyle: {},
        filterFunction: defaultFilterFunction,
        placeholder: 'Search...',
        emptyText: 'No matches.',
    };

    constructor(props: Props) {
        super(props);

        this.state = {};
    }

    _onFilterChange = debounce(text => {
        this.setState({
            text,
        });
    }, 250);

    render() {
        // passthrough props for List
        const { keyFunction, labelFunction, listStyle, defaultStyles, defaultSelection, onItemSelect } = this.props;
        const { data, disabled, emptyText, filterFunction, placeholder, style } = this.props;

        const { text } = this.state;
        const filtered = data ? data.filter(item => !text || filterFunction(text, item)) : [];
        const noMatches = text && filtered.length === 0;

        const listProps = {
            labelFunction,
            keyFunction,
            defaultStyles,
            defaultSelection,
            onItemSelect,
        };

        const outerClass = this.props.className || '';

        return (
            <div className={`FilterableList ${outerClass}`} style={style}>
                <TextInput placeholder={placeholder} disabled={disabled} onChange={text => this._onFilterChange(text)} iconLeft="ActionSearch" />

                {noMatches && emptyText && <div className="FilterableList-empty-text">{emptyText}</div>}

                {!noMatches && (
                    <List {...listProps} disabled={disabled} className="FilterableList-List" data={filtered} style={listStyle}>
                        {this.props.children}
                    </List>
                )}
            </div>
        );
    }
}

FilterableList.propTypes = {
    // from List
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
    // for FilterableList
    listStyle: PropTypes.object,
    filterFunction: PropTypes.func,
    placeholder: PropTypes.string,
    emptyText: PropTypes.string,
};

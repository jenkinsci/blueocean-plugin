import React, { Component, PropTypes } from 'react';
import {
    TextInput,
    Dropdown,
  } from '@jenkins-cd/design-language';
import { Icon } from '@jenkins-cd/react-material-icons'; 
import Autocomplete from 'react-autocomplete';

/**
 * Simple column filter
 */
export class ColumnFilter extends Component {
    constructor(props) {
        super(props);
        this.state = { value: props.value, visible: false };
    }
    
    componentWillReceiveProps(newProps) {
        if (this.state.value != newProps.value) {
            this.setState({ value: newProps.value });
        }
    }
    
    clearInput() {
        this.onChange({type:'select'}, '');
    }
    
    onChange(event, value) {
        const { onChange } = this.props;
        this.setState({value: value});
        // only update on enter press or click
        if (event.type === 'select'
            || event.type === 'blur'
            || (event.type === 'keypress' && event.key === 'Enter')) {
            onChange(value);
        }
    }
    
    focus(e) {
        this.setState({focused: true});
        e.target.select();
    }
    
    blur() {
        this.setState({focused: false});
    }
    
    // hack due to strange behavior of triggering onChange from autocomplete when
    // clicking on the input when the dropdown is open and an item is selected
    preventStupidInput(e) {
        if (this.state.visible && e.target.tagName && e.target.tagName.toLowerCase() === 'input') {
            this.refs.autocomplete._ignoreClick = true;
        }
    }
    
    handleEmptyEnterPressBetter(event) {
        if (this.state.visible && event.key === 'Enter' && this.state.value === '') {
            this.clearInput();
        }
    }
    
    render() {
        const { placeholder, options } = this.props;
        const { value, focused } = this.state;

        const style = {position: 'absolute'};
        return (<div className={`ColumnFilter ${value ? '' : 'empty'} ${focused ? 'focused' : ''}`}>
            <Autocomplete
                ref="autocomplete"
                value={value}
                inputProps={{
                    className: "autocomplete",
                    name: "Filter",
                    placeholder: focused ? '' : placeholder,
                    onMouseDown: e => this.preventStupidInput(e),
                    onKeyDown: e => this.handleEmptyEnterPressBetter(e),
                    onFocus: e => this.focus(e),
                    onBlur: e => this.blur(e)}}
                menuStyle={{
                    position: 'fixed',
                    overflow: 'auto',
                    maxHeight: '50%' }}
                items={options}
                autoHighlight={true}
                getItemValue={(item) => item}
                shouldItemRender={(item,value) => item.toLowerCase().indexOf(value.toLowerCase()) >= 0}
                onChange={(event, value) => this.setState({ value: value }) || this.onChange(event, value)}
                onSelect={value => this.setState({ value: value }) || this.onChange({type:'select'}, value)}
                onMenuVisibilityChange={e => this.setState({visible: !this.state.visible})}
                renderItem={(item, selected) => (
                  <div className={selected ? 'item selected' : 'item'} key={item}>{item}</div>
                )}
              />
            <span className="Icon-filter">
                <Icon icon="filter_list" size={15} />
            </span>
            <span className="Icon-clear" onClick={e => this.clearInput()}>
                <Icon icon="clear" size={15} />
            </span>
          </div>);
    }
}

ColumnFilter.propTypes = {
    placeholder: PropTypes.string,
    onChange: PropTypes.func,
    value: PropTypes.object,
    options: PropTypes.object,
};

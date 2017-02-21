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
        this.state = { value: props.value };
    }
    
    componentWillReceiveProps(newProps) {
        if (this.state.value != newProps.value) {
            this.setState({ value: newProps.value });
        }
    }
    
    onKeyPress(e) {
        this.setState({ value: e.target.value });
        setTimeout(x => this.refs.dropDown._openDropdownMenu(e), 10);
    }
    
    onChange(event, value) {
        const { onChange } = this.props;
        // only update on enter press or click
        if (event.type === 'select'
            || event.type === 'blur'
            || (event.type === 'keypress' && event.key === 'Enter')) {
            onChange(value);
        }
    }
    
    completionFilter(item) {
        if (!this.state.value || !item) {
            return true;
        }
        const str = item instanceof String ? item : item.toString();
        const out = str.toLowerCase().indexOf(this.state.value.toLowerCase()) >= 0;
        console.log('testing', str.toLowerCase(), 'against', this.state.value.toLowerCase(), 'and is', out);
        return out;
    }
    
    focus(e) {
        this.setState({focused: true});
        e.target.select();
    }
    
    blur() {
        this.setState({focused: false});
    }
    
    render() {
        const { placeholder, options } = this.props;
        const { value, focused } = this.state;

        const style = {position: 'absolute'};
        return (<div className={`ColumnFilter ${value ? '' : 'empty'} ${focused ? 'focused' : ''}`}>
            <Autocomplete
                value={value}
                inputProps={{
                    className: "autocomplete",
                    name: "Filter",
                    placeholder: focused ? '' : placeholder,
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
                renderItem={(item, selected) => (
                  <div className={selected ? 'item selected' : 'item'} key={item}>{item}</div>
                )}
              />
            <span className="Icon-filter">
                <Icon icon="filter_list" size={15} />
            </span>
            <span className="Icon-clear" onClick={e => this.onChange({type:'select'}, '')}>
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

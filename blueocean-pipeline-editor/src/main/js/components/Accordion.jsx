import React from 'react';
import { Icon } from '@jenkins-cd/design-language';

export class Accordion extends React.Component {
    static propTypes = {
        children: React.PropTypes.object,
    };
    constructor(props) {
        super(props);
        this.state = { selected: props.show && '.$' + props.show };
    }
    render() {
        const children = React.Children.toArray(this.props.children);
        let { selected } = this.state;
        if (!selected) selected = children && children[0].key;
        return (<div className="Accordion">
             {children.map(child => [
                <h4 className={`Label ${selected === child.key && 'active'}`}
                    onClick={() => { this.setState({ selected: child.key }); }}>
                    {child.props.title}
                    <Icon icon="NavigationExpandMore" size={24} />
                </h4>,
                <div className="Content">{child}</div>
            ])}
        </div>);
    }
}

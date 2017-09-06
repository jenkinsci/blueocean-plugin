import React from 'react';
import { Icon } from '@jenkins-cd/design-language';

export class Accordion extends React.Component {
    static propTypes = {
        children: React.PropTypes.object,
    };
    constructor(props) {
        super(props);
        this.state = { selected: undefined };
    }
    render() {
        const { children } = this.props;
        const { selected = children[0].key } = this.state;
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

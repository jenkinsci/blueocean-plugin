import * as React from 'react';
import { Icon } from '@jenkins-cd/design-language';

interface Props {
    children?: React.ReactNode;
    show?: string;
}

interface State {
    selected: string;
}
export class Accordion extends React.Component<Props, State> {
    static propTypes = {
        children: React.PropTypes.object,
    };
    constructor(props: Props) {
        super(props);

        this.state = { selected: (props.show && '.$' + props.show) || '' };
    }
    render() {
        const children = React.Children.toArray(this.props.children);
        let { selected } = this.state;
        if (!selected) selected = children && (children[0] as any).key;
        return (
            <div className="Accordion">
                {children.map((child: any) => [
                    <h4
                        className={`Label ${selected === child.key && 'active'}`}
                        onClick={() => {
                            this.setState({ selected: child.key });
                        }}
                    >
                        {child.props.title || child.props['data-label']}
                        <Icon icon="NavigationExpandMore" size={24} />
                    </h4>,
                    <div className="Content">{child}</div>,
                ])}
            </div>
        );
    }
}

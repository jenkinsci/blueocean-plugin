import React, {Component, PropTypes} from 'react';
import { storiesOf } from '@kadira/storybook';

import { FloatingElement } from '../components';


storiesOf('FloatingElement', module)
    .add('Above', () => <ExampleBasic position="above"/>)
    .add('Below', () => <ExampleBasic position="below"/>)
    .add('Left', () => <ExampleBasic position="left"/>)
    .add('Right', () => <ExampleBasic position="right"/>)
;

const triggerStyle = {
    display: 'block',
    position: 'fixed',
    minWidth: '0px',
    width: '100px',
    height: '100px',
    padding: '0px'
};

const triggerStyleCenter = {
    ...triggerStyle,
    left: '50%',
    top: '50%',
    marginLeft: '-50px',
    marginTop: '-50px'
};

const triggerStyleNW = {
    ...triggerStyle,
    left: '1em',
    top: '1em'
};

const triggerStyleNE = {
    ...triggerStyle,
    right: '1em',
    top: '1em'
};

const triggerStyleSW = {
    ...triggerStyle,
    left: '1em',
    bottom: '1em'
};

const triggerStyleSE = {
    ...triggerStyle,
    right: '1em',
    bottom: '1em'
};

const popoverStyle = {
    maxWidth: '22em'
};

class ExampleBasic extends Component {

    constructor(props) {
        super(props);

        this.state = {
            targetElement: null
        };
    }

    triggerClicked = (e) => {
        e.target.blur();
        this.setState({targetElement: e.target});
    };

    popoverDismissed = () => {
        this.setState({targetElement: null});
    };

    render() {

        const {targetElement} = this.state;
        const {position} = this.props;

        return (
            <div>
                <button style={triggerStyleNW} onClick={this.triggerClicked}>Trigger</button>
                <button style={triggerStyleNE} onClick={this.triggerClicked}>Trigger</button>
                <button style={triggerStyleCenter} onClick={this.triggerClicked}>Trigger</button>
                <button style={triggerStyleSW} onClick={this.triggerClicked}>Trigger</button>
                <button style={triggerStyleSE} onClick={this.triggerClicked}>Trigger</button>

                { targetElement &&
                <FloatingElement position={position}
                         targetElement={targetElement}
                         onDismiss={this.popoverDismissed}
                         style={popoverStyle}>
                    <div>
                        <h3>This is my Popover</h3>
                        <p>There are many like it, but this one is mine. The quick brown fox jumps over the lazy dog.</p>
                    </div>
                </FloatingElement>
                }
            </div>
        );
    }

    static propTypes = {
        position: PropTypes.string
    }
}

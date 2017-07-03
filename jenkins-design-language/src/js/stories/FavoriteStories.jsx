import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Favorite } from '../components';

storiesOf('Favorite', module)
    .add('styles', () => {
        const style = { padding: 10, backgroundColor: '#4A90E2', color: 'white' };

        return (
            <div>
                <div style={{padding: 10}}>
                    <Favorite label="Default" />
                </div>
                <div style={{padding: 10}}>
                    <Favorite checked label="Default, Checked" />
                </div>
                <div style={style}>
                    <Favorite className="dark-yellow"label="Dark / Yellow" />
                </div>
                <div style={style}>
                    <Favorite className="dark-yellow" checked label="Dark / Yellow, Checked" />
                </div>
                <div style={style}>
                    <Favorite className="dark-white" label="Dark / White" />
                </div>
                <div style={style}>
                    <Favorite className="dark-white" checked label="Dark / White, Checked" />
                </div>
            </div>
        );
    })
    .add('interactions', () => <Interactions />)
;

class Interactions extends React.Component {

    constructor(props) {
        super(props);

        this.fav = null;
    }

    render() {
        return (
            <div>
                <Favorite ref={fav => { this.fav = fav; }} label="Default" />
                <br />
                <button onClick={() => console.log('checked?', this.fav.checked)}>Checked?</button>
                <br />
                <Favorite label="Toggle Me" onToggle={(val) => console.log('onToggle', val)} />
            </div>
        );
    }
}

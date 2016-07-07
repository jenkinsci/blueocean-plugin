import React from 'react';
import { action, storiesOf } from '@kadira/storybook';
import { Favorite } from '../components';

storiesOf('Favorite', module)
    .add('default', () => {
        const style = { display: 'flex', alignItems: 'center' };
        const style2 = { ... style, backgroundColor: 'blue', color: 'white' };

        return (
            <div>
                <div style={style}>
                    <Favorite checked /> Checked
                </div>
                <div style={style}>
                    <Favorite onToggle={action('test')} /> With Callback
                </div>
                <div style={style}>
                    <Favorite /> Normal
                </div>
                <div style={style2}>
                    <Favorite className="dark-yellow" /> Dark, Yellow
                </div>
                <div style={style2}>
                    <Favorite className="dark-white" /> Dark, White
                </div>
            </div>
        );
    });

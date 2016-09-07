import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { Favorite } from '../components';

storiesOf('Favorite', module)
    .add('default', () => {
        const style = { display: 'flex', alignItems: 'center' };
        const style2 = { ... style, backgroundColor: '#4A90E2', color: 'white' };

        return (
            <div>
                <div style={style}>
                    <Favorite /> Default
                </div>
                <div style={style}>
                    <Favorite checked /> Default, Checked
                </div>
                <div style={style2}>
                    <Favorite className="dark-yellow" /> Dark / Yellow
                </div>
                <div style={style2}>
                    <Favorite className="dark-yellow" checked /> Dark / Yellow, Checked
                </div>
                <div style={style2}>
                    <Favorite className="dark-white" /> Dark / White
                </div>
                <div style={style2}>
                    <Favorite className="dark-white" checked /> Dark / White, Checked
                </div>
            </div>
        );
    });

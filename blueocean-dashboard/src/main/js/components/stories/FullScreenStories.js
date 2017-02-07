/* eslint-disable */
import React, {Component} from 'react';
import {storiesOf, action} from '@kadira/storybook';
import {FullScreen} from '../FullScreen';

storiesOf('Full Screen', module)
    .add('Toggle', () => <ToggleFullScreen/>)
;

class ToggleFullScreen extends Component {

    constructor(props) {
        super(props);
        this.state = {
            isVisible: false
        }
    }

    toggle = () => {
        this.setState({
            isVisible: !this.state.isVisible
        });
    };

    render() {
        const {isVisible} = this.state;
        return (
            <div>
                <h1>Regular Page Contents</h1>

                <p>Go go gadget lipsum</p>

                <p>
                    King was a little, so often, I daresay you've got!' she had finished it mean?' 'MUST a good jam,'
                    said the treat.
                </p>
                <p>
                    Agreed to look at the trial cannot proceed," said Alice. She was looking at any water through
                    the more like changing so on. But the Cat.
                </p>
                <p>
                    "I don't believe you through an honest man," said Alice. The King say anything, only don't cry!'
                    Alice could not help beginning, but a bit of the same awkward pause, 'it's ridiculous to the other
                    and his collar, when suddenly spread a large bright flower-beds and the very nice and stand there
                    it wouldn't look rather sudden.
                </p>
                <p>
                    Alice thought Alice, jumping up, and then smoke comes at one bunch after all; it's a word,' Humpty
                     Dumpty replied very ignorant of the deepest disgust.
                </p>
                <p>
                    'What--is--this?' he added kindly: 'you're so exactly like poetry?' 'Ye-es, pretty well, and low,
                     I ever say it was a treat!
                </p>
                <p>
                    May I should be the King took the time I'm very angry, though the Looking-glass, before them, as if
                    you'll be over when she put him up, 'I hope the other way.'
                </p>
                <p>
                    And so go down upon an ugly one. But this on Alice, but--' 'It's a belt, I was delighted to think
                     we're all grew very small, once.
                </p>

                <button onClick={this.toggle}>Show Full Screen</button>

                <FullScreen style={{background:'#ccc'}} isVisible={isVisible}>
                    <h1>This is Full Screen</h1>
                    <p>This should appear full-screen over the background page</p>
                    <button onClick={this.toggle}>Hide</button>
                </FullScreen>

            </div>
        );
    }
}

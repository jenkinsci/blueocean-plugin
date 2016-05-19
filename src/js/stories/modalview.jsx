import React from 'react';
import { action, storiesOf } from '@kadira/storybook';
import { ModalView, ModalHeader, ModalBody } from '../components/modal/modalview';

storiesOf('ModalView', module)
    .add('standard', scenario1)
    .add('transition: slide up', scenario2)
    .add('transition: fade in, slow', scenario3);

function scenario1() {
    return (
        <ModalView
            isVisible
        >
            <ModalHeader>
                <div>
                    Test Header
                </div>
            </ModalHeader>
            <ModalBody>
                <div>
                    Test Modal
                </div>
            </ModalBody>
        </ModalView>
    );
}

function scenario2() {
    return (
        <ModalView
            isVisible
            transitionClass="slideup"
        >
            <ModalHeader>
                <div>
                    Test Header
                </div>
            </ModalHeader>
            <ModalBody>
                <div>
                    Test Modal
                </div>
            </ModalBody>
        </ModalView>
    );
}

function scenario3() {
    return (
        <ModalView
            isVisible
            transitionClass="fadein"
            transitionDuration={1000}
        >
            <ModalHeader>
                <div>
                    Test Header
                </div>
            </ModalHeader>
            <ModalBody>
                <div>
                    Test Modal
                </div>
            </ModalBody>
        </ModalView>
    );
}
// @flow

import AbstractPositionStrategy from '../AbstractPositionStrategy';

export default class DropdownMenuPosition extends AbstractPositionStrategy {
    // eslint-disable-next-line max-len, no-unused-vars
    positionTarget(selfWidth:number, selfHeight:number, targetWidth:number, targetHeight:number, targetLeft:number, targetTop:number, viewportWidth:number, viewportHeight:number) {
        return {
            newLeft: targetLeft,
            newTop: targetTop + targetHeight,
        };
    }
}

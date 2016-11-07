// @flow

import AbstractPositionStrategy from './AbstractPositionStrategy';

export const positionValues = {
    above: 'above',
    below: 'below',
    left: 'left',
    right: 'right'
};

type Position = $Keys<typeof positionValues>;

export const positions: Array<Position> = Object.keys(positionValues);

export function sanitizePosition(input:Position) {

    if (positions.indexOf(input) === -1) {
        return positionValues.above;
    }

    return input;
}

/**
 * Positioning strategy that places target above, below, left or right of target
 * while ensuring it is not placed outside of the viewport.
 */
class SimplePositionStrategy extends AbstractPositionStrategy {

    constructor(position:Position) {
        super();
        this.position = position;
    }

    positionTarget(selfWidth, selfHeight, targetWidth, targetHeight, targetLeft, targetTop, viewportWidth, viewportHeight) {
        let newLeft, newTop;
        const margin = 5; // PX
        const preferred = sanitizePosition(this.position || positionValues.above);

        // Initial calculations
        switch (preferred) {
            default:
            case positionValues.above:
                newLeft = targetLeft - Math.floor((selfWidth - targetWidth) / 2);
                newTop = targetTop - selfHeight - margin;
                break;
            case positionValues.below:
                newLeft = targetLeft - Math.floor((selfWidth - targetWidth) / 2);
                newTop = targetTop + targetHeight + margin;
                break;
            case positionValues.left:
                newLeft = targetLeft - selfWidth - margin;
                newTop = targetTop - Math.floor((selfHeight - targetHeight) / 2);
                break;
            case positionValues.right:
                newLeft = targetLeft + targetWidth + margin;
                newTop = targetTop - Math.floor((selfHeight - targetHeight) / 2);
                break;
        }

        // Do a basic adjustment to make sure it's within the viewport if possible
        if (newLeft < margin) {
            newLeft = margin;
        } else if (newLeft + selfWidth + margin > viewportWidth) {
            newLeft = viewportWidth - selfWidth - margin;
        }

        if (newTop < margin) {
            newTop = margin;
        } else if (newTop + selfHeight + margin > viewportHeight) {
            newTop = viewportHeight - selfHeight - margin;
        }

        // Wishlist: Try other preferred positions rather than just shifting
        // into viewport?

        return {
            newLeft,
            newTop,
        };
    }

}

export default {
    above: new SimplePositionStrategy(positionValues.above),
    below: new SimplePositionStrategy(positionValues.below),
    left: new SimplePositionStrategy(positionValues.left),
    right: new SimplePositionStrategy(positionValues.right)
};

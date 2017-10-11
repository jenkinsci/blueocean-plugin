// @flow

export const positionValues = {
    above: 'above',
    below: 'below',
    left: 'left',
    right: 'right',
};

type Position = $Keys<typeof positionValues>;

export const positions: Array<Position> = Object.keys(positionValues);

export function sanitizePosition(input: Position) {
    if (positions.indexOf(input) === -1) {
        return positionValues.above;
    }

    return input;
}

function makePosition(position: Position) {
    // eslint-disable-next-line max-len, unused-var
    return function simplePositionFunction(
        selfWidth: number,
        selfHeight: number,
        targetWidth: number,
        targetHeight: number,
        targetLeft: number,
        targetTop: number,
        viewportWidth: number,
        viewportHeight: number
    ) {
        let newLeft: number, newTop: number;
        const margin: number = 5; // PX
        const preferred = sanitizePosition(position || positionValues.above);

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
    };
}

export const PositionFunctions = {
    above: makePosition(positionValues.above),
    below: makePosition(positionValues.below),
    left: makePosition(positionValues.left),
    right: makePosition(positionValues.right),
};

// @flow

//
// Common Helper functions for building SVGs. Could include simple math funcs as well as higher-order components.
//

export function polarToCartesian(centerX: number, centerY: number, radius: number, angleInDegrees: number) {

    const angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;

    return {
        x: centerX + (radius * Math.cos(angleInRadians)),
        y: centerY + (radius * Math.sin(angleInRadians)),
    };
}

export function describeArcAsPath(x: number, y: number, radius: number, startAngle: number, endAngle: number) {
    const start = polarToCartesian(x, y, radius, endAngle);
    const end = polarToCartesian(x, y, radius, startAngle);

    const arcSweep = endAngle - startAngle <= 180 ? '0' : '1';

    const d = [
        'M', start.x, start.y,
        'A', radius, radius, 0, arcSweep, 0, end.x, end.y,
    ].join(' ');

    return d;
}

// Also export an OBJ of classes + funs, like Math
const SVG = {
    polarToCartesian,
    describeArcAsPath
};

export default SVG;

// @flow

export default class AbstractPositionStrategy {

    positionTarget(selfWidth, selfHeight, targetWidth, targetHeight, targetLeft, targetTop, viewportWidth, viewportHeight) {
        throw new Error("PositionStrategy subclass must implement positionTarget");
    }

}

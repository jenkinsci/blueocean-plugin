// @flow

export default class AbstractPositionStrategy {
    // eslint-disable-next-line max-len, no-unused-vars
    positionTarget(selfWidth:number, selfHeight:number, targetWidth:number, targetHeight:number, targetLeft:number, targetTop:number, viewportWidth:number, viewportHeight:number) {
        throw new Error("PositionStrategy subclass must implement positionTarget");
    }

}

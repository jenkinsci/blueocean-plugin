/**
 * Creates a function that returns the result of invoking the given functions
 * with the `this` binding of the created function, where each successive
 * invocation is supplied the return value of the previous.
 *
 * Basically, we’re just nesting functions inside each other so that when they’re called with a final value,
 * the result explodes outwards through each layer.
 *
 * @param {...(Function|Function[])} [funcs] The functions to invoke.
 * @returns {Function} Returns the new composite function.
 *
 * @example
 * const composed  = compose(
 *   translate(['jenkins.plugins.blueocean.dashboard.Messages'], { wait: true }),
 *   documentTitle
 * );
 *
 * export default composed(Pipelines);
 *
 */
const compose = (fn, ...rest) => (rest.length === 0 ? fn : (...args) => fn(compose(...rest)(...args)));

export default compose;

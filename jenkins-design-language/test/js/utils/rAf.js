// a poor man's polyfill for requestAnimationFrame

if (!window.requestAnimationFrame) {
    window.requestAnimationFrame = (callback) => {
        setTimeout(() => {
            if (callback) {
                callback();
            }
        }, 0);
    };
}

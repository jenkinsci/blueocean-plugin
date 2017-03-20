/**
 * This contains a bit that reuses the index.jelly progress bar
 * so React components can just add a <PageLoading/> indicator,
 * and overlapping loading should just extend the time, etc.
 */
let loadingCount = 0;
const timeouts = [];

// use a short timeout so fast connections aren't seeing
// flashes of the progress bar all the time
const delay = 350;
const loadbar = typeof document !== 'undefined' && document.getElementById('loadbar');

/**
 * Remove queued progress additions
 */
function clearTimeouts() {
    while (timeouts.length) {
        clearTimeout(timeouts.pop());
    }
}

/**
 * Add a timeout to transition the loading animation differently
 */
function setLoaderClass(c, t) {
    timeouts.push(setTimeout(() => {
        loadbar.classList.add(c);
    }, t));
}

/**
 * Simple mostly css-based loading animation
 */
export default {
    show() {
        if (loadbar) {
            if (loadingCount === 0) {
                // (re)start the loading animation
                clearTimeouts();
                loadbar.classList.remove('complete');
                setLoaderClass('go', delay); // these times need to match the index.jelly CSS definitions
                setLoaderClass('long', delay + 1000);
                setLoaderClass('longer', delay + 6000);
            }
            loadingCount++;
        }
    },

    hide() {
        if (loadbar) {
            if (loadingCount > 0) {
                loadingCount--;
            }
    
            if (loadingCount === 0) {
                // stop the loading animation
                clearTimeouts();
                setLoaderClass('complete', 10);
                timeouts.push(setTimeout(() => {
                    // The Element.classList is a read-only property
                    const classList = loadbar.classList;
                    if (classList && classList.length && classList.length > 0) {
                        // remove all items - compatible with older browser
                        classList.remove.apply(classList, [... classList]);
                    }
                }, 500));
            }
        }
    },

    // TODO should make this a stack to push/pop
    setDarkBackground() {
        if (loadbar) document.getElementsByTagName('body')[0].classList.add('loadbar-light');
    },

    setLightBackground() {
        if (loadbar) document.getElementsByTagName('body')[0].classList.remove('loadbar-light');
    },
};

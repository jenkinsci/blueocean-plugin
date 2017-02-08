/**
 * This contains a bit that reuses the index.jelly progress bar
 * so React components can just add a <PageLoading/> indicator,
 * and overlapping loading should just extend the time, etc.
 */
let loadingCount = 0;
const timeouts = [];

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
        document.getElementById('loadbar').classList.add(c);
    }, t));
}

/**
 * Simple mostly css-based loading animation
 */
export default {
    show() {
        if (loadingCount === 0) {
            // start the loading animation
            document.getElementById('loadbar').classList.remove('complete');
            clearTimeouts();
            // use a short timeout so fast connections aren't seeing
            // flashes of the progress bar all the time
            setLoaderClass('go', 250); // these times need to match the index.jelly CSS definitions
            setLoaderClass('long', 1250);
            setLoaderClass('longer', 6250);
        }
        loadingCount++;
    },

    hide() {
        if (loadingCount > 0) {
            loadingCount--;
        }

        if (loadingCount === 0) {
            // stop the loading animation
            clearTimeouts();
            setLoaderClass('complete', 10);
            timeouts.push(setTimeout(() => {
                // The Element.classList is a read-only property
                const classList = document.getElementById('loadbar').classList;
                if (classList && classList.length && classList.length > 0) {
                    const classListAsArray = new Array(classList.length);
                    for (let i = 0, len = classList.length; i < len; i++) {
                        classListAsArray[i] = classList[i];
                    }
                    // remove all items - compatible with older browser
                    classList.remove.apply(classList, classListAsArray);
                }
            }, 500));
        }
    },

    setDarkBackground() {
        document.getElementsByTagName('body')[0].classList.add('loadbar-light');
    },

    setLightBackground() {
        document.getElementsByTagName('body')[0].classList.remove('loadbar-light');
    },
};

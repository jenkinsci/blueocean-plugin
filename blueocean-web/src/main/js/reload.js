// Page reload
// See https://issues.jenkins-ci.org/browse/JENKINS-36054
//
// Simple timeout based window reload.
//

const CHECK_FREQUENCY = 1000;
const TOLERANCE = CHECK_FREQUENCY + 20000; 
let lastCheck = new Date();

function check() {
    setTimeout(() => {
        const now = new Date();
        if (now.getTime() - lastCheck.getTime() > TOLERANCE) {
            window.location.reload(true);
        } else {
            lastCheck = now;
            check();
        }
    }, CHECK_FREQUENCY);
}
if (!window.isDevelopmentMode) {
    check();
}

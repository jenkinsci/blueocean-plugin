import log from "loglevel";
try {
    // start the App
    require('./main.jsx');
} catch (e) {
    log.error('Error starting Blue Ocean.', e);
}

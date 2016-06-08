// Apply polyfills before we do anything.
require('./polyfills');

// Initialise the Blue Ocean app.
const init = require('./init.jsx');

init.initialize(() => {
    // start the App
    require('./main.jsx');
});



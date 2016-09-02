try {
    // Initialise the Blue Ocean app.
    const init = require('./init.jsx');

    init.initialize(() => {
        // start the App
        require('./main.jsx');
    });
} catch (e) {
    console.error('Error starting Blue Ocean.', e);
}



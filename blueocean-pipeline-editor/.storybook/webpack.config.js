const path = require('path');

module.exports = {
    module: {},
    resolve: {
        extensions: [
            '.js', // required by storybook
            '', '.jsx' // for blueocean files
        ],
    }
};

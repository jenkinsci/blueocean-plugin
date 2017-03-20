const path = require('path');

module.exports = {
    module: {},
    node: {
        net: "empty",
        dns: "empty",
        tls: "empty"
    },
    resolve: {
        extensions: [
            '.js', // required by storybook
            '', '.jsx' // for blueocean files
        ],
    }
};

const path = require('path');

module.exports = {
    module: {
        loaders: [
            {
                test: /\.css?$/,
                loaders: [ 'style', 'raw' ],
                include: path.resolve(__dirname, '../')
            }
        ]
    },
    resolve: {
        extensions: [
            '.js', // required by storybook
            '', '.jsx' // for blueocean files
        ],
    }
};

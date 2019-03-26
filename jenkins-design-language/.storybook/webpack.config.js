const path = require('path');
const autoprefixer = require('autoprefixer');
module.exports = ({ config, mode }) => {
    config.module.rules.push({
        test: /\.less$/,
        use: [
            require.resolve('style-loader'),
            {
                loader: require.resolve('css-loader'),
                options: {
                    importLoaders: 1,
                    url: (url, resourcePath) => {
                        // resourcePath - path to css file

                        // `url()` with `octicons` stay untouched because they get merged in later
                        return url.includes('octicons');
                    },
                },
            },
            {
                loader: require.resolve('postcss-loader'),
                options: {
                    ident: 'postcss',
                    postcss: {},
                    plugins: () => [
                        require('postcss-flexbugs-fixes'), // eslint-disable-line global-require
                        autoprefixer({
                            flexbox: 'no-2009',
                        }),
                    ],
                },
            },
            {
                loader: require.resolve('less-loader'),
            },
        ],
    });
    config.module.rules.push({
        test: /\.(ts|tsx)$/,
        use: [
            {
                loader: require.resolve('awesome-typescript-loader'),
            },
            // Optional
            {
                loader: require.resolve('react-docgen-typescript-loader'),
            },
        ],
    });
    config.resolve.extensions.push('.ts', '.tsx');
    return config;
};

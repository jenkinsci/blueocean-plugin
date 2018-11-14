const path = require('path');
var ProgressBarPlugin = require('progress-bar-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
    entry: {
        blueocean: './src/main/js/blueocean.js',
        iepolyfills: './src/main/js/ie/iepolyfills.js',
    },
    output: {
        pathinfo: false,
        path: path.resolve(__dirname, 'target/classes/io/jenkins/blueocean/'),
        filename: '[name].js',
    },
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                use: 'babel-loader',
            },
            {
                test: /\.tsx?$/,
                exclude: /node_modules/,
                use: {
                    loader: 'ts-loader',
                    options: {
                        transpileOnly: true,
                    },
                },
            },
            {
                test: /\.(less|css)$/,
                exclude: /node_modules/,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    {
                        loader: 'less-loader',
                    },
                ],
            },
            {
                test: /\.(eot|woff|woff2|svg|ttf)([\?]?.*)$/,
                loader: 'file-loader',
            },
            {
                test: /locales/,
                loader: '@alienfast/i18next-loader',
            },
        ],
    },
    plugins: [
        new ProgressBarPlugin(),
        new MiniCssExtractPlugin({
            filename: 'blueocean.css',
        }),
    ],
    resolve: {
        alias: {
            react: path.resolve('node_modules/react'),
        },
        extensions: ['.js', '.jsx', '.ts', '.tsx', '.less'],
    },
    node: {
        dns: 'empty',
        net: 'empty',
    },
};

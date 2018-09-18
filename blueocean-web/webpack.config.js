const path = require('path');
var ProgressBarPlugin = require('progress-bar-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
    entry: './src/main/js/blueocean.js',
    output: {
        pathinfo: false,
        path: path.resolve(__dirname, 'dist'),
        filename: 'blueocean.js',
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
        ],
    },
    plugins: [
        new ProgressBarPlugin(),
        new MiniCssExtractPlugin({
            filename: 'blueocean.css',
        }),
    ],
    resolve: {
        extensions: ['.js', '.jsx', '.ts', '.tsx', '.less'],
    },
    node: {
        dns: 'empty',
        net: 'empty',
    },
};

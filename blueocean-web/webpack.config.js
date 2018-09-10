const path = require('path');
var ProgressBarPlugin = require('progress-bar-webpack-plugin');

module.exports = {
    mode: 'development',
    entry: './src/main/js/blueocean.js',
  output: {   
    pathinfo: false,
      path: path.resolve(__dirname, 'dist'),
  filename: 'blueocean.bundle.js'
  },
  module: {
    rules: [
        { 
            test: /\.jsx?$/, 
            use: 'babel-loader'
        }, 
        {
            test: /\.tsx?$/,
            use: {
                loader: 'ts-loader',
            options: {
                transpileOnly: true
            }
        }
        }
    ]
  },
  plugins: [
    new ProgressBarPlugin()
  ],
  resolve: {
      extensions: ['.js', '.jsx', '.ts', '.tsx']
  },
  node: {
      dns: 'empty',
      net: 'empty'
  }
};
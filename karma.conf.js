// Karma configuration
// Generated on Wed Jun 01 2016 16:04:37 GMT-0400 (EDT)

module.exports = function (config) {
    config.set({

        basePath: '',

        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['mocha', 'browserify'],

        // include only tests here; browserify will find the rest
        files: [
            'test/**/*-spec.+(js|jsx)'
        ],

        exclude: [],

        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            'test/**/*-spec.+(js|jsx)': ['browserify']
        },

        browserify: {
            debug: true,
            transform: ['babelify'],
            extensions: ['.js', '.jsx'],
            // needed for enzyme
            configure: function (bundle) {
                bundle.on('prebundle', function () {
                    bundle.external('react/addons');
                    bundle.external('react/lib/ReactContext');
                    bundle.external('react/lib/ExecutionEnvironment');
                });
            }
        },

        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['mocha'],

        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: false,

        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,

        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['PhantomJS'],

        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true,

        // how many browser should be started simultaneous
        concurrency: Infinity
    })
};

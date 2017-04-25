"use strict";

/*
 Build file for Jenkins Blue Ocean Commons JavaScript.
 */
const gulp = require('gulp');
const gutil = require('gulp-util');
const sourcemaps = require('gulp-sourcemaps');
const babel = require('gulp-babel');
const less = require('gulp-less');
const rename = require('gulp-rename');
const copy = require('gulp-copy');
const del = require('del');
const runSequence = require('run-sequence');
const lint = require('gulp-eslint');
const Karma = require('karma').Server;
const jest = require('gulp-jest').default;
const fs = require('fs');
const minimist = require('minimist');

// Options, src/dest folders, etc

const config = {
    clean: ["coverage", "dist", "licenses", "reports"],
    react: {
        sources: "src/**/*.{js,jsx}",
        dest: "dist"
    },
    less: {
        sources: "src/less/core.less",
        watch: 'src/less/**/*.{less,css}',
        dest: "dist/assets/css",
    },
    copy: {
        less_assets: {
            sources: "src/less/**/*.svg",
            dest: "dist/assets/css"
        },
    },
    test: {
        sources: '.',
        match: ['**/?(*-)(spec|test).js?(x)'],
        output: 'reports/junit.xml',
    },
};


// Watch all

gulp.task("watch", ["clean-build"], () => {
    gulp.watch(config.react.sources, ["compile-react"]);
    gulp.watch(config.less.watch, ["less"]);
});

// Default to all

gulp.task("default", () =>
    runSequence("clean", "lint", "test", "build", "validate"));

// Clean and build only, for watching

gulp.task("clean-build", () =>
    runSequence("clean", "build", "validate"));

// Clean

gulp.task("clean", () =>
    del(config.clean));

// Testing

gulp.task("lint", () => (
    gulp.src([config.react.sources, config.test.sources])
        .pipe(lint())
        .pipe(lint.format())
        .pipe(lint.failAfterError())
));

gulp.task("test", ['test-jest']);

gulp.task("test-debug", ['test-jest-debug']);

gulp.task("test-fast", ['test-jest-fast']);

gulp.task("test-karma", (done) => {
    new Karma({
        configFile: __dirname + '/karma.conf.js',
    }, done).start();
});

gulp.task("test-karma-debug", (done) => {
    new Karma({
        configFile: __dirname + '/karma.conf.js',
        colors: true,
        autoWatch: true,
        singleRun: false,
        browsers: ['Chrome'],
    }, done).start();
});

function runJest(options) {
    const argv = minimist(process.argv.slice(2));
    options.testPathPattern = argv.test || null;

    return gulp.src(config.test.sources)
        .pipe(jest(options))
        .on('error', () => {
            process.exit(1);
        });
}

gulp.task('test-jest', () => {
    if (!process.env.JEST_JUNIT_OUTPUT) {
        process.env.JEST_JUNIT_OUTPUT = config.test.output;
    }

    runJest({
        config: {
            collectCoverage: true,
            testMatch: config.test.match,
            testResultsProcessor: 'jest-junit',
        },
    });
});

gulp.task('test-jest-fast', () =>
    runJest({
        notify: true,
        forceExit: true,
        config: {
            testMatch: config.test.match,
        },
    })
);

gulp.task('test-jest-debug', () =>
    runJest({
        runInBand: true,
        forceExit: true,
        config: {
            testMatch: config.test.match,
        },
    })
);


// Build all

gulp.task("build", ["compile-react", "less", "copy"]);

// Compile react sources

gulp.task("compile-react", () =>
    gulp.src(config.react.sources)
        .pipe(sourcemaps.init())
        .pipe(babel(config.react.babel))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.react.dest)));

gulp.task("less", () =>
    gulp.src(config.less.sources)
        .pipe(sourcemaps.init())
        .pipe(less())
        .pipe(rename("blueocean-core-js.css"))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.less.dest)));

gulp.task("copy", ["copy-less-assets"]);

gulp.task("copy-less-assets", () =>
    gulp.src(config.copy.less_assets.sources)
        .pipe(copy(config.copy.less_assets.dest, { prefix: 2 })));

// Validate contents
gulp.task("validate", () => {
    const paths = [
        config.react.dest,
    ];

    for (const path of paths) {
        try {
            fs.statSync(path);
        } catch (err) {
            gutil.log('Error occurred during validation; see stack trace for details');
            throw err;
        }
    }
});

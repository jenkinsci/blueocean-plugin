"use strict";

/*
 Build file for Jenkins Blue Ocean Commons JavaScript.
 */
const gulp = require('gulp');
const gutil = require('gulp-util');
const sourcemaps = require('gulp-sourcemaps');
const babel = require('gulp-babel');
const clean = require('gulp-clean');
const runSequence = require('run-sequence');
const lint = require('gulp-eslint');
const Karma = require('karma').Server;
const fs = require('fs');

// Options, src/dest folders, etc

const config = {
    react: {
        sources: "src/**/*.{js,jsx}",
        dest: "dist"
    },
    clean: ["dist", "licenses", "reports"],
    test: {
        sources: "test/**/*-spec.{js,jsx}"
    }
};

// Watch all

gulp.task("watch", ["clean-build"], () => {
   gulp.watch(config.react.sources, ["compile-react"]);
});

// Default to all

gulp.task("default", () =>
    runSequence("clean", "lint", "test", "build", "validate"));

// Clean and build only, for watching

gulp.task("clean-build", () =>
    runSequence("clean", "build", "validate"));

// Clean

gulp.task("clean", () =>
    gulp.src(config.clean, {read: false})
        .pipe(clean()));

// Testing

gulp.task("lint", () => (
    gulp.src([config.react.sources, config.test.sources])
        .pipe(lint())
        .pipe(lint.format())
        .pipe(lint.failAfterError())
));

gulp.task("test", ['test-karma']);

gulp.task("test-debug", ['test-karma-debug']);

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

// Build all

gulp.task("build", ["compile-react"]);

// Compile react sources

gulp.task("compile-react", () =>
    gulp.src(config.react.sources)
        .pipe(sourcemaps.init())
        .pipe(babel(config.react.babel))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.react.dest)));

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

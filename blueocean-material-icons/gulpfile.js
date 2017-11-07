"use strict";

process.env.SKIP_BLUE_IMPORTS = 'YES';
process.env.NODE_ENV = 'production';

/*
 Build file for BlueOcean Material Icons.
 */

const gulp = require('gulp');
const fs = require('fs');
const sourcemaps = require('gulp-sourcemaps');
const babel = require('gulp-babel');
const clean = require('gulp-clean');
const runSequence = require('run-sequence');
const lint = require('gulp-eslint');

// Options, src/dest folders, etc

const config = {
    clean: ["dist", "target"],
    react: {
        sources: "src/**/*.{js,jsx}",
        dest: "dist"
    },
    test: {
        sources: 'test/js',
    },
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

// Build all

gulp.task("build", ["compile-react"]);

// Compile react sources

gulp.task("compile-react", () =>
    gulp.src(config.react.sources)
        .pipe(sourcemaps.init())
        .pipe(babel(config.react.babel))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.react.dest)));

var builder = require('@jenkins-cd/js-builder');

builder.src([
    'src/js'
]);

builder.tests(config.test.sources);

// redefine 'lint' to check only react and test sources (avoid dist)
builder.defineTask("lint", () => (
    gulp.src([config.react.sources, config.test.sources])
        .pipe(lint())
        .pipe(lint.format())
        .pipe(lint.failAfterError())
));

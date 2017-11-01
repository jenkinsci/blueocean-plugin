"use strict";

process.env.SKIP_BLUE_IMPORTS = 'YES';

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
const fs = require('fs');

// Options, src/dest folders, etc

const config = {
    clean: ["dist", "target"],
    react: {
        sources: ["src/**/*.{js,jsx}", "!**/__mocks__/**"],
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


var builder = require('@jenkins-cd/js-builder');

builder.src([
    'src/js',
    'less']);

//
// Create the main bundle.
//
builder.bundle('src/js/index.js', 'blueocean-core-js.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/less/blueocean-core-js.less')
    .import('react@any', {
        aliases: ['react/lib/React'] // in case a module requires react through the back door
    })
    .import('react-dom@any')
    .import("react-router@any")
    .export("@jenkins-cd/js-extensions")
    .export("@jenkins-cd/logging")
    .export('mobx')

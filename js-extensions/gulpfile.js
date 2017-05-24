"use strict";

/*
 Build file for Jenkins Blue Ocean Commons JavaScript.
 */
const gulp = require('gulp');
const gutil = require('gulp-util');
const sourcemaps = require('gulp-sourcemaps');
const babel = require('gulp-babel');
const rename = require('gulp-rename');
const copy = require('gulp-copy');
const del = require('del');
const runSequence = require('run-sequence');
const lint = require('gulp-eslint');
const fs = require('fs');

// Options, src/dest folders, etc

const config = {
    clean: ["dist", "licenses", "reports"],
    react: {
        sources: "src/**/*.{js,jsx}",
        dest: "dist"
    }
};

// Watch all

gulp.task("watch", ["clean-build"], () => {
    gulp.watch(config.react.sources, ["compile-react"]);
});

// Default to all

gulp.task("default", () =>
    runSequence("clean", "build", "validate"));

// Clean and build only, for watching

gulp.task("clean-build", () =>
    runSequence("clean", "build", "validate"));

// Clean

gulp.task("clean", () =>
    del(config.clean));

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

//
// See https://github.com/jenkinsci/js-builder
//
var builder = require('@jenkins-cd/js-builder');
builder.src(['src', 'js-extensions/@jenkins-cd', 'js-extensions/@jenkins-cd/subs']);
builder.lang('es6');
//builder.lint('none');
//
//Redefine the "test" task to use mocha and support es6.
//We might build this into js-builder, but is ok here
//for now.
//
builder.defineTask('test', function() {
     var mocha = require('gulp-mocha');
     var babel = require('babel-core/register');
    
     // Allow running of a specific test
     // e.g.  gulp test --test pipelines
     // will run the pipelines-spec.js
     var filter = builder.args.argvValue('--test', '*');
    
     builder.gulp.src('spec/' + filter + '-spec.js')
     .pipe(mocha({
         compilers: { js: babel }
     })).on('error', function(e) {
         if (builder.isRetest()) {
             // ignore test failures if we are running retest.
             return;
         }
         throw e;
     });
});

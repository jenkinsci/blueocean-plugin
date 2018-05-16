'use strict';

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
const fs = require('fs');
const ts = require('gulp-typescript');
const tsProject = ts.createProject('./tsconfig.json');
const eslint = require('gulp-eslint');


// Options, src/dest folders, etc

const config = {
    react: {
        sources: ['src/**/*.{js,jsx}', '!**/__mocks__/**'],
        dest: 'dist',
    },
    ts: {
        sources: ['src/**/*.{ts,tsx}'],
        dest: 'dist',
        destBundle: 'target/tstemp',
    },
    less: {
        sources: 'src/less/core.less',
        watch: 'src/less/**/*.{less,css}',
        dest: 'dist/assets/css',
    },
    copy: {
        less_assets: {
            sources: 'src/less/**/*.svg',
            dest: 'dist/assets/css',
        },
    },
};

// Watch all

gulp.task('watch', ['build'], () => {
    gulp.watch(config.react.sources, ['compile-react']);
    gulp.watch(config.less.watch, ['less']);
});

// Default to all

gulp.task('default', ['validate']);

// Build all

gulp.task('build', ['compile-typescript', 'compile-react', 'less', 'copy']);

// Compile react sources

gulp.task('compile-react', () =>
    gulp
        .src(config.react.sources)
        .pipe(sourcemaps.init())
        .pipe(babel(config.react.babel))
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest(config.react.dest))
);

gulp.task('compile-typescript', () =>
    gulp
        .src(config.ts.sources)
        .pipe(tsProject())
        .pipe(gulp.dest(config.ts.dest))
);

gulp.task('copy-src', () => gulp.src('src/js/**/*').pipe(gulp.dest(config.ts.destBundle + '/js')));
gulp.task('compile-typescript-bundle', ['copy-src'], () =>
    gulp
        .src(config.ts.sources)
        .pipe(tsProject())
        .pipe(gulp.dest(config.ts.destBundle))
);

gulp.task('less', () =>
    gulp
        .src(config.less.sources)
        .pipe(sourcemaps.init())
        .pipe(less())
        .pipe(rename('blueocean-core-js.css'))
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest(config.less.dest))
);

gulp.task('copy', ['copy-less-assets']);

gulp.task('copy-less-assets', () => gulp.src(config.copy.less_assets.sources).pipe(copy(config.copy.less_assets.dest, { prefix: 2 })));


// Validate contents
gulp.task('validate', ['lint', 'test'], () => {
    const paths = [config.react.dest];

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

builder.defineTask('lint', () => gulp.src([process.cwd()+"/src/**/*.{js,jsx}", process.cwd()+"/test/**/*.{js,jsx}"])
    .pipe(eslint(process.cwd()+'/../.eslintrc'))
    .pipe(eslint.format())
    .pipe(eslint.results(function (results) {
        if (results.errorCount > 0 || results.warningCount > 0) {
            gutil.log(gutil.colors.magenta('Oops, there are some eslint errors/warnings:'));
            if (results.warningCount > 0) {
                gutil.log(gutil.colors.magenta('\tWarnings: ' + results.warningCount));
            }
            if (results.errorCount > 0) {
                gutil.log(gutil.colors.red('\tErrors:   ' + results.errorCount));
                process.exit(1);
            }
        }})))
builder.src([config.ts.destBundle, 'less']);

//
// Create the main bundle.
//

builder
    .bundle('target/tstemp/js/index.js', 'blueocean-core-js.js')
    .onStartup('./target/tstemp/js/bundleStartup.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('src/less/blueocean-core-js.less')
    .import('react@any', {
        aliases: ['react/lib/React'], // in case a module requires react through the back door
    })
    .import('react-dom@any')
    .import('react-router@any')
    .export('@jenkins-cd/js-extensions')
    .export('@jenkins-cd/logging')
    .export('mobx');

//megaultrahax
gulp.tasks['js_bundle_blueocean-core-js_bundle_1'].dep = ['compile-typescript-bundle'];

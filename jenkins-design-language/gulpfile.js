"use strict";

process.env.SKIP_BLUE_IMPORTS = 'YES';
process.env.NODE_ENV = 'production';

/*
 Build file for Jenkins Design Language theme.
 */

const gulp = require('gulp');
const gutil = require('gulp-util');
const fs = require('fs');
const sourcemaps = require('gulp-sourcemaps');
const babel = require('gulp-babel');
const less = require('gulp-less');
const rename = require('gulp-rename');
const copy = require('gulp-copy');
const svgmin = require('gulp-svgmin');
const lint = require('gulp-eslint');
const ts = require('gulp-typescript');
const tsProject = ts.createProject('./tsconfig.json');
// Options, src/dest folders, etc

const config = {
    react: {
        sources: "src/**/*.{js,jsx}",
        dest: "dist"
    },
    ts: {
        sources: "src/**/*.{ts,tsx}",
        dest: "dist"
    },
    less: {
        sources: "less/theme.less",
        watch: "less/**/*.less", // Watch includes as well as main
        dest: "dist/assets/css"
    },
    svgmin: {
        sources: "icons/**/*",
        dest: "icons/"
    },
    copy: {
        icons: {
            sources: "icons/**/*",
            dest: "dist/assets/"
        },
        octicons: {
            sources: "node_modules/octicons/build/font/octicons.{eot,woff,woff2,ttf,svg}",
            dest: "target/classes/io/jenkins/blueocean/"
        },
        fonts: {
            sources: "fonts/*.woff",
            dest: "dist/assets/fonts/"
        },
        componentDocFiles: {
            sources: "src/js/stories/doc-*.{md,ejs,jade}",
            dest: "website/component-docs/"
        },
        licenses_octicons: {
            sources: "node_modules/octicons/LICENSE",
            dest: "licenses/"
        },
        licenses_ofl: {
            sources: "fonts/OFL.txt",
            dest: "licenses/"
        }
    },
    test: {
        sources: 'test/js',
    },
};

// Default to all

var builder = require('@jenkins-cd/js-builder');

builder.src([
    'src/js',
    'less',
    'dist' // for icons & fonts; NOTE: would be nice to find another way to do this as files in dist creates issues for jest & eslint
]);

builder.tests(config.test.sources);

// redefine 'lint' to check only react and test sources (avoid dist)
builder.defineTask("lint", () => (
    gulp.src([config.react.sources, config.test.sources])
        .pipe(lint())
        .pipe(lint.format())
        .pipe(lint.failAfterError())
));


gulp.task("default", gulp.series("lint", "test", "build", "validate"));

// Build all

gulp.task("build", gulp.series("compile-typescript", "compile-react", "less", "copy"));

// Watch all

gulp.task("watch", gulp.series("build", () => {
   gulp.watch(config.react.sources, ["compile-react"]);
   gulp.watch(config.less.watch, ["less"]);
}));

// Watch only styles, for when you're using Storybook

gulp.task("watch-styles", gulp.series("build", () => {
   gulp.watch(config.less.watch, ["less"]);
}));

// Compile react sources

gulp.task("compile-react", () =>
    gulp.src(config.react.sources)
        .pipe(sourcemaps.init())
        .pipe(babel(config.react.babel))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.react.dest)));

gulp.task("compile-typescript", () =>
    gulp.src(config.ts.sources)
        .pipe(tsProject())
        .pipe(gulp.dest(config.ts.dest)));
// Build the CSS

gulp.task("less", () =>
    gulp.src(config.less.sources)
        .pipe(sourcemaps.init())
        .pipe(less())
        .pipe(rename("jenkins-design-language.css"))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.less.dest)));

// Optimize SVG
gulp.task("svgmin", () =>
    gulp.src(config.svgmin.sources)
        .pipe(svgmin())
        .pipe(gulp.dest(config.svgmin.dest)));

// Copy things

gulp.task("copy", ["copy-icons", "copy-octicons", "copy-fonts",
    "copy-componentDocFiles", "copy-licenses-octicons", "copy-licenses-ofl"]);

gulp.task("copy-icons", () =>
    gulp.src(config.copy.icons.sources)
        .pipe(copy(config.copy.icons.dest)));

gulp.task("copy-octicons", () =>
    gulp.src(config.copy.octicons.sources)
        .pipe(copy(config.copy.octicons.dest, {prefix: 4})));

gulp.task("copy-fonts", () =>
    gulp.src(config.copy.fonts.sources)
        .pipe(copy(config.copy.fonts.dest, {prefix: 1})));

gulp.task("copy-componentDocFiles", () =>
    gulp.src(config.copy.componentDocFiles.sources)
        .pipe(copy(config.copy.componentDocFiles.dest, {prefix: 3})));

gulp.task("copy-licenses-octicons", () =>
    gulp.src(config.copy.licenses_octicons.sources)
        .pipe(copy(config.copy.licenses_octicons.dest, {prefix: 1})));

gulp.task("copy-licenses-ofl", () =>
    gulp.src(config.copy.licenses_ofl.sources)
        .pipe(copy(config.copy.licenses_ofl.dest, {prefix: 1})));

// Validate contents

gulp.task("validate", () => {
    const paths = [
        config.less.dest,
        config.copy.fonts.dest,
        config.copy.octicons.dest,
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
// Create the main bundle.
//
builder.bundle('src/js/components/index.js', 'jenkins-design-language.js')
    .inDir('target/classes/io/jenkins/blueocean')
    .less('less/jenkins-design-language.less')
    .export('moment')
    .export('moment-duration-format')
    .export('react')
    .export('react-dom')
    .export('react-router')
    .export('react-addons-css-transition-group');

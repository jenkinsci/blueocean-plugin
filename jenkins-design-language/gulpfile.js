"use strict";

/*
 Build file for Jenkins Design Language theme.
 */

const gulp = require('gulp');
const sourcemaps = require('gulp-sourcemaps');
const babel = require('gulp-babel');
const concat = require('gulp-concat');
const less = require('gulp-less');
const clean = require('gulp-clean');
const runSequence = require('run-sequence');
const rename = require('gulp-rename');
const copy = require('gulp-copy');

// Options, src/dest folders, etc

const config = {
    react: {
        sources: "src/**/*.{js,jsx}",
        dest: "dist",
        babel: {
            presets: ["es2015", "react", "stage-0"]
        }
    },
    less: {
        sources: "less/theme.less",
        watch: "less/**/*.less", // Watch includes as well as main
        dest: "dist/assets/css"
    },
    copy: {
        icons: {
            sources: "icons/**/*",
            dest: "dist/assets/"
        },
        octicons: {
            sources: "node_modules/octicons/octicons/octicons.{eot,woff,ttf,svg,css}",
            dest: "dist/assets/"
        },
        normalize: {
            sources: "node_modules/normalize.css/normalize.css",
            dest: "dist/assets/css/"
        },
        fontsCSS: {
            sources: "fonts/*.css",
            dest: "dist/assets/css/"
        },
        fonts: {
            sources: "fonts/*.woff",
            dest: "dist/assets/fonts/"
        },
        licenses_octicons: {
            sources: "node_modules/octicons/LICENSE.txt",
            dest: "licenses/"
        },
        licenses_ofl: {
            sources: "fonts/OFL.txt",
            dest: "licenses/"
        }
    },
    clean: ["dist", "licenses"]
};

// Watch

gulp.task("watch", ["default"], () => {
   gulp.watch(config.react.sources, ["compile-react"]);
   gulp.watch(config.less.watch, ["less"]);
});

// Default to clean and build

gulp.task("default", () =>
    runSequence("clean", "build"));

// Clean

gulp.task("clean", () =>
    gulp.src(config.clean, {read: false})
        .pipe(clean()));

// Build all

gulp.task("build", ["compile-react", "less", "copy"]);

// Compile react sources

gulp.task("compile-react", () =>
    gulp.src(config.react.sources)
        .pipe(sourcemaps.init())
        .pipe(babel(config.react.babel))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.react.dest)));

// Build the CSS

gulp.task("less", () =>
    gulp.src(config.less.sources)
        .pipe(sourcemaps.init())
        .pipe(less())
        .pipe(rename("jenkins-design-language.css"))
        .pipe(sourcemaps.write("."))
        .pipe(gulp.dest(config.less.dest)));

// Copy things

gulp.task("copy", ["copy-icons", "copy-octicons", "copy-normalize", "copy-fontsCSS", "copy-fonts",
    "copy-licenses-octicons", "copy-licenses-ofl"]);

gulp.task("copy-icons", () =>
    gulp.src(config.copy.icons.sources)
        .pipe(copy(config.copy.icons.dest)));

gulp.task("copy-octicons", () =>
    gulp.src(config.copy.octicons.sources)
        .pipe(copy(config.copy.octicons.dest, {prefix: 2})));

gulp.task("copy-normalize", () =>
    gulp.src(config.copy.normalize.sources)
        .pipe(copy(config.copy.normalize.dest, {prefix: 2})));

gulp.task("copy-fontsCSS", () =>
    gulp.src(config.copy.fontsCSS.sources)
        .pipe(copy(config.copy.fontsCSS.dest, {prefix: 1})));

gulp.task("copy-fonts", () =>
    gulp.src(config.copy.fonts.sources)
        .pipe(copy(config.copy.fonts.dest, {prefix: 1})));

gulp.task("copy-licenses-octicons", () =>
    gulp.src(config.copy.licenses_octicons.sources)
        .pipe(copy(config.copy.licenses_octicons.dest, {prefix: 1})));

gulp.task("copy-licenses-ofl", () =>
    gulp.src(config.copy.licenses_ofl.sources)
        .pipe(copy(config.copy.licenses_ofl.dest, {prefix: 1})));











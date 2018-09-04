'use strict';

const gulp = require('gulp');
const gutil = require('gulp-util');
const sourcemaps = require('gulp-sourcemaps');
const babel = require('gulp-babel');
const less = require('gulp-less');
const rename = require('gulp-rename');
const copy = require('gulp-copy');
const fs = require('fs');
const ts = require('gulp-typescript');
const jest = require('gulp-jest').default;
const tsProject = ts.createProject('./tsconfig.json');
const eslint = require('gulp-eslint');

// Options, src/dest folders, etc

const config = {
    react: {
        sources: ['src/main/js/**/*.{js,jsx}', '!**/__mocks__/**'],
        dest: 'dist',
    },
    ts: {
        sources: ['src/main/js/**/*.{ts,tsx}'],
        dest: 'dist',
    },
    less: {
        sources: 'src/main/less/extensions.less',
        watch: 'src/main/less/**/*.{less,css}',
        dest: 'dist/assets/css',
    },
    copy: {
        less_assets: {
            sources: 'src/main/less/**/*.svg',
            dest: 'dist/assets/css',
        },
    },
};

// Watch all

gulp.task('watch', ['bundle'], () => {
    gulp.watch(config.react.sources, ['compile-react']);
    gulp.watch(config.less.watch, ['less']);
});

// Default to all

gulp.task('default', ['validate']);

// Build all

gulp.task('bundle', ['compile-typescript', 'compile-react', 'less', 'copy']);

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

gulp.task('less', () =>
    gulp
        .src(config.less.sources)
        .pipe(sourcemaps.init())
        .pipe(less())
        .pipe(rename('blueocean-pipeline-editor.css'))
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

gulp.task('test', () => {
    return gulp.src('src/test/js').pipe(
        jest({
            collectCoverageFrom: ['src/test/js/**/*.{js,jsx}'],
            testMatch: ['**/?(*-)(spec|test).js?(x)'],
            transform: {
                '^.+\\.tsx?$': '<rootDir>/node_modules/ts-jest/preprocessor.js',
                '^.+\\.jsx?$': 'babel-jest',
            },
            moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
        })
    );
});

// redefine 'lint' to check only react and test sources (avoid dist)
gulp.task('lint', () =>
    gulp
        .src([
            //process.cwd()+"/src/**/*.{js,jsx}",
            process.cwd() + '/src/test/js/**/*.{js,jsx}',
        ])
        .pipe(eslint(process.cwd() + '/../.eslintrc'))
        .pipe(eslint.format())
        .pipe(
            eslint.results(function(results) {
                if (results.errorCount > 0 || results.warningCount > 0) {
                    gutil.log(gutil.colors.magenta('Oops, there are some eslint errors/warnings:'));
                    if (results.warningCount > 0) {
                        gutil.log(gutil.colors.magenta('\tWarnings: ' + results.warningCount));
                    }
                    if (results.errorCount > 0) {
                        gutil.log(gutil.colors.red('\tErrors:   ' + results.errorCount));
                        process.exit(1);
                    }
                }
            })
        )
);

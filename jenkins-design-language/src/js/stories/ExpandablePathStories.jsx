/**
 * Created by cmeyers on 10/4/16.
 */
import React from 'react';
import { storiesOf } from '@kadira/storybook';
import { ExpandablePath } from '../components';

storiesOf('ExpandablePath', module)
    .add('short', () => {
        const path = 'jenkins / pipeline';
        return (
            <ExpandablePath path={path} />
        );
    })
    .add('long', () => {
        const path = 'jenkins / folder1 / folder2 / pipeline';
        return (
            <ExpandablePath path={path} />
        );
    })
    .add('long, with wrap', () => {
        const path = 'jenkins / long-folder-name1 / long-folder-name2 / long-folder-name3 / long-folder-name4 / pipeline';
        return (
            <div style={ { width: 250 } }>
                <ExpandablePath path={path} />
            </div>
        );
    })
    .add('short, hide first', () => {
        const path = 'jenkins / folder1';
        return (
            <ExpandablePath path={path} hideFirst />
        );
    })
    .add('long, hide first', () => {
        const path = 'jenkins / folder1 / folder2 / pipeline';
        return (
            <ExpandablePath path={path} hideFirst />
        );
    })
    .add('with uri-encoded parts', () => {
        const path = 'jenkins / Pipeline%20Jobs / pipeline1';
        return (
            <ExpandablePath path={path} />
        );
    })
    .add('style: with link', () => {
        const path = 'Jenkins / folder1 / folder2 / pipeline';
        return (
            <a href="http://jenkins.io" target="_blank">
                <ExpandablePath path={path} />
            </a>
        );
    })
    .add('style: large', () => {
        const path = 'Jenkins / folder1 / folder2 / pipeline';
        return (
            <div style={ { fontSize: 24 } }>
                <ExpandablePath path={path} iconSize={28} />
            </div>
        );
    })
    .add('style: dark theme', () => {
        const path = 'Jenkins / folder1 / folder2 / pipeline';
        return (
            <div style={ { background: '#4A90E2' } }>
                <ExpandablePath className="dark-theme" path={path} />
            </div>
        );
    })
    .add('custom label', () => {
        let path = 'Jenkins / folder1 / folder2 / pipeline';
        path = ExpandablePath.replaceLastPathElement(path, 'Fancy Pipeline Name');
        return (
            <a href="http://jenkins.io" target="_blank">
                <ExpandablePath path={path} />
            </a>
        );
    });

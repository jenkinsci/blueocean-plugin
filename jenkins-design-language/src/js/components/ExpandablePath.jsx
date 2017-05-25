/**
 * Created by cmeyers on 10/4/16.
 */
import React, { Component, PropTypes } from 'react';
import { Icon } from '@jenkins-cd/react-material-icons';

const SHOW_LABEL_CLASS = 'show-label';
const SHOW_FOLDER_CLASS = 'show-folder';

/**
 * ExpandablePath displays a list of path elements with built-in truncation logic.
 * By default the first, last and second-to-last path items will always be shown as labels.
 * Intermediate path items will be displayed as folder icons and expand to show labels on hover.
 *
 * Props:
 *      className: CSS class to put on root element
 *      iconSize: for folder icons, in pixels
 *      path: a forward-slash delimited string of a path, e.g. 'folder1/folder2/pipeline'
 *      hideFirst: set to true to display the first path element as a folder.
 *      uriDecode: set to true to show uri-decoded form of each path element.
 */
export class ExpandablePath extends Component {

    /**
     * Replace the last element in 'path' with 'label'.
     * Helpful for transforming a path to include a friendly display name.
     *
     * @param {string} path
     * @param {string} label
     * @returns {string}
     */
    static replaceLastPathElement(path, label) {
        return path ? path.split('/').slice(0, -1).concat(label).join('/') : '';
    }

    render() {
        if (!this.props.path) {
            return null;
        }

        const pathElements = this.props.path.split('/');
        const extraClass = this.props.className || '';

        return (
            <span className={`expandable-path ${extraClass}`}>
                <ul className="path-list">
                    { pathElements.map((pathElem, index, elements) => {
                        const isFirst = index === 0;
                        const isSecondLast = (index + 1) === (elements.length - 1);
                        const isLast = (index + 1) === elements.length;

                        const label = this.props.uriDecode ? decodeURIComponent(pathElem) : pathElem;

                        let displayClass = SHOW_FOLDER_CLASS;

                        if ((isFirst && !this.props.hideFirst) || isSecondLast || isLast) {
                            displayClass = SHOW_LABEL_CLASS;
                        }

                        return (
                            <li key={index} className={`path-item ${displayClass}`}>
                                <Icon size={this.props.iconSize} icon="folder" />
                                <span className="path-text">{label.trim()}</span>
                                <span className="separator">&nbsp;/&nbsp;</span>
                            </li>
                        );
                    })}
                </ul>
            </span>
        );
    }
}

ExpandablePath.propTypes = {
    className: PropTypes.string,
    iconSize: PropTypes.number,
    path: PropTypes.string,
    hideFirst: PropTypes.bool,
    uriDecode: PropTypes.bool,
};

ExpandablePath.defaultProps = {
    iconSize: 16,
    hideFirst: false,
    uriDecode: true,
};

import React, { PropTypes } from 'react';
import { Link } from 'react-router';

const LinkifiedText = (props) => {
    const { text, partialTextLinks, textLink } = props;
    const textWithPartialLinks = [];

    if (partialTextLinks && partialTextLinks.length) {
        let partialLinksIdString = '';
        const partialLinksObj = {};

        for (const link of partialTextLinks) {
            partialLinksIdString += `${link.id}|`;
            partialLinksObj[link.id] = link.url;
        }

        if (partialLinksIdString) {
            const partialLinksRegExpString = new RegExp(`(${partialLinksIdString.slice(0, -1)})`, 'gi');

            for (let commitMsgPart of text.split(partialLinksRegExpString)) {
                if (partialLinksObj[commitMsgPart]) {
                    textWithPartialLinks.push(<a href={partialLinksObj[commitMsgPart]} target="_blank">{commitMsgPart}</a>);
                } else {
                    if (commitMsgPart) {
                        if (textLink) {
                            textWithPartialLinks.push((<Link to={textLink} className="unstyled-link" >{commitMsgPart}</Link>));
                        } else {
                            textWithPartialLinks.push(commitMsgPart);
                        }
                    }
                }
            }
        }

        if (!textWithPartialLinks.length) {
            textWithPartialLinks.push((<Link to={textLink} className="unstyled-link" >{text}</Link>));
        }

        return (<span>{textWithPartialLinks}</span>);
    }

    return (textLink ? (<Link to={textLink} className="unstyled-link" >{text}</Link>) : (<span>{text}</span>));
};

LinkifiedText.propTypes = {
    partialTextLinks: PropTypes.object,
    text: PropTypes.string.isRequired,
    textLink: PropTypes.string,
};

export default LinkifiedText;

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
                commitMsgPart = commitMsgPart.replace(/ /g, '\u00A0'); //replace empty space chars with &nbsp;

                if (partialLinksObj[commitMsgPart]) {
                    textWithPartialLinks.push(<a href={partialLinksObj[commitMsgPart]} target="_blank"><span className="ellipsis-text">{commitMsgPart}</span></a>);
                } else {
                    if (commitMsgPart) {
                        if (textLink) {
                            textWithPartialLinks.push((<Link to={textLink} className="unstyled-link" ><span className="ellipsis-text">{commitMsgPart}</span></Link>));
                        } else {
                            textWithPartialLinks.push(commitMsgPart);
                        }
                    }
                }
            }
        }

        if (!textWithPartialLinks.length) {
            textWithPartialLinks.push((<Link to={textLink} className="unstyled-link" ><span className="ellipsis-text">{text}</span></Link>));
        }

        return (<span>{textWithPartialLinks}</span>);
    }

    return (textLink ? (<Link to={textLink} className="unstyled-link" ><span className="ellipsis-text">{text}</span></Link>) : (<span>{text}</span>));
};

LinkifiedText.propTypes = {
    partialTextLinks: PropTypes.object,
    text: PropTypes.string.isRequired,
    textLink: PropTypes.string,
};

export default LinkifiedText;

import React, { PropTypes } from 'react';
import { Link } from 'react-router';

const LinkifiedText = (props) => {
    const { text, issues, textLink } = props;
    const textWithIssues = [];

    if (issues) {
        let issuesIdString = '';
        const issuesObj = {};

        for (const issue of issues) {
            issuesIdString += `${issue.id}|`;
            issuesObj[issue.id] = issue.url;
        }

        if (issuesIdString) {
            const issuesRegExpString = new RegExp(`(${issuesIdString.slice(0, -1)})`, 'gi');

            for (let commitMsgPart of text.split(issuesRegExpString)) {
                if (issuesObj[commitMsgPart]) {
                    textWithIssues.push(<a href={issuesObj[commitMsgPart]} target="_blank">{commitMsgPart}</a>);
                } else {
                    if (commitMsgPart) {
                        if (textLink) {
                            textWithIssues.push((<Link to={textLink} className="unstyled-link" >{commitMsgPart}</Link>));
                        } else {
                            textWithIssues.push(commitMsgPart);
                        }
                    }
                }
            }
        }

        return (<span>{textWithIssues}</span>);
    }

    return (textLink ? (<Link to={textLink} className="unstyled-link" >{text}</Link>) : (<span>{text}</span>));
};

LinkifiedText.propTypes = {
    issues: PropTypes.object,
    text: PropTypes.string.isRequired,
    textLink: PropTypes.string,
};

export default LinkifiedText;

import React from 'react';
import { Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import { Icon, LiveStatusIndicator } from '@jenkins-cd/design-language';

const Button = ({ icon, onClick }) => (
    <span onClick={onClick} className="bundle-watch-icon">
        <Icon icon={icon} size={18} />
    </span>
);

Button.propTypes = {
    icon: React.PropTypes.string,
    onClick: React.PropTypes.func,
};

export default class BundleWatchProgress extends React.Component {
    static propTypes = {
        icon: React.PropTypes.any,
        onClick: React.PropTypes.any,
    };

    since = 0;
    updateSpeed = 500;

    constructor() {
        super();
        this.state = { builds: [], expanded: {}};
    }

    componentWillMount() {
        const logFollower = () => {
            this.fetchLogs();
            this.timeout = setTimeout(logFollower, this.updateSpeed);
        };
        logFollower();
    }

    componentWillUnmount() {
        clearTimeout(this.timeout);
    }

    fetchLogs() {
        Fetch.fetchJSON(`${UrlConfig.getRestBaseURL()}/bundle:watch/?since=${this.since}`)
            .then(response => {
                const builds = [];
                let updated = false;
                let since = this.since;
                let building = false;
                for (const log of response) {
                    const old = this.state.builds.filter(l => l.name === log.name)[0] || { log: '' };
                    if (log.since > since) {
                        since = log.since;
                        updated = true;
                    }
                    if (log.building) {
                        building = true;
                    }
                    if (old.building != log.building) {
                        updated = true;
                    }
                    log.log = old.log + log.log;
                    builds.push(log);
                }
                if (building) {
                    this.updateSpeed = 100;
                } else {
                    this.updateSpeed = 500;
                }
                if (updated) {
                    this.since = since;
                    this.setState({ builds }, () => this.scrollToBottomOfLogs());
                }
            });
    }

    scrollToBottomOfLogs() {
        var logs = document.getElementsByClassName('bundle-watch-log');
        for (var i = 0; i < logs.length; i++) {
            var elem = logs[i];
            elem.scrollTop = elem.scrollHeight;
        }
    }

    reRunBundle(name) {
        if (!this.state.expanded[name]) {
            this.toggleLog(name);
        }
        Fetch.fetchJSON(`${UrlConfig.getRestBaseURL()}/bundle:watch/`, { fetchOptions: {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name })}})
        .then(() => this.fetchLogs());
    }

    clearLogs(name) {
        const old = this.state.builds.filter(l => l.name === name)[0];
        if (old) old.log = '';
        this.forceUpdate();
        Fetch.fetchJSON(`${UrlConfig.getRestBaseURL()}/bundle:watch/`, { fetchOptions: {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name })}})
        .then(() => {
            this.fetchLogs()
        });
    }

    toggleLog(name) {
        const expanded = { ...this.state.expanded };
        expanded[name] = !expanded[name];
        this.setState({ expanded }, () => this.scrollToBottomOfLogs(name));
    }

    render() {
        return (
            <div className="bundle-watch">
                <style type="text/css">{`
                    .bundle-watch {
                        margin: 36px;
                    }
                    .bundle-watch-header {
                        border-bottom: 1px solid #eee;
                    }
                    .bundle-watch-icon {
                        display: inline-block;
                        vertical-align: middle;
                        cursor: pointer;
                        margin-right: 12px;
                        line-height: 18px;
                    }
                    .bundle-watch-icon svg {
                        fill: #444 !important;
                    }
                    .bundle-watch-icon:hover {
                        color: #999;
                    }
                    .bundle-watch-icon:hover svg {
                        fill: #999 !important;
                    }
                    .bundle-watch-log {
                        max-height: 400px;
                        overflow: auto;
                        padding: 16px 0;
                        box-shadow: 0 -5px 0 -4px #bbb, 0 5px 0 -4px #bbb;
                        border-top: 16px solid #fff;
                        border-bottom: 16px solid #fff;
                    }
                `}</style>

                <h1 className="bundle-watch-header">Bundle Watches Running</h1>

                {this.state.builds.map(b => <div name={b.name.replace(/[^a-zA-Z0-1]/,'')}>
                        <h1 style={{marginTop: 20}}>
                            {b.building && <span className="bundle-watch-icon"><LiveStatusIndicator result="RUNNING" height={18} /></span>}
                            <span className="bundle-watch-icon" onClick={() => this.toggleLog(b.name)}>{b.name}</span>
                            <span> in: {b.lastBuildTime}ms </span>
                            <Button icon="NavigationRefresh" onClick={() => this.reRunBundle(b.name)}/>
                            <Button icon="ActionDelete" onClick={() => this.clearLogs(b.name)}/>
                        </h1>
                        {(this.state.expanded[b.name] || b.building) && <pre className="bundle-watch-log">
                            {b.log}
                        </pre>}
                    </div>
                )}
            </div>
        );
    }
}

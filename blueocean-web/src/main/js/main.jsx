import React, {Component} from 'react';
import {render} from 'react-dom';

import {ExtensionPoint} from '@jenkins-cd/js-extensions';

/**
 * Root Blue Ocean UI component
 */
class App extends Component {
    render() {
        return (
            <div id="outer">
                <header className="global-header">
                    <nav>
                        <a href="#">Pipelines</a>
                        <a href="#">Applications</a>
                        <a href="#">Reports</a>
                        <a href="#">Administration</a>
                    </nav>
                    {/* TODO: <ExtensionPoint name="jenkins.main.globalNav" wrappingElement="nav"/> */}
                </header>
                <main>
                    <ExtensionPoint name="jenkins.main.body"/>
                </main>
            </div>
        );
    }
}

render(<App />, document.getElementById('root'));

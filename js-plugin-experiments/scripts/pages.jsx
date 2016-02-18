import React, {Component} from 'react';
import {ExtensionPoint, store, actions} from './blue-ocean';
// TODO: ^^^^^ get store from <Provider> or through jenkins, not import! See: https://goo.gl/jCbg08
import AlienLairLink from './plugins/AlienLairLink.jsx'
import AlienPageSubMenu from './plugins/AlienPageSubMenu.jsx'

function PiplineListHeader(props) {
    const count = props.pipelines ? props.pipelines.length : 0;
    return <h2>{count} Pipelines</h2>;
}

function renderHomepagePipeline(pipeline) {
    return <div key={pipeline.name}>
        <h3>{pipeline.name}</h3>
        <ExtensionPoint name="jenkins.pipeline.pipelineRow" pipeline={pipeline}/>
    </div>
}

// TODO: Split all this mess up into its own files

export class HomePage extends Component {

    constructor() {
        super();
        this.state = {};
    }

    componentDidMount() {
        const update = () => {this.setState({pipelines:store.getState().pipelines.pipelines});};
        this.unsubscribe = store.subscribe(update);
        update();
    }

    componentWillUnmount() {
        this.unsubscribe();
    }

    render() {

        const pipelines = this.state.pipelines || [];

        return <article>
            <h1>Home</h1>
            <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ad architecto autem deleniti, dicta
                exercitationem explicabo facere harum hic inventore laborum magnam magni maiores molestias nemo
                recusandae rem saepe! Illo, perferendis?</p>

            <PiplineListHeader pipelines={this.state.pipelines}/>
            {pipelines.map(renderHomepagePipeline)}
            
            <input ref="newPipelineName" type="text" />
            <button onClick={() => this.addNewPipeline()}>Add Pipeline</button>
        </article>
    }

    addNewPipeline() {
        const newPipeline = {
            name: this.refs.newPipelineName.getDOMNode().value,
            status: "green"
        };
        store.dispatch({type:actions.ADD_PIPELINE, pipeline:newPipeline});
    }
}

// TODO: Put something useful here?
export class AboutPage extends Component {
    render() {
        return <article>
            <h1>About</h1>
            <p>
                Jenkins is an award-winning, cross-platform, continuous integration and continuous delivery application
                that increases your productivity. Use Jenkins to build and test your software projects continuously
                making it easier for developers to integrate changes to the project, and making it easier for users to
                obtain a fresh build. It also allows you to continuously deliver your software by providing powerful
                ways to define your build pipelines and integrating with a large number of testing and deployment
                technologies.
            </p>
        </article>
    }
}

// This is just some example code with a silly name.
export class AlienPage extends Component {
    render() {
        return <article>
            <h1>This is the third page with a dynamic menu</h1>
            <div className="subMenu">
                <ExtensionPoint name="jenkins.pipeline.alienPageHome" />
            </div>
        </article>
    }
}

export class NotFoundPage extends Component {
    render() {
        console.log("Rendering NotFoundPage, props", this.props);
        return <article>
            <h1>Not found</h1>
            <p>This route (<strong>{this.props.location.pathname}</strong>) is not currently mapped to anything :(</p>
            <p><img src="/resources/hawhaw.gif"/></p>
        </article>
    }
}

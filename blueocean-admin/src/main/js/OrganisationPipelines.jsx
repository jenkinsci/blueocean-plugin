import React, {Component} from 'react';
import  Pipelines from './components/Pipelines';
import {components} from '@jenkins-cd/design-language';

const { Page, PageHeader, Title } = components;

export default class OrganisationPipelines extends Component {
    constructor() {
        super();
        this.state = {pipelines: []};
    }

    componentDidMount() {
        fetchPipelineData((data) => {
            this.setState({pipelines: data});
        });
    }

    render() {
        const
            { pipelines } = this.state,
            link = <a target='_blank' href="/jenkins/view/All/newJob">New Pipeline</a>;

        return (
            <Page>
                <PageHeader>
                    <Title>CloudBees</Title>
                </PageHeader>
                <main>
                    <article>
                        {/* TODO: Move the link into the Title */}
                        <div>{link}</div>
                        {(pipelines && pipelines.length > 0 ? <Pipelines pipelines={pipelines}/>
                            : <p>No pipelines</p> )}
                    </article>
                </main>
            </Page>);
    }
}

/** FIXME: Ghetto ajax loading of pipeline data for an org @store*/
function fetchPipelineData(onLoad) {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = () => {
        if (xmlhttp.readyState == XMLHttpRequest.DONE) {
            if (xmlhttp.status == 200) {
                console.log(xmlhttp.responseText);
                var pipes = JSON.parse(xmlhttp.responseText);
                onLoad(pipes);
                console.log(pipes);
            } else {
                console.log('something else other than 200 was returned')
            }
        }
    };
    xmlhttp.open("GET", "/jenkins/blue/rest/organizations/jenkins/pipelines/", true);
    xmlhttp.send();
}

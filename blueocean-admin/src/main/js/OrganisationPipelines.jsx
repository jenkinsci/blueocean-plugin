import React, { Component } from 'react';
import  Pipelines from './components/Pipelines';
import { components } from '@jenkins-cd/design-language';

const { Page, PageHeader, Title } = components;

/** FIXME: Ghetto ajax loading of pipeline data for an org @store*/
function fetchPipelineData(onLoad) {
    const xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = () => {
        if (xmlhttp.readyState === XMLHttpRequest.DONE) {
            if (xmlhttp.status === 200) {
                console.log(xmlhttp.responseText);
                const pipes = JSON.parse(xmlhttp.responseText);
                onLoad(pipes);
                console.log(pipes);
            } else {
                console.log('something else other than 200 was returned');
            }
        }
    };
    // TODO: fixme ... the url may not have a "/jenkins" prefix
    xmlhttp.open('GET', '/jenkins/blue/rest/organizations/jenkins/pipelines/', true);
    xmlhttp.send();
}

export default class OrganisationPipelines extends Component {
  constructor() {
    super();
        this.state = { pipelines: [] };
  }

  componentDidMount() {
    fetchPipelineData((data) => {
            this.setState({ pipelines: data });
    });
  }

  render() {
        const { pipelines } = this.state;
        // TODO: fixme "/jenkins"
        const link = <a target="_blank" href="/jenkins/view/All/newJob">New Pipeline</a>;

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

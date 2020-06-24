import { React, Component } from 'react';
import { ComponentLink, i18nTranslator, } from '@jenkins-cd/blueocean-core-js';


const t = i18nTranslator('blueocean-dashboard');

export class FakeRunDetails extends Component {
    render() {
        return (
            <div>Fake Run Details</div>
        )
    }
}

export default class RunDetailsBuildMetadataLink extends ComponentLink {
    name = 'fakerundetails';
    title = t('rundetail.header.tab.fakerundetails', { defaultValue: 'Fake' });
    component = FakeRunDetails;
}
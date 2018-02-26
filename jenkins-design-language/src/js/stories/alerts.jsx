import React from 'react';
import {storiesOf} from '@kadira/storybook';
import {Alerts} from '../components/Alerts';
import { ResultItem } from '../components';

storiesOf('Alerts', module)
    .add('all', allCases);

function onCollapse(data) {
    // eslint-disable-next-line
    console.log("Collapsing",data);
}
function allCases() {
    return (<div>
        <Alerts/>
        <br/>
        <Alerts title="Custom title" message="Custom message"/>
        <br/>
        <Alerts type="Error" message={<div>This pipeline uses input types that are unsupported. Use <a href="#">Jenkins Classic</a> to resolve this input step</div>} />
        <br/>
        <Alerts type="Success"  message="Yay! All the things worked. Obviously don’t use this text"/>
        <br/>
        <Alerts message="Generic alert text. Other info about the alert." />
        <br/>
        <Alerts type="Warning" message="Generic alert text. Other info about the alert." />
        <br/>
        <ResultItem result="paused" label="Input needed"  onExpand={data => console.log('expand', data)}
                    onCollapse={onCollapse} data="bravo">
            <Alerts type="Error" message={<div>This pipeline uses input types that are unsupported. Use <a href="#">Jenkins Classic</a> to resolve this input step</div>} />
        </ResultItem>

    </div>);
}

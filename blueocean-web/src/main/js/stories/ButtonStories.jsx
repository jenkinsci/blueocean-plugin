/* eslint-disable */
import React from 'react';
import { storiesOf } from '@kadira/storybook';

storiesOf('Button', module)
    .add('general', General)
    .add('inverse', Inverse)
    .add('monochrome', Monochrome)
;

const titleCell = {
    width: 150,
};

const buttonCell = {
    display: 'flex',
    width: '33%',
    justifyContent: 'center',
};

const buttonRow = {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 10
};

function ButtonRow(props) {
    const label = props.label || 'Normal';
    const element = props.element || 'button';
    const text = props.text || 'Primary';

    const buttonProps = props.props || {};

    const layouts = ['layout-small', '', 'layout-large'];

    return (
        <div style={buttonRow}>
            <div style={titleCell}>{label}</div>

            {layouts.map(layout => (
                <div key={layout} className={layout} style={buttonCell}>
                    {React.createElement(element, { ...buttonProps }, text)}
                </div>
            ))}
        </div>
    );
}

function ButtonHeader(props) {
    return (
        <div style={{marginTop: 10}}>
            <h3>{props.title}</h3>
            <div style={buttonRow}>
                <div style={titleCell} />
                <div style={buttonCell}>Small</div>
                <div style={buttonCell}>Medium</div>
                <div style={buttonCell}>Large</div>
            </div>
        </div>
    )
}

function ButtonTable(props) {
    const className = props.className || '';
    const style = props.style || {};
    style.padding = 10;
    style.backgroundColor = style.backgroundColor || 'transparent';

    return (
        <div className={className} style={style}>
            {props.children}
        </div>
    )
}

function General() {
    return (
        <ButtonTable>
            <span className="componentdoc" data-docfile="doc-buttons-default"></span>
            <ButtonHeader title="Primary Button" />
            <ButtonRow label="Normal" />
            <ButtonRow label="Hover" props={{className:'hover'}} />
            <ButtonRow label="Active" props={{className:'active'}} />
            <ButtonRow label="Disabled" props={{disabled: true}} />
            <ButtonRow label="Focus" props={{className:'focus'}} />
            <ButtonRow label="Destructive" props={{className: 'btn-danger'}} />

            <ButtonHeader title="Secondary Button" />
            <ButtonRow label="Normal" text="Secondary" props={{className: 'btn-secondary'}}/>
            <ButtonRow label="Hover" text="Secondary" props={{className:'btn-secondary hover'}} />
            <ButtonRow label="Active" text="Secondary" props={{className:'btn-secondary active'}} />
            <ButtonRow label="Disabled" text="Secondary" props={{className: 'btn-secondary', disabled: true}} />
            <ButtonRow label="Focus" text="Secondary" props={{className:'btn-secondary focus'}} />
            <ButtonRow label="Destructive" text="Secondary" props={{className: 'btn-secondary btn-danger'}} />

            <ButtonHeader title="Link Button" />
            <ButtonRow label="Normal" element="a" text="Link Button" props={{className: 'btn-link', href: '#'}}/>
            <ButtonRow label="Hover" element="a" text="Link Button" props={{className: 'btn-link hover', href: '#'}}/>
            <ButtonRow label="Active" element="a" text="Link Button" props={{className: 'btn-link active', href: '#'}}/>
            <ButtonRow label="Focus" element="a" text="Link Button" props={{className: 'btn-link focus', href: '#'}}/>

            <ButtonHeader title="A tag as Button" />
            <ButtonRow label="Normal" element="a" text="Link Button" props={{className: 'btn', href: '#'}}/>
            <ButtonRow label="Hover" element="a" text="Link Button" props={{className: 'btn hover', href: '#'}}/>
            <ButtonRow label="Active" element="a" text="Link Button" props={{className: 'btn active', href: '#'}}/>
            <ButtonRow label="Focus" element="a" text="Link Button" props={{className: 'btn focus', href: '#'}}/>
        </ButtonTable>
    );
}

function Inverse() {
    return (
        <ButtonTable className="inverse" style={{color: '#FFF', backgroundColor: '#003054'}}>
            <span className="componentdoc" data-docfile="doc-buttons-inverse"></span>
            <ButtonHeader title="Primary" />
            <ButtonRow label="Normal" />
            <ButtonRow label="Hover" props={{className:'hover'}} />
            <ButtonRow label="Active" props={{className:'active'}} />
            <ButtonRow label="Disabled" props={{disabled: true}} />
            <ButtonRow label="Focus" props={{className:'focus'}} />
            <ButtonRow label="Destructive" props={{className: 'btn-danger'}} />

            <ButtonHeader title="Link Button" />
            <ButtonRow label="Normal" element="a" text="Link Button" props={{className: 'btn-link', href: '#'}}/>
            <ButtonRow label="Hover" element="a" text="Link Button" props={{className: 'btn-link hover', href: '#'}}/>
            <ButtonRow label="Active" element="a" text="Link Button" props={{className: 'btn-link active', href: '#'}}/>
            <ButtonRow label="Focus" element="a" text="Link Button" props={{className: 'btn-link focus', href: '#'}}/>
        </ButtonTable>
    )
}

function Monochrome() {
    return (
        <ButtonTable className="monochrome">
            <ButtonHeader title="Primary" />
            <ButtonRow label="Normal" props={{className:'monochrome'}} />
            <ButtonRow label="Hover" props={{className:'monochrome hover'}} />
            <ButtonRow label="Active" props={{className:'monochrome active'}} />
            <ButtonRow label="Disabled" props={{className:'monochrome', disabled: true}} />
            <ButtonRow label="Focus" props={{className:'monochrome focus'}} />
            <ButtonRow label="Destructive" props={{className: 'monochrome'}} />
        </ButtonTable>
    )
}

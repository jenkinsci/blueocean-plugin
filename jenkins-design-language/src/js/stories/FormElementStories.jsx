import React, { PropTypes } from 'react';
import { storiesOf } from '@kadira/storybook';
import { Checkbox, Dropdown, Favorite, FormElement, PasswordInput, RadioButtonGroup, TextArea, TextInput } from '../components';

storiesOf('FormElement', module)
    .add('general', () => <General />)
    .add('layouts', () => <Layouts />)
    .add('disabled', () => <Disabled />)
    .add('errors', () => <Errors />)
    .add('sizes', () => <Sizes />)
;

const style = {
    padding: 10,
};


function General() {
    return (
        <div>
            <div style={style}>
                <p>With title</p>

                <FormElement title="Title">
                    <TextInput defaultValue="Child" />
                </FormElement>
            </div>
            <div style={style}>
                <p>With showDivider</p>

                <FormElement title="Title" showDivider>
                    <RadioButtonGroup options={['A', 'B', 'C']} />
                </FormElement>
            </div>
            <div style={style}>
                <p>With errorMessage</p>

                <FormElement title="TextInput" errorMessage="error">
                    <TextInput defaultValue="error" />
                </FormElement>
                <FormElement title="PasswordInput" errorMessage="error">
                    <PasswordInput defaultValue="error" />
                </FormElement>
                <FormElement title="TextArea" errorMessage="error">
                    <TextArea defaultValue="error" />
                </FormElement>
            </div>
        </div>
    );
}

function Layouts() {
    return (
        <div>
            <div style={style}>
                <p>Multiple children lay out horizontally by default...</p>

                <FormElement title="Details">
                    <TextInput placeholder="name" />
                    <button>Save</button>
                </FormElement>
            </div>
            <div style={style}>
                <p>... or use verticalLayout=true</p>

                <FormElement title="Details" verticalLayout>
                    <TextArea placeholder="name" />
                    <button>Save</button>
                </FormElement>
            </div>
            <div style={style}>
                <p>Nesting FormElements within FormElements</p>

                <FormElement title="Legal Name" showDivider>
                    <FormElement title="First Name">
                        <TextInput defaultValue="Michael"/>
                    </FormElement>
                    <FormElement title="Last Name">
                        <TextInput defaultValue="Garibaldi"/>
                    </FormElement>
                </FormElement>
            </div>

            <div style={style}>
                <p>Nesting FormElements within FormElements with verticalLayout=true</p>

                <FormElement title="Authentication Details" showDivider verticalLayout errorMessage="fill in fields below">
                    <FormElement title="Username">
                        <TextInput defaultValue="michael.garibaldi"/>
                    </FormElement>
                    <FormElement title="Password">
                        <PasswordInput defaultValue="peekaboo"/>
                    </FormElement>
                    <FormElement title="Secret Key">
                        <TextArea />
                    </FormElement>
                </FormElement>
            </div>
        </div>
    );
}


// Sizes

// show several FormElements w/ TextInputs to demonstrate FormElement spacing
function TextInputGroup(props) {
    const count = props.count || 3;
    const divider = props.divider;
    const array = new Array(count);

    return (
        <div>
            {React.Children.map(array, (item, index) => (
                <FormElement title={`Element #${index}`} showDivider={divider}>
                    <TextInput defaultValue={`Child #${index}`} />
                </FormElement>
            ))}
        </div>
    );
}

TextInputGroup.propTypes = {
    count: PropTypes.number,
    divider: PropTypes.bool,
};

function Sizes() {
    return (
        <div>
            <div style={style}>
                <p>Using no layout</p>
                <TextInputGroup />
            </div>
            <div className="layout-small" style={style}>
                <p>Using layout-small</p>
                <TextInputGroup />
            </div>
            <div className="layout-medium" style={style}>
                <p>Using layout-medium</p>
                <TextInputGroup />
            </div>
            <div className="layout-large" style={style}>
                <p>Using layout-large</p>
                <TextInputGroup />
            </div>
            <h1 style={style}>With Dividers</h1>
            <div className="layout-small" style={style}>
                <p>Using layout-small</p>
                <TextInputGroup divider />
            </div>
            <div className="layout-medium" style={style}>
                <p>Using layout-medium</p>
                <TextInputGroup divider />
            </div>
            <div className="layout-large" style={style}>
                <p>Using layout-large</p>
                <TextInputGroup divider />
            </div>
        </div>
    );
}

function Disabled() {
    return (
        <div>
            <div style={style}>
                <p>Elements are disabled when nested in &#60;fieldset disabled&#62;</p>

                <fieldset disabled>
                    <FormElement>
                        <button>Button</button>
                    </FormElement>
                    <FormElement>
                        <TextInput placeholder="TextInput" />
                    </FormElement>
                    <FormElement>
                        <PasswordInput placeholder="PasswordInput" />
                    </FormElement>
                    <FormElement>
                        <TextArea placeholder="TextArea" />
                    </FormElement>
                    <FormElement>
                        <Checkbox label="Checkbox" />
                    </FormElement>
                    <FormElement>
                        <Favorite label="Favorite" />
                    </FormElement>
                    <FormElement>
                        <RadioButtonGroup options={['Radio','Button','Group']} />
                    </FormElement>
                    <FormElement>
                        <Dropdown placeholder="Dropdown" options={['Drop','Down','Menu']} />
                    </FormElement>
                </fieldset>
            </div>
        </div>
    );
}

// Errors

function FormError(props) {
    return (
        <FormElement title="Title" errorMessage="with error message">
            {props.children}
        </FormElement>
    );
}

FormError.propTypes = {
    children: PropTypes.node,
};

function Errors() {
    return (
        <div>
            <div style={style}>
                 <FormError>
                     <TextInput defaultValue="TextInput with error" />
                 </FormError>
            </div>
            <div style={style}>
                <FormError>
                    <TextArea defaultValue="TextArea with error" />
                </FormError>
            </div>
        </div>
    );
}

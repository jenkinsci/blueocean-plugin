Features
--------

- Very simple modal/dialog
- Callback before open
- Callback after open
- Callback before close
- Callback after close
- Callback on overlay click
- All styles can be overridden or not used at all


Proptypes
---------

```
ModalView.propTypes = {
    afterClose: PropTypes.func,
    afterOpen: PropTypes.func,
    beforeClose: PropTypes.func,
    beforeOpen: PropTypes.func,

    title: PropTypes.string,
    body: PropTypes.string,
    children: PropTypes.object,

    onOverlayClicked: PropTypes.func,
    hideOnOverlayClicked: PropTypes.bool,
    showOverlay: PropTypes.bool,
    isVisible: PropTypes.bool,

    styles: React.PropTypes.oneOfType([
        PropTypes.shape({
            closeButtonStyle: PropTypes.object,
            dialogStyles: PropTypes.object,
            overlayStyles: PropTypes.object,
            titleStyle: PropTypes.object
        }),
        PropTypes.bool,
    ]),

}
```

How to use
--------------------

```
import ModalView from 'react-header-modal';

<ModalView styles={true} />
```

Default view. The `styles={true}` is to activate the default style support, 
however we have implement the css classes, so you can remove it when
developing in blueocean.

![default](./screenshot/default.png "default")

```
import ModalView from 'react-header-modal';

<ModalView 
  hideOnOverlayClicked
  title="Hi, xxx modal"
  body="Simple Text"
/>
```

Simplest use is to pass the title and the body as an attribute.

![simplest](./screenshot/simple.png "simplest")

```
import ModalView, {ModalBody, ModalHeader} from 'react-header-modal';

<ModalView hideOnOverlayClicked>
    <ModalHeader>
        <ul>
            <li>Lorem ipsum dolor sit amet, impetus dissentiunt vix ne. Vix accumsan adipisci no, ius no
                populo voluptaria, no eam viderer appareat persequeris. Ex harum tollit nullam mea. Mei
                sanctus placerat ut, ad mei recusabo instructior, quo eu nonumes deleniti principes. Ceteros
                oportere aliquando ei pro, et dolores forensibus quo, te zril adolescens vix. Pro at illum
                dicit referrentur, fabellas conclusionemque ne nam.
            </li>
            ...
            <li>Lorem ipsum dolor sit amet, impetus dissentiunt vix ne. Vix accumsan adipisci no, ius no
                populo voluptaria, no eam viderer appareat persequeris. Ex harum tollit nullam mea. Mei
                sanctus placerat ut, ad mei recusabo instructior, quo eu nonumes deleniti principes. Ceteros
                oportere aliquando ei pro, et dolores forensibus quo, te zril adolescens vix. Pro at illum
                dicit referrentur, fabellas conclusionemque ne nam.
            </li>
    
        </ul>
    </ModalHeader>
    <ModalBody>
        <ul>
            <li>Lorem ipsum dolor sit amet, impetus dissentiunt vix ne. Vix accumsan adipisci no, ius no
                populo voluptaria, no eam viderer appareat persequeris. Ex harum tollit nullam mea. Mei
                sanctus placerat ut, ad mei recusabo instructior, quo eu nonumes deleniti principes. Ceteros
                oportere aliquando ei pro, et dolores forensibus quo, te zril adolescens vix. Pro at illum
                dicit referrentur, fabellas conclusionemque ne nam.
            </li>
            ...
            <li>Lorem ipsum dolor sit amet, impetus dissentiunt vix ne. Vix accumsan adipisci no, ius no
                populo voluptaria, no eam viderer appareat persequeris. Ex harum tollit nullam mea. Mei
                sanctus placerat ut, ad mei recusabo instructior, quo eu nonumes deleniti principes. Ceteros
                oportere aliquando ei pro, et dolores forensibus quo, te zril adolescens vix. Pro at illum
                dicit referrentur, fabellas conclusionemque ne nam.
            </li>
    
        </ul>
    </ModalBody>
</ModalView>
```

You can pass anything to header and body.

![head_body_lot](./screenshot/head_body_lot.png "head_body_lot")

```
import ModalView from 'react-header-modal';

<ModalView 
  hideOnOverlayClicked
  title="Hi, xxx modal"
  body="Simple Text"
  styles={{
    contentStyle: {
      backgroundColor: '#767535'
    }
  }}
/>
```

Simply override the default styles or turn them off (`styles={false}`) 
and use the class name hooks for add the styling.

![style](./screenshot/style.png "style")

```
import ModalView from 'react-header-modal';

<ModalView 
  hideOnOverlayClicked
  title="Hi, xxx modal"
  body="Simple Text"
  isVisible={true}
/>
```

You can show the modal visible when page has loaded, e.g. to show a 
notice or whatever.

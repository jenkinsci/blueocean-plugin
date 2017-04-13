# Extension Points in JS

Extension Points are placeholders where a system comprised of plugins or
modules may contribute implementations to provide additional functionality
and alternate implementation of behavior.

In general, Extension Points are a defined marker, and multiple Extensions
that adhere to a common interface. In practice, Extension Points are usually
defined as ES 6 classes.

Extensions cover a few use cases:
* Finding implementations
* Finding best match implementations
* Providing consistent ordering of implementations
* Managing dependency injection

`@ExtensionPoint` is the decorator used to register a class as an extension point.

`@Extension` is the decorator used to mark a class as an implementor of the
extension point. Note, while it's generally a good idea to extend the extension
point, it is not required due to the dynamic nature of javascript. As long
as the same contract is met by the extension, it will function properly.

`@Ordinal` is a class decorator to set priority between extensions. _Note: this must
be defined *after* the `@Extension` declaration.

`@ExtensionList` is the property decorator to inject an extension list of a
particular type. It's also possible to programmatically invoke `lookupExtensions`

`@Singleton` is the class decorator to use to make an object available externally to the
module, automatically. This dramatically reduces the dependency on publishing
npm modules, and allows various modules within the system to share services
automatically. It is also possible to call `registerExport` with a
provider method to achieve a similar function.

`@Inject` is the property decorator to use to get all implementations of an
Extension Point or any other Exported object.

Note: all of these can be accomplished with plain ES 5, if you choose to use it,
there are just a singificant amount more lines of code to write for it all and
you must create your own.

## Examples

### Define and use an ExtensionPoint:

```
import { ExtensionPoint, ExtensionList } from 'blueocean-js-extensions';

@ExtensionPoint
class PersonDetailsLink {
    name() {}
    url() {}
}

class PersonDetailLinkRenderer extends React.Component {
    @ExtensionList(PersonDetailsLink) personDetailsLinks;

    render() {
        return (<div className="person-details-links">
            {personDetailsLinks.map(l =>
                <a href={l.url()}>{l.name()}</a>
            )}
        </div>);
    }
}
```

### Define an extension:

```
import { extensionPoints, Extension } from 'blueocean-js-extensions';

const {
    PersonDetailsLink,
} = extensionPoints;

@Extension
class AddressDetailsLink extends PersonDetailsLink {
    name() {
        return 'Address';
    }
    url() {
        return '/address';
    }
}
```

### Ordered extensions:

```
import { extensions, extensionPoints, Extension, Ordinal } from 'blueocean-js-extensions';

const {
    React,
    LocalUserDatabase,
} = extensions;

const {
    SecurityAuthenticator,
    UrlScanner,
} = extensionPoints;

@Extension
@Ordinal(0)
class BlackListScanner extends UrlScanner {
    isAllowed(url) {
        return blacklist.contains(url);
    }
}

@Extension
@Ordinal(1) // let blacklist happen first
class WhitelistJenkinsDotIo extends UrlScanner {
    isAllowed(url) {
        return /https?[:]\/\/jenkins[.]io\/.*/.test(url);
    }
}
```

### Extensions in lieu of DI

```
import { extensions, extensionPoints, Extension, Inject } from 'blueocean-js-extensions';

const {
    React,
    LocalUserDatabase,
} = extensions;

class LoginForm extends React.Component {
    @Inject(LocalUserDatabase) localUserDatabase;

    render() {
        return <div>...
    }
}

@Extension
class LocalSecurityAuthenticator extends SecurityAuthenticator {
    @Inject(LocalUserDatabase) localUserDatabase;

    getAuthenticationType() {
        return localUserDatabase;
    }
    getLoginComponent() {
        return LoginForm;
    }
}
```




//@ExtensionPoint
class Hello_ {
    getStuff(): boolean {
        return false; }
}

@ExtensionPoint
interface Hello2 {
    getStuff(): boolean;
}

@ExtensionPoint
class Hello implements Hello2 {
    getStuff(): boolean { return false }
}

class Foo extends Hello {
    getS
}

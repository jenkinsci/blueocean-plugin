export class JenkinsEncode {
    static _uriMap = null;
    static _getUrlMap() {
        if (this._uriMap == null) {
            const raw = "!  $ &'()*+,-. 0123456789   =  @ABCDEFGHIJKLMNOPQRSTUVWXYZ    _ abcdefghijklmnopqrstuvwxyz";
            this._uriMap = [];

            let i = 0;
            for (i = 0; i < 33; i++) {
                this._uriMap[i] = true;
            }

            for (let j = 0; j < raw.length; i++, j++) {
                this._uriMap[i] = raw.charAt(j) == ' ';
            }
        }
        return this._uriMap;
    }

    static encode(str) {
        const uriMap = this._getUrlMap();

        return str
            .split('')
            .map(c => {
                const charCode = c.charCodeAt(0);
                if (charCode > 122 || uriMap[charCode]) {
                    return encodeURIComponent(c);
                }
                return c;
            })
            .join('');
    }
}

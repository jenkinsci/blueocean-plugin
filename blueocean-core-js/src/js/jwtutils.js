import fetch from 'isomorphic-fetch';
import jwt from 'jsonwebtoken';
import UrlUtils from './urlutils';
import FetchUtils from './fetchutils';
import { jwk2pem } from 'pem-jwk';
let storedToken = null;
let publicKeyStore = null;
let tokenFetchPromise = null;
export default {
    fetchJWT() {
        if (storedToken && storedToken.exp) {
            const diff = storedToken.exp - Math.trunc(new Date().getTime() / 1000);
            if (diff < 300) {
                tokenFetchPromise = null;
            }
        }

        if (!tokenFetchPromise) {
            tokenFetchPromise = fetch(`${UrlUtils.getJenkinsRootURL()}/jwt-auth/token`, { credentials: 'same-origin' })
                .then(this.checkStatus)
                .then(response => {
                    const token = response.headers.get('X-BLUEOCEAN-JWT');
                    if (token) {
                        return token;
                    }
                    
                    throw new Error('Could not fetch jwt_token');
                });
        }

        return tokenFetchPromise;
    },

    verifyToken(token, certObject) {
        return new Promise((resolve, reject) =>
            jwt.verify(token, jwk2pem(certObject), { algorithms: [certObject.alg] }, (err, payload) => {
                if (err) {
                    reject(err);
                } else {
                    resolve(payload);
                }
            }));
    },

    fetchJWTPublicKey(token) {
        const decoded = jwt.decode(token, { complete: true });
        const url = `${UrlUtils.getJenkinsRootURL()}/jwt-auth/jwks/${decoded.header.kid}/`;
        if (!publicKeyStore) {
            publicKeyStore = fetch(url, { credentials: 'same-origin' })
                .then(FetchUtils.checkStatus)
                .then(FetchUtils.parseJSON)
                .then(cert => this.verifyToken(token, cert)
                    .then(payload => ({
                        token,
                        payload,
                    })));
        }

        return publicKeyStore;
    },

    storeToken(data) {
        storedToken = data.payload;
        return data;
    },

    getTokenWithPayload() {
        return this.fetchJWT()
            .then(FetchUtils.checkStatus)
            .then(token => this.fetchJWTPublicKey(token))
            .then(data => this.storeToken(data));
    },

    getToken() {
        return this.getTokenWithPayload().then(token => token.token);
    },
};

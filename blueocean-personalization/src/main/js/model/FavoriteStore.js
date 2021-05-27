import { observable, action } from 'mobx';
import { UrlConfig, Fetch, AppConfig, User, capabilityAugmenter, ToastService, i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import { cleanSlashes } from '../util/UrlUtils';
import { FavoritesSortHelper } from '../util/SortUtils';
import { blueocean } from '@jenkins-cd/blueocean-core-js/dist/js/scopes';

const t = i18nTranslator('blueocean-personalization');
const sortHelper = new FavoritesSortHelper();

class FavoriteStore {
    @observable _favoritesList = blueocean.favoritesList;
    @observable _favorites = [];
    _fetched = false;

    @action
    _setFavoritesList(_favoritesNames) {
        this._favoritesList = _favoritesNames;
    }

    @action
    _setFavorites(_favorites) {
        this._favorites = _favorites;
    }

    @action
    setFavorite(item, favorite) {
        if (favorite === this.isFavorite(item)) {
            return; // nothing to do
        }

        const baseUrl = UrlConfig.getJenkinsRootURL();
        const url = cleanSlashes(`${baseUrl}${item._links.self.href}/favorite`);

        const fetchOptions = {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ favorite: favorite }),
        };

        this.fetch(url, fetchOptions)
            .then(data => {
                if (favorite) {
                    const f = {
                        name: data.item.fullName,
                        primary: !(data.item.branch && !data.item.branch.isPrimary) // if it is a branch honor primary flag, in any other case assume its primary
                    };
                    this._setFavoritesList([...this._favoritesList, f]);
                } else {
                    const idx = this._favoritesList.findIndex(f => f.name === data.item.fullName);
                    this._setFavoritesList([...this._favoritesList.slice(0, idx), ...this._favoritesList.slice(idx + 1)]);
                }
                this.clearCache();
            });
    }

    isFavorite(item) {
        const fullName = item.fullName;
        if (this._favoritesList) {
            return this._favoritesList.find(f => {
                if (f.name === fullName) return true;
                return item.branch === undefined && f.primary &&
                    f.name.substr(0, f.name.lastIndexOf("/")) === fullName;
            }) !== undefined;
        }
        return false;
    }

    get favorites() {
        if (!this._fetched) {
            const user = User.current();
            this._fetched = true;
            if (user && !user.isAnonymous()) {
                const baseUrl = UrlConfig.getBlueOceanAppURL();
                const username = user.id;
                const organization = AppConfig.getOrganizationName();
                const url = cleanSlashes(`${baseUrl}/rest/organizations/${organization}/users/${username}/favorites/?start=0&limit=26`);
                this.fetch(url).then(favorites => this._setFavorites(sortHelper.applyStandardSort(favorites)));
            }
        }
        return this._favorites;
    }

    fetch(url, fetchOptions) {
        return Fetch.fetchJSON(url, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .catch(error => {
                const responseBody = error.responseBody;
                if (responseBody && responseBody.code && responseBody.message) {
                    ToastService.newToast({
                        style: 'error',
                        caption: t('Favoriting Error'),
                        text: t(responseBody.message),
                    });
                }
                console.error(error); // eslint-disable-line no-console
            });
    }

    @action
    clearCache() {
        this._favorites = [...this._favorites];
        this._fetched = false;
    }

    @action
    onPipelineRun = jobRun => {
        if (this._fetched) {
            for (const fav of this._favorites) {
                const runsBaseUrl = `${fav.item._links.self.href}runs`;
                const runUrl = jobRun._links.self.href;

                // if the job's run URL starts with the favorited item's '/runs' URL,
                // then the run applies to that item, so update the 'latestRun' property
                if (runUrl.indexOf(runsBaseUrl) === 0) {
                    const idx = this._favorites.indexOf(fav);
                    const updatedFavorite = JSON.parse(JSON.stringify(fav));
                    updatedFavorite.item.latestRun = jobRun;
                    const updatedFavorites = [...this._favorites.slice(0, idx), updatedFavorite, ...this._favorites.slice(idx + 1)];
                    const sortedFavorites = sortHelper.applyUpdateSort(updatedFavorites, updatedFavorite);
                    this._favorites = sortedFavorites;
                }
            }
        }
    };
}

export default new FavoriteStore();

import { observable, action } from 'mobx';
import { UrlConfig, Fetch, AppConfig, User, capabilityAugmenter, ToastService, i18nTranslator } from '../../core/index.js';
import { cleanSlashes } from '../util/UrlUtils';
import { FavoritesSortHelper } from '../util/SortUtils';
import { blueocean } from '../../core/index.js/dist/js/scopes';

const t = i18nTranslator('blueocean-personalization');
const sortHelper = new FavoritesSortHelper();

const defaultBranchName = 'master';

function getItemName(item) {
    const fullName = item.fullName;
    if (fullName.indexOf('/') < 0) {
        return fullName + '/' + defaultBranchName;
    }
    return fullName;
}

class FavoriteStore {
    @observable _favoritesNames = blueocean.favoritesList;
    @observable _favorites = [];
    _fetched = false;

    @action
    _setFavoritesNames(_favoritesNames) {
        this._favoritesNames = _favoritesNames;
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
        .then(() => {
            const { _favoritesNames: f } = this;
            const itemName = getItemName(item);
            if (favorite) {
                this._setFavoritesNames([...f, itemName]);
            } else {
                const idx = f.indexOf(itemName);
                this._setFavoritesNames([...f.slice(0, idx), ...f.slice(idx + 1)]);
            }
            this.clearCache();
        });
    }

    isFavorite(item) {
        return this._favoritesNames && this._favoritesNames.indexOf(getItemName(item)) >= 0;
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
                this.fetch(url)
                    .then(favorites => this._setFavorites(sortHelper.applyStandardSort(favorites)));
            }
        }
        return this._favorites;
    }

    fetch(url, fetchOptions) {
        return Fetch.fetchJSON(url, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .catch((error) => {
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
    onPipelineRun = (jobRun) => {
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
    }
}

export default new FavoriteStore();

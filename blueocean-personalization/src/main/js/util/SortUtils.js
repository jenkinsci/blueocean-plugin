/**
 * Created by cmeyers on 9/14/16.
 */
// the order the cards should be displayed based on their result/state (aka 'status')
const statusSortOrder = [
    'UNKNOWN', 'FAILURE', 'ABORTED', 'NOT_BUILT',
    'UNSTABLE', 'RUNNING', 'QUEUED', 'SUCCESS',
];

export const extractStatus = (favorite) => {
    const latestRun = favorite && favorite.item && favorite.item.latestRun || {};
    return latestRun.result === 'UNKNOWN' ? latestRun.state : latestRun.result;
};

// sorts the cards based on 1. status 2. endTime, startTime or enQueueTime (descending)
export const sortByStatusByRecent = (favoriteA, favoriteB) => {
    const statusA = extractStatus(favoriteA);
    const statusB = extractStatus(favoriteB);
    const orderA = statusSortOrder.indexOf(statusA);
    const orderB = statusSortOrder.indexOf(statusB);

    if (orderA < orderB) {
        return -1;
    } else if (orderA > orderB) {
        return 1;
    }

    const endTimeA = favoriteA && favoriteA.item && favoriteA.item.latestRun && favoriteA.item.latestRun.endTime;
    const endTimeB = favoriteB && favoriteB.item && favoriteB.item.latestRun && favoriteB.item.latestRun.endTime;

    if (endTimeA && endTimeB) {
        const endCompare = endTimeA.localeCompare(endTimeB);

        if (endCompare !== 0) {
            return -endCompare;
        }
    }

    const startTimeA = favoriteA && favoriteA.item && favoriteA.item.latestRun && favoriteA.item.latestRun.startTime;
    const startTimeB = favoriteB && favoriteB.item && favoriteB.item.latestRun && favoriteB.item.latestRun.startTime;

    if (startTimeA && startTimeB) {
        const startCompare = startTimeA.localeCompare(startTimeB);

        if (startCompare !== 0) {
            return -startCompare;
        }
    }

    const queuedTimeA = favoriteA && favoriteA.item && favoriteA.item.latestRun && favoriteA.item.latestRun.enQueueTime;
    const queuedTimeB = favoriteB && favoriteB.item && favoriteB.item.latestRun && favoriteB.item.latestRun.enQueueTime;

    if (queuedTimeA && queuedTimeB) {
        const queueCompare = queuedTimeA.localeCompare(queuedTimeB);

        if (queueCompare !== 0) {
            return -queueCompare;
        }
    }

    return 0;
};

/**
 * Applies proper sorting to favorites.
 */
export class FavoritesSortHelper {

    constructor() {
        this._statusMap = {};
    }

    /**
     * Apply the standard "by status, then by recent" sort to favorites.
     *
     * @param {Array|List} favorites
     * @returns {Array|List}
     */
    applyStandardSort(favorites) {
        this._statusMap = {};

        favorites.forEach(fav => {
            const href = fav.item._links.self.href;
            const status = extractStatus(fav);
            this._statusMap[href] = status;
        });

        return favorites.sort(sortByStatusByRecent);
    }

    /**
     * Apply the proper sort when favorite in the list is updated.
     *
     * @param {Array|List} allFavorites
     * @param updatedFavorite
     * @returns {Array|List}
     */
    applyUpdateSort(allFavorites, updatedFavorite) {
        const status = extractStatus(updatedFavorite);

        // a significant change occurred that should cause a full resort of the list
        // this prevents the list from resorting when jobs transition to QUEUED or RUNNING
        if (status === 'FAILURE' || status === 'ABORTED' || status === 'UNSTABLE' || status === 'SUCCESS') {
            return this.applyStandardSort(allFavorites);
        }

        return allFavorites.slice();
    }

}

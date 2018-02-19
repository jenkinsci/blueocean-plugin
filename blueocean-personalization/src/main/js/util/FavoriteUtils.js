/**
 * Created by cmeyers on 7/11/16.
 */

/**
 * Determine whether the URL for the supplied favorite and pipeline/branch match.
 *
 * @param favoriteUrl
 * @param pipelineOrBranchUrl
 * @returns {boolean}
 */
export const checkMatchingFavoriteUrls = (favoriteUrl, pipelineOrBranchUrl) => {
    if (favoriteUrl === pipelineOrBranchUrl) {
        return true;
    }

    // A favorite can point to a pipeline or a specific branch of a multi-branch pipeline.
    // This logic handles the special case where a multi-branch pipeline was favorited -
    // implicitly favoriting the 'master' branch - but the URL to the pipeline itself is supplied.
    // We watch this to count as a match, even though the URL's are actually different.
    return (favoriteUrl === `${pipelineOrBranchUrl}branches/master` ||
        favoriteUrl === `${pipelineOrBranchUrl}branches/master/`);
};

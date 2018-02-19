import { Fetch } from '../..';
/**
 * Generic options needed for all requests
 * @type {{credentials: string, method: string, headers: {Content-Type: string}}}
 */
const fetchOptionsCommon = {
    credentials: 'include',
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
    },
};
/**
 * Helper method to clone and prepare the body if attached
 * @param body - JSON object that we want to sent to the server
 * @returns {*} fetchOptions
 */
function prepareOptions(body) {
    const fetchOptions = Object.assign({}, fetchOptionsCommon);
    if (body) {
        try {
            fetchOptions.body = JSON.stringify(body);
        } catch (e) {
            console.warn('The form body are not added. Could not extract data from the body element', body);
        }
    }
    return fetchOptions;
}
export class ParameterApi {

    /**
     * Start a run with parameters
     * @param href - the destination (ends normally with /runs/)
     * @param parameters - the parameters we want to submit
     * @returns {*} Promise
     */
    startRunWithParameters(href, parameters) {
        const fetchOptions = prepareOptions({ parameters });
        return Fetch.fetchJSON(href, { fetchOptions });
    }

    /**
     * Submit an InputStep form
     * @param href - the destination (the step href)
     * @param id - unique id of the input step we submit to
     * @param parameters - the parameters we want to submit
     * @returns {*} Promise
     */
    submitInputParameter(href, id, parameters) {
        const fetchOptions = prepareOptions({ id, parameters });
        return Fetch.fetchJSON(href, { fetchOptions });
    }

    /**
     * Cancel an InputStep form
     * @param href - the destination (the step href)
     * @param id - unique id of the input step we want to cancel
     * @returns {*} Promise
     */
    cancelInputParameter(href, id) {
        const fetchOptions = prepareOptions();
        fetchOptions.body = JSON.stringify({ id, abort: true });
        return Fetch.fetchJSON(href, { fetchOptions });
    }
}

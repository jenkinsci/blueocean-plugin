/*
 * Update a GET url param value
 * @param paramName: which param to change value of
 * @param newParamValue
 * @param getParams: query obj that needs to be passed along to this function e.g. "this.props.location.query"
 */

export default function updateGetParamm(paramName, newParamValue, getParams) {
    let updatedParamsStr = '?';
    let i;

    for (i in getParams) {
        if (getParams.hasOwnProperty(i)) {
            if (i === paramName) {
                updatedParamsStr += newParamValue ? `${i}=${newParamValue}&` : '';
            } else {
                updatedParamsStr += `${i}=${getParams[i]}&`;
            }
        }
    }

    if (!getParams.hasOwnProperty(paramName)) {
        updatedParamsStr += `${paramName}=${newParamValue}&`;
    }

    return updatedParamsStr.slice(0, -1);
}

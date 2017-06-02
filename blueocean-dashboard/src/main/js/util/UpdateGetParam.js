/*
 * Calculate whether to fetch a node
 * @param paramName
 * @param newParamValue
 * @param getParams
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

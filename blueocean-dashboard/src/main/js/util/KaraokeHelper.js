/*
 * Calculate whether to fetch a node
 * @param props
 * @param nextProps
 * @param mergedConfig
 * @param setState
 */

export function calculateNode(props, nextProps, mergedConfig, setState) {
    // case the param is different from the one we currently in
    if (nextProps.params.node !== props.params.node) {
        console.log(props.params.node, nextProps.params.node, nextProps.params.node < props.params.node)
        let refetch = mergedConfig.followAlong || false;
        // the param is prior of the node we have in the nodeReducer
        if (nextProps.params.node < props.params.node) {
            mergedConfig.node = nextProps.params.node;
        } else {
            // if we click in the future we will let the node to focused be calculated
            if (nextProps.result.state == 'RUNNING') {
                // we turn on refetch if we follow along so we always fetch a new Node result
                refetch = true;
                mergedConfig.followAlong = true;
            }
            delete mergedConfig.node;
        }
        const answer = { state: { followAlong: refetch} };
        answer.config = { ...mergedConfig, refetch };
        answer.action = 'fetchNodes';
        console.log('answer', answer);
        return answer;
    }
    return null;
}

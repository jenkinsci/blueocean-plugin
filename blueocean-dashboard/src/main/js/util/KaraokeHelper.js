/*
 * Calculate whether to fetch a node
 * @param props
 * @param nextProps
 * @param mergedConfig
 */

export function calculateNode(props, nextProps, mergedConfig) {
    // case the param is different from the one we currently in
    if (nextProps.params.node !== props.params.node) {
        // console.log(props.params.node, nextProps.params.node, nextProps.params.node < props.params.node);
        let refetch = mergedConfig.followAlong || false;
        // clone config
        const clonedConfig = { ...mergedConfig };
        // the param is prior of the node we have in the nodeReducer
        if (nextProps.params.node < props.params.node) {
            clonedConfig.node = nextProps.params.node;
            refetch = true;
            console.log('nextProps are smaller');
        } else {
            // if we click in the future we will let the node to focused be calculated
            if (nextProps.result.state === 'RUNNING') {
                // we turn on refetch if we follow along so we always fetch a new Node result
                refetch = true;
                console.log('bigger nextProps');
                clonedConfig.followAlong = true;
                delete clonedConfig.node;
            }
        }
        const answer = { state: { followAlong: refetch } };
        answer.config = { ...clonedConfig, refetch };
        answer.action = 'fetchNodes';
        console.log('answer', answer);
        return answer;
    } /* else {
        if (nextProps.result.state === 'RUNNING') {
            if(nextProps.nodeReducer && nextProps.params.node ) {
                console.log('bigger2 fall back nextProps', props, nextProps, mergedConfig)
                // const clonedConfig = {...mergedConfig};
                // let refetch = true;
                // clonedConfig.followAlong = true;
                // delete clonedConfig.node;
                // const answer = {state: {followAlong: refetch}};
                // answer.config = {...clonedConfig, refetch};
                // answer.action = 'fetchNodes';
                // console.log('answer2', answer);
                // return answer;
            }
        }
        console.log('karaokeHelper ELSE')
    }*/
    return null;
}

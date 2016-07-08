/*
 * Calculate whether to fetch a node
 * @param props
 * @param nextProps
 * @param mergedConfig
 */

export function calculateNode(props, nextProps, mergedConfig) {
    const refetch = nextProps.result.state === 'RUNNING';
    // case the param is different from the one we currently in
    if (nextProps.params.node !== props.params.node) {
        // clone config
        const clonedConfig = { ...mergedConfig };
        clonedConfig.node = nextProps.params.node;
        const answer = { state: { followAlong: mergedConfig.followAlong } };
        answer.config = { ...clonedConfig, refetch };
        answer.action = 'fetchNodes';
        return answer;
    }
    return null;
}

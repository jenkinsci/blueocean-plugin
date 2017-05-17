node {
   stage 'Stage 1'
   sh 'sleep 6; echo `date` Stage 1a we need some time here for slower ath hosts;'
   sh 'sleep 6; echo `date` Stage 1b;'
    stage 'Stage 2'
    parallel firstBranch: {
        sh 'echo `date` Stage 2 - first;sleep 3; echo `date` Stage 2 - first;sleep 5; echo `date` Stage 2 - first;'

    }, secondBranch: {
        sh 'echo `date` Stage 2 - second;sleep 3; echo `date` Stage 2 - second;sleep 5; echo `date` Stage 2 - second;'
    },
    failFast: true
    stage 'fin'
    sh 'echo `date` fin;sleep 6; echo `date` fin;'
    sh 'echo yeah > foo.txt'
    archiveArtifacts 'foo.txt'
    stage 'NoSteps'
}

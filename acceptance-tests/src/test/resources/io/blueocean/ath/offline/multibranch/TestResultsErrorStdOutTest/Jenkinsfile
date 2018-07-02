node {
    stage 'Stage 1 - scm'
    echo 'Stage 1 - scm'

    //
    // Need a small wait here so as not to hit JENKINS-36408.
    // Without it, the build run SSE end events *can* arrive at the client
    // before the it has finished processing the earlier events, which means
    // the end events are meaningless to it (no proper context until earlier
    // events are processed), causing the UI state for that run to end up in
    // limbo state - constant spinning orb etc requiring a page reload to fix.
    //
    sleep 2


    checkout scm
    stage 'Stage test'
    echo 'stuff'
    sh 'touch TEST-*.xml'
    step([$class: 'JUnitResultArchiver', testResults: 'TEST-*.xml'])
    
}
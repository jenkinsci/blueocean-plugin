node {
    echo 'first step'
    // Have a long'ish sleep at the start so as to allow the test
    // to start.
    sh 'sleep 7; echo `date` first;'
    // The following step is "step-7" in the flow... we wait for this node
    // to appear before we stop the karaoke.
    echo 'first step end'
    echo 'Second coming up'
    // Then have a long'ish series of short sleeps. The accumulated
    // time needs to be enough for the test pause (see noStages.js) to have
    // the enough potential karaoke activity during the pause. This activity
    // should not appear in the view when karaoke is stopped.
    sh 'sleep 1; echo `date` second;'
    echo 'third now'
    sh '''#!/bin/bash -l
        echo $0
        COUNTER=0
        while [  $COUNTER -lt 10001 ]; do
         echo The counter is $COUNTER
         let COUNTER=COUNTER+1
        done
        '''
        // add a step that does not produce a log
    sh 'sleep 1; echo `date` third;'
    echo '4th'
    sh 'sleep 1; echo `date`;'
    echo '5th'
    sh 'sleep 1; echo `date`;'
    echo '6th'
    sh 'sleep 1; echo `date`;'
    echo '7th'
    sh 'sleep 1; echo `date`;'
    echo '8th'
    sh 'sleep 1; echo `date`;'
    echo '9th'
    sh 'sleep 1; echo `date`;'
    echo '10th'
    sh 'sleep 1; echo `date`;'
    echo 'and we are finished'
    sh 'echo end'
    deleteDir()
}
node {
    echo 'first step'
    sh 'sleep 1; echo `date` first;'
    echo 'first step end'
    echo 'Second coming up'
    sh 'sleep 1; echo `date` second;'
    echo '9th'
    sh 'sleep 1; echo `date`;'
    echo '10th'
    sh 'sleep 1; echo `date`;'
    echo 'and we are finished'
    sh 'echo end; error 1'
}
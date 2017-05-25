#!/usr/bin/env bash
echo `date` freeStyle start;
sleep 4;
echo `date` step 1;
COUNTER=0
while [  $COUNTER -lt 10001 ]; do
 echo The counter is $COUNTER
 let COUNTER=COUNTER+1
done
sleep 5;
echo `date` freeStyle end;

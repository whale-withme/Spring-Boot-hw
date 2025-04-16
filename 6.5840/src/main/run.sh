#!/usr/bin/env bash
rm mr-out*
rm mr-tmp-*

go run mrsequential.go wc.so pg*.txt
sort mr-out-0 > mr-correct.txt
rm mr-out*

# go run mrcoordinator.go pg-*.txt
go build -buildmode=plugin ../mrapps/wc.go
go run mrworker.go wc.so
sort mr-out* > mr-out.txt
if cmp mr-out.txt mr-correct.txt
then
    echo "PASS"
else
    echo "FAIL"
fi
# bash test-mr.sh | grep -E 'Starting|PASS|FAIL'
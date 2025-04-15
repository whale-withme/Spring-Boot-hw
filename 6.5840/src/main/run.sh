#!/usr/bin/env bash
rm mr-out*
rm mr-tmp-*
go run mrcoordinator.go pg-*.txt
go run mrworker.go wc.so
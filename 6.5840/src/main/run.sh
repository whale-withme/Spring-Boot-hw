#!/usr/bin/env bash
rm mr-out*
go run mrcoordinator.go pg-*.txt
go run mrworker.go wc.so
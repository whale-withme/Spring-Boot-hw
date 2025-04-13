package main

import (
	"fmt"
	"time"
)

// sendHeartbeat 定义了一个发送心跳的函数
func sendHeartbeat(ch chan string) {
	// 创建一个每秒触发一次的 Ticker
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop() // 确保在函数退出时停止 Ticker

	// 无限循环，每秒钟发送一次心跳信号
	for {
		<-ticker.C
		ch <- "tick"
	}
}

func main() {
	ch := make(chan string) // 创建一个 channel 用于接收心跳信号

	go sendHeartbeat(ch) // 启动一个 goroutine，开始发送心跳

	// 主程序不断接收并打印来自 channel 的数据
	for {
		msg := <-ch                   // 从 channel 接收数据
		fmt.Println("Received:", msg) // 打印接收到的心跳信号
	}
}

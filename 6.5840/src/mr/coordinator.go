package mr

// package main

import (
	"fmt"
	"log"
	"net"
	"net/http"
	"net/rpc"
	"os"
	"sync"
)

var mu sync.Mutex

// job状态
type JobPhase int

const (
	UNSTART JobPhase = iota
	MAPPING
	REDUCING
	DONE
	ERROR
)

type TaskType int

const (
	MAP TaskType = iota
	REDUCE
)

type TaskStatus string

const (
	WAITING TaskStatus = "WAITING"
	MAPPED  TaskStatus = "MAPPED"
	REDUCED TaskStatus = "REDUCED"
)

type Coordinator struct {
	// Your definitions here.
	files         []string
	nMap          int
	nReduce       int
	taskCounter   int
	completeTask  int      // 任务计数器
	schedulePhase JobPhase // 任务完成/调度时期
	taskHolder    map[int]*TaskInfo

	heartbeatCh chan *TaskInfo    // 发送任务，和保存的task一致
	responseCh  chan TaskResponse // 接收worker响应
}

type TaskInfo struct {
	Condition   TaskStatus
	TaskType    TaskType
	Id          int
	ProcessFile []string
	ReduceNum   int
}

type TaskResponse struct {
	TaskType  TaskType
	TaskId    int
	Condition TaskStatus
}

// Your code here -- RPC handlers for the worker to call.

// an example RPC handler.
//
// the RPC argument and reply types are defined in rpc.go.
func (c *Coordinator) Example(args *ExampleArgs, reply *ExampleReply) error {
	reply.Y = args.X + 1
	return nil
}

// start a thread that listens for RPCs from worker.go
func (c *Coordinator) server() {
	rpc.Register(c)
	rpc.HandleHTTP()
	l, e := net.Listen("tcp", ":1234")
	sockname := coordinatorSock()
	os.Remove(sockname)
	// l, e := net.Listen("unix", sockname)
	if e != nil {
		log.Fatal("listen error:", e)
	}
	go http.Serve(l, nil)
}

// main/mrcoordinator.go calls Done() periodically to find out
// if the entire job has finished.
func (c *Coordinator) Done() bool {
	ret := false

	// Your code here.
	if c.schedulePhase == DONE {
		ret = true
	}
	return ret
}

// create a Coordinator.
// main/mrcoordinator.go calls this function.
// nReduce is the number of reduce tasks to use.
func MakeCoordinator(files []string, nReduce int) *Coordinator {
	c := Coordinator{
		files:         files,
		nMap:          len(files),
		nReduce:       nReduce,
		taskHolder:    make(map[int]*TaskInfo),
		completeTask:  0,
		taskCounter:   0,
		schedulePhase: UNSTART,

		heartbeatCh: make(chan *TaskInfo, len(files)),
		responseCh:  make(chan TaskResponse, nReduce),
	}

	c.makeMaptask(files)

	c.server()
	return &c
}

func (c *Coordinator) makeMaptask(files []string) {
	fmt.Println("开始任务切割，并传送到TaskChannel")
	if c.schedulePhase == UNSTART {
		c.schedulePhase = MAPPING
	}

	for _, file := range files {
		task := TaskInfo{
			Condition:   WAITING,
			TaskType:    MAP,
			Id:          c.taskCounter,
			ProcessFile: []string{file},
			ReduceNum:   c.nReduce,
		}

		c.taskHolder[task.Id] = &task
		c.taskCounter++
		c.heartbeatCh <- &task
		fmt.Println(task.Id, "making map task")
	}
}

func (c *Coordinator) AssignTask(args *ExampleArgs, task *TaskInfo) error {
	if c.schedulePhase == MAPPING {
		*task = *<-c.heartbeatCh
	}
	return nil
}

func (c *Coordinator) ResponeseTask(response *TaskResponse, reply *ExampleArgs) error {
	c.responseCh <- *response
	return nil
}

// 修改任务状态
func (c *Coordinator) HandleResponse() {
	response := <-c.responseCh

	switch response.TaskType {
	case MAP:
		{
			id := response.TaskId
			c.taskHolder[id].Condition = response.Condition
			mu.Lock()
			c.completeTask++
			mu.Unlock()
		}
	}
}

func (c *Coordinator) checkDone() {
	mu.Lock()
	defer mu.Unlock()

	if c.schedulePhase == MAPPING && c.completeTask == c.nMap {
		c.schedulePhase = REDUCING
		c.completeTask = 0
	} else if c.schedulePhase == REDUCING && c.completeTask == c.nReduce {
		c.schedulePhase = DONE
		c.completeTask = 0
	}
}

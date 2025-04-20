package mr

// package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"net/rpc"
	"os"
	"strconv"
	"strings"
	"sync"
	"time"
)

// var mu sync.Mutex
var counterMutex sync.Mutex
var completeTaskMutex sync.Mutex
var taskHolderMutex sync.Mutex
var schedulePhaseMutex sync.RWMutex

type JobPhase string

const (
	UNSTART      JobPhase = "UNSTART"
	MAP_PHASE    JobPhase = "MAP_PHASE"
	REDUCE_PHASE JobPhase = "REDUCE_PHASE"
	DONE         JobPhase = "DONE"
	ERROR        JobPhase = "ERROR"
)

type TaskType string

const (
	MAP    TaskType = "MAP"
	REDUCE TaskType = "REDUCE"
	END    TaskType = "END"
)

type TaskStatus string

const (
	WAITING   TaskStatus = "WAITING"
	MAP_READY TaskStatus = "MAP_READY"
	MAPPING   TaskStatus = "MAPPING"
	MAPPED    TaskStatus = "MAPPED"

	REDUCE_READY TaskStatus = "REDUCE_READY"
	REDUCING     TaskStatus = "REDUCING"
	REDUCED      TaskStatus = "REDUCED"
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
	// crashCh     chan CrashMsg     // crash检测
}

// type CrashMsg struct {
// 	id  int
// 	msg string
// }

type TaskInfo struct {
	Condition   TaskStatus
	TaskType    TaskType
	Id          int
	ProcessFile []string
	ReduceNum   int
	StartTime   time.Time
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
	schedulePhaseMutex.Lock()
	if c.schedulePhase == DONE {
		ret = true
	}
	schedulePhaseMutex.Unlock()
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
	// map任务完成后分配reduce任务
	go c.makeReduceTask()

	go c.HandleResponse()
	go c.crashHandler()
	c.server()
	return &c
}

// 检查有没有任务crash
func (c *Coordinator) crashHandler() {
	exit := false

	for !exit {
		time.Sleep(time.Second * 1)

		for _, unitTask := range c.taskHolder {
			taskHolderMutex.Lock()
			if ((unitTask.StartTime != time.Time{}) && time.Now().Sub(unitTask.StartTime) > time.Second*5) && (unitTask.Condition == MAP_READY || unitTask.Condition == REDUCE_READY) {
				fmt.Println("crash task detected. type: ", unitTask.TaskType, " id: ", unitTask.Id, " starttime: ", unitTask.StartTime)

				// taskHolderMutex.Lock()
				if c.schedulePhase == MAP_PHASE {
					unitTask.Condition = WAITING
				} else if c.schedulePhase == REDUCE_PHASE {
					unitTask.Condition = MAPPED
				}
				unitTask.StartTime = time.Time{}
				// taskHolderMutex.Unlock()

				c.heartbeatCh <- unitTask
				fmt.Println("crash task resend to channel, task id", unitTask.Id)
			}
			taskHolderMutex.Unlock()
		}

		if c.schedulePhase == DONE {
			exit = true
		}
	}
}

// func (c *coordinator) sendAliveMsg()

func (c *Coordinator) makeMaptask(files []string) {
	fmt.Println("开始任务切割，并传送到TaskChannel")
	if c.schedulePhase == UNSTART {
		c.schedulePhase = MAP_PHASE
	}

	for _, file := range files {
		task := TaskInfo{
			Condition:   WAITING,
			TaskType:    MAP,
			Id:          c.taskCounter,
			ProcessFile: []string{file},
			ReduceNum:   c.nReduce,
			StartTime:   time.Time{},
		}

		taskHolderMutex.Lock()
		c.taskHolder[task.Id] = &task
		taskHolderMutex.Unlock()

		counterMutex.Lock()
		c.taskCounter++
		counterMutex.Unlock()
		c.heartbeatCh <- &task
		fmt.Println("task id: ", task.Id, "making map task")
	}
}

func TmpFileAssignHelper(whichReduce int, tmpFileDirectoryName string) []string {
	var res []string
	path, _ := os.Getwd()
	rd, _ := ioutil.ReadDir(path)
	for _, fi := range rd {
		if strings.HasPrefix(fi.Name(), "mr-tmp") && strings.HasSuffix(fi.Name(), strconv.Itoa(whichReduce)) {
			res = append(res, fi.Name())
		}
	}
	return res
}

// 分配reduce任务->通道
func (c *Coordinator) makeReduceTask() {
	alive := true
	for alive {
		if c.schedulePhase == REDUCE_PHASE {
			for i := 0; i < c.nReduce; i++ {
				id := c.taskCounter
				counterMutex.Lock()
				c.taskCounter++
				counterMutex.Unlock()

				task := TaskInfo{
					Id:          id,
					ReduceNum:   c.nReduce,
					ProcessFile: TmpFileAssignHelper(i, "main/mr-tmp"),
					TaskType:    REDUCE,
					Condition:   WAITING,
					StartTime:   time.Time{},
				}
				c.taskHolder[task.Id] = &task
				c.heartbeatCh <- &task
				fmt.Println("已发送reduce任务", task.Id, "	处理任务", task.ProcessFile)
			}
			alive = false
		}
	}
}

// 分配任务后task状态改变
func (c *Coordinator) changeTaskStatusBeforeAssign(taskid int, phase JobPhase) bool {

	taskHolderMutex.Lock()
	c.taskHolder[taskid].StartTime = time.Now()
	if c.schedulePhase == MAP_PHASE && c.taskHolder[taskid].Condition == WAITING {
		fmt.Println("task id: ", taskid, "修改状态 WAITING -> MAP_READY")
		c.taskHolder[taskid].Condition = MAP_READY
	} else if c.schedulePhase == REDUCE_PHASE && c.taskHolder[taskid].Condition == MAPPED {
		fmt.Println("task id: ", taskid, "修改状态 MAPPED -> MAP_READY")
		c.taskHolder[taskid].Condition = REDUCE_READY
	}
	taskHolderMutex.Unlock()

	return true
}

// worker请求任务入口，分配不同类型任务
func (c *Coordinator) AssignTask(args *ExampleArgs, task *TaskInfo) error {
	// mu.Lock()
	// defer mu.Unlock()

	// c.crashHandler()
	if c.schedulePhase == MAP_PHASE || c.schedulePhase == REDUCE_PHASE {
		select {
		case chanMsg := <-c.heartbeatCh:
			{
				if c.changeTaskStatusBeforeAssign(chanMsg.Id, c.schedulePhase) {
					fmt.Println("task id", chanMsg.Id, "准备执行任务，类型", task.TaskType)
				}
				*task = *chanMsg
			}
		}
	} else if c.schedulePhase == DONE {
		endTask := TaskInfo{}
		endTask.TaskType = END
		*task = endTask
	}
	return nil
}

func (c *Coordinator) ResponeseTask(response *TaskResponse, reply *ExampleArgs) error {
	c.responseCh <- *response
	return nil
}

// job状态根据phase和counter共同决定，每完成map reduce task都记录
func (c *Coordinator) HandleResponse() {
	alive := true
	for alive {

		response := <-c.responseCh
		fmt.Println("resonse handle...")
		switch response.TaskType {
		case MAP, REDUCE:
			{
				id := response.TaskId

				taskHolderMutex.Lock()
				c.taskHolder[id].Condition = response.Condition // 单个task完成后状态修改
				c.taskHolder[id].StartTime = time.Time{}
				taskHolderMutex.Unlock()

				completeTaskMutex.Lock()
				c.completeTask++
				c.checkJobPhase()
				completeTaskMutex.Unlock()

				fmt.Println("task id: ", response.TaskId, "task type: ", response.TaskType, "已确认完成任务，状态已修改")
			}
		}
		if c.schedulePhase == DONE {
			alive = false
		}
	}
}

// 检查job phase condition
func (c *Coordinator) checkJobPhase() {
	schedulePhaseMutex.Lock()
	if c.schedulePhase == MAP_PHASE && c.completeTask == c.nMap {
		// schedulePhaseMutex.Lock()
		c.schedulePhase = REDUCE_PHASE
		c.completeTask = 0
		// schedulePhaseMutex.Unlock()
	} else if c.schedulePhase == REDUCE_PHASE && c.completeTask == c.nReduce {
		// schedulePhaseMutex.Lock()
		c.schedulePhase = DONE
		c.completeTask = 0
		// schedulePhaseMutex.Unlock()
	}
	schedulePhaseMutex.Unlock()
}

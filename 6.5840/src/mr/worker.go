package mr

// package main

import (
	"fmt"
	"hash/fnv"
	"io/ioutil"
	"json"
	"log"
	"net/rpc"
	"os"
	"strconv"
)

// Map functions return a slice of KeyValue.
type KeyValue struct {
	Key   string
	Value string
}

// use ihash(key) % NReduce to choose the reduce
// task number for each KeyValue emitted by Map.
func ihash(key string) int {
	h := fnv.New32a()
	h.Write([]byte(key))
	return int(h.Sum32() & 0x7fffffff)
}

// main/mrworker.go calls this function.
func Worker(mapf func(string, string) []KeyValue,
	reducef func(string, []string) string) {

	alive := true
	for alive {
		task := RequestTask()
		switch task.TaskType {
		case MAP:
			{
				fmt.Println(task.Id, "MAP 任务处理中")
				doMapTask(mapf, task)
				fmt.Println(task.Id, "MAP 任务完成")
				doneTask(task)
			}
		}
	}
}

// 返回任务结果，可以使用channel
func doneTask(task TaskInfo) {

}

func RequestTask() TaskInfo {
	reply := TaskInfo{}
	args := ExampleArgs{}
	ok := call("Coordinator.AssignTask", &args, &reply)
	if !ok {
		fmt.Println("请求任务失败")
	}

	fmt.Println(reply.TaskType, "请求任务成功")
	return reply
}

func doMapTask(mapf func(string, string) []KeyValue, task TaskInfo) {
	var intermediate []KeyValue
	filename := task.ProcessFile[0]
	file, err := os.Open(filename)
	if err != nil {
		log.Fatalf("cannot open %v", filename)
	}
	content, err := ioutil.ReadAll(file)
	if err != nil {
		log.Fatalf("cannot read %v", filename)
	}
	file.Close()

	intermediate = mapf(filename, string(content)) // 任务文件中内容转换成中间表示存储起来

	// 中间表示放在hashkv中
	reduceNum := task.ReduceNum
	HashKV := make([][]KeyValue, reduceNum)
	for _, kv := range intermediate {
		HashKV[ihash(kv.Key)%reduceNum] = append(HashKV[ihash(kv.Key)%reduceNum], kv)
	}

	for i := 0; i < reduceNum; i++ {
		oname := "mr-tmp-" + strconv.Itoa(task.Id) + "-" + strconv.Itoa(i)
		ofile, _ := os.Create(oname)
		enc := json.NewEncoder(ofile)
		for _, kv := range HashKV[i] {
			enc.Encode(kv)
		}
		ofile.Close()
	}
}

// example function to show how to make an RPC call to the coordinator.
//
// the RPC argument and reply types are defined in rpc.go.
func CallExample() {

	// declare an argument structure.
	args := ExampleArgs{}

	// fill in the argument(s).
	args.X = 99

	// declare a reply structure.
	reply := ExampleReply{}

	// send the RPC request, wait for the reply.
	// the "Coordinator.Example" tells the
	// receiving server that we'd like to call
	// the Example() method of struct Coordinator.
	ok := call("Coordinator.Example", &args, &reply)
	if ok {
		// reply.Y should be 100.
		fmt.Printf("reply.Y %v\n", reply.Y)
	} else {
		fmt.Printf("call failed!\n")
	}
}

// send an RPC request to the coordinator, wait for the response.
// usually returns true.
// returns false if something goes wrong.
func call(rpcname string, args interface{}, reply interface{}) bool {
	// c, err := rpc.DialHTTP("tcp", "127.0.0.1"+":1234")
	// sockname := CoordinatorSock()
	c, err := rpc.DialHTTP("tcp", "127.0.0.1"+":1234")
	if err != nil {
		log.Fatal("dialing:", err)
	}
	defer c.Close()

	err = c.Call(rpcname, args, reply)
	if err == nil {
		return true
	}

	fmt.Println(err)
	return false
}

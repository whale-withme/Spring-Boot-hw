package kvraft

import (
	"log"
	"sync"
	"sync/atomic"
	"time"

	"6.824/labgob"
	"6.824/labrpc"
	"6.824/raft"
	"golang.org/x/text/message"
)

const Debug = false

func DPrintf(format string, a ...interface{}) (n int, err error) {
	if Debug {
		log.Printf(format, a...)
	}
	return
}

type Op struct {
	// Your definitions here.
	// Field names must start with capital letters,
	// otherwise RPC will break.
}

type KVServer struct {
	mu      sync.RWMutex
	me      int
	rf      *raft.Raft
	applyCh chan raft.ApplyMsg
	dead    int32 // set by Kill()

	maxraftstate   int // snapshot if log grows this big
	statemachine   KVStateMachine
	lastapplied    int
	lastOperations map[int64]OperationContext
	// Your definitions here.

	notifychans map[int]chan *CommandResponse
}

func (kv *KVServer) Get(args *GetArgs, reply *GetReply) {
	// Your code here.
}

func (kv *KVServer) PutAppend(args *PutAppendArgs, reply *PutAppendReply) {
	// Your code here.
}

func (kv *KVServer) isDuplicateRequest(requestId int64, clientId int64) bool {
	operationContext, ok := kv.lastOperations[clientId]
	return ok && requestId <= operationContext.MaxAppliedCommandId
}

func (kv *KVServer) getNotifyChans(index int) chan *CommandResponse {
	if _, ok := kv.notifychans[index]; !ok {
		kv.notifychans[index] = make(chan *CommandResponse, 1)
	}
	return kv.notifychans[index]
}

func (kv *KVServer) removeNotifyChans(index int) {
	delete(kv.notifychans, index)
}

func (kv *KVServer) Command(request *CommandRequest, response *CommandResponse) {
	kv.mu.RLock()
	if request.Op != OpGet && kv.isDuplicateRequest(request.CommandId, request.ClientId) {
		lastOperationContext := kv.lastOperations[request.ClientId]
		response.Value = lastOperationContext.LastResponse.Value
		response.Err = lastOperationContext.LastResponse.Err
		kv.mu.RLock()
		return
	}

	index, _, isleader := kv.rf.Start(Command{request})
	if !isleader {
		response.Err = Err_WrongLeader
		return
	}

	kv.mu.Lock()
	notifyCh := kv.getNotifyChans(index)
	kv.mu.Unlock()
	select {
	case result := <-notifyCh:
		response.Err, response.Value = result.Err, result.Value
	case <-time.After(KVSERVER_TIMEOUT):
		response.Err = Err_Timeout
	}
	go func() {
		kv.mu.Lock()
		kv.removeNotifyChans(index)
		kv.mu.Unlock()
	}()
}

// the tester calls Kill() when a KVServer instance won't
// be needed again. for your convenience, we supply
// code to set rf.dead (without needing a lock),
// and a killed() method to test rf.dead in
// long-running loops. you can also add your own
// code to Kill(). you're not required to do anything
// about this, but it may be convenient (for example)
// to suppress debug output from a Kill()ed instance.
func (kv *KVServer) Kill() {
	atomic.StoreInt32(&kv.dead, 1)
	kv.rf.Kill()
	// Your code here, if desired.
}

func (kv *KVServer) killed() bool {
	z := atomic.LoadInt32(&kv.dead)
	return z == 1
}

// servers[] contains the ports of the set of
// servers that will cooperate via Raft to
// form the fault-tolerant key/value service.
// me is the index of the current server in servers[].
// the k/v server should store snapshots through the underlying Raft
// implementation, which should call persister.SaveStateAndSnapshot() to
// atomically save the Raft state along with the snapshot.
// the k/v server should snapshot when Raft's saved state exceeds maxraftstate bytes,
// in order to allow Raft to garbage-collect its log. if maxraftstate is -1,
// you don't need to snapshot.
// StartKVServer() must return quickly, so it should start goroutines
// for any long-running work.
func StartKVServer(servers []*labrpc.ClientEnd, me int, persister *raft.Persister, maxraftstate int) *KVServer {
	// call labgob.Register on structures you want
	// Go's RPC library to marshall/unmarshall.
	labgob.Register(Command{})
	applyCh := make(chan raft.ApplyMsg, 1)

	kv := &KVServer{
		me:             me,
		rf:             raft.Make(servers, me, persister, applyCh),
		applyCh:        applyCh,
		lastapplied:    0,
		dead:           0,
		lastOperations: make(map[int64]OperationContext),
		statemachine:   NewMemoryKV(),
		notifychans:    make(map[int]chan *CommandResponse),
	}

	go kv.applier()
	DPrintf("kv%v start", me)
	return kv
}

func (kv *KVServer) applyLogtoStateMachine(command Command) CommandResponse {
	op := command.Op
	var response CommandResponse
	switch op {
	case OpGet:
		response.Value, response.Err = kv.statemachine.Get(command.Key)
	case OpPut:
		response.Err = kv.statemachine.Put(command.Key, command.Value)
	case OpAppend:
		response.Err = kv.statemachine.Append(command.Key, command.Value)
	}
	return response
}

func (kv *KVServer) applier() {
	for !kv.killed() {
		select {
		case message := <-kv.applyCh:
			if message.CommandValid == true {
				kv.mu.Lock()
				if message.CommandIndex < kv.lastapplied {
					kv.mu.Unlock()
					continue
				}
				kv.lastapplied = message.CommandIndex

				var response CommandResponse
				command := message.Command.(Command)
				if command.Op != OpGet && kv.isDuplicateRequest(command.CommandId, command.ClientId) {
					response = kv.lastOperations[command.ClientId].LastResponse
					DPrintf("kv%v receive duplicate request%v from client%v", kv.me, command.CommandId, command.ClientId)
				} else {
					response := kv.applyLogtoStateMachine(command)
					if command.Op != OpGet {
						kv.lastOperations[command.ClientId] = OperationContext{command.CommandId, response}
					}
				}

				if currentTerm, isleader := kv.rf.GetState(); currentTerm == message.CommandTerm && isleader {
					notifyCh := kv.getNotifyChans(message.CommandIndex)
					notifyCh <- &response
				}
			}
		}
	}
}

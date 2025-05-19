package kvraft

import "time"

const KVSERVER_TIMEOUT = 500 * time.Millisecond

const (
	OK             = "OK"
	ErrNoKey       = "ErrNoKey"
	ErrWrongLeader = "ErrWrongLeader"
)

type Err uint

const (
	Ok Err = iota
	Err_WrongLeader
	Err_Timeout
	Err_Nokey
)

const (
	OpGet    string = "OpGet"
	OpPut    string = "OpPut"
	OpAppend string = "OpAppend"
)

// command interface{}
type Command struct {
	*CommandRequest
}

// Put or Append
type PutAppendArgs struct {
	Key   string
	Value string
	Op    string // "Put" or "Append"
	// You'll have to add definitions here.
	// Field names must start with capital letters,
	// otherwise RPC will break.
}

type PutAppendReply struct {
	Err Err
}

type GetArgs struct {
	Key string
	// You'll have to add definitions here.
}

type GetReply struct {
	Err   Err
	Value string
}

type CommandRequest struct {
	Key       string
	Value     string
	ClientId  int64
	CommandId int64
	Op        string
}

type CommandResponse struct {
	Err   Err
	Value string
}

// kvserver 维护的
type OperationContext struct {
	MaxAppliedCommandId int64
	LastResponse        CommandResponse
}

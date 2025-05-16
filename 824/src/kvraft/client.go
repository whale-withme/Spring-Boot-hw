package kvraft

import "6.824/labrpc"
import "crypto/rand"
import "math/big"

type Clerk struct {
	servers []*labrpc.ClientEnd
	// You will have to modify this struct.
	leaderId int64
	commadId int64
	clientId int64
}

func nrand() int64 {
	max := big.NewInt(int64(1) << 62)
	bigx, _ := rand.Int(rand.Reader, max)
	x := bigx.Int64()
	return x
}

func MakeClerk(servers []*labrpc.ClientEnd) *Clerk {
	return &Clerk{
		servers:  servers,
		leaderId: 0,
		clientId: nrand(),
		commadId: 0,
	}
}

// fetch the current value for a key.
// returns "" if the key does not exist.
// keeps trying forever in the face of all other errors.
//
// you can send an RPC with code like this:
// ok := ck.servers[i].Call("KVServer.Get", &args, &reply)
//
// the types of args and reply (including whether they are pointers)
// must match the declared types of the RPC handler function's
// arguments. and reply must be passed as a pointer.
func (ck *Clerk) Get(key string) string {

	return ck.common(&CommandRequest{Key: key, Op: OpGet})
}

// shared by Put and Append.
//
// you can send an RPC with code like this:
// ok := ck.servers[i].Call("KVServer.PutAppend", &args, &reply)
//
// the types of args and reply (including whether they are pointers)
// must match the declared types of the RPC handler function's
// arguments. and reply must be passed as a pointer.
func (ck *Clerk) PutAppend(key string, value string, op string) {
	// You will have to modify this function.
	commandRequest := CommandRequest{
		Key:   key,
		Value: value,
	}
	if op == "Put" {
		commandRequest.Op = OpPut
	} else if op == "Append" {
		commandRequest.Op = OpAppend
	} else {
		DPrintf("ck client%v: wrong command op %v", ck.clientId, op)
	}
	ck.common(&commandRequest)
}

func (ck *Clerk) Put(key string, value string) {
	ck.PutAppend(key, value, "Put")
}
func (ck *Clerk) Append(key string, value string) {
	ck.PutAppend(key, value, "Append")
}

func (ck *Clerk) common(request *CommandRequest) string {
	// clientId, commadId := ck.clientId, ck.commadId
	request.ClientId, request.CommandId = ck.clientId, ck.commadId
	for {
		var response CommandResponse
		if !ck.servers[ck.leaderId].Call("KVServer.Command", request, &response) || response.Err == Err_WrongLeader || response.Err == Err_Timeout {
			// test next leaderID
			ck.leaderId = (ck.leaderId + 1) % int64(len(ck.servers))
			continue
		}
		ck.commadId++
		return response.Value
	}
}

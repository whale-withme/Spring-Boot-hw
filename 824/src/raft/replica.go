package raft

import "sort"

// import "testing"

type AppendEntriesRequest struct {
	Term         int
	LeaderId     int
	PrevLogIndex int
	PrevLogTerm  int
	Entries      []Entry
	LeaderCommit int
}

type AppendEntriesResponse struct {
	Term          int
	Success       bool
	ConflictTerm  int
	ConflictIndex int
}

// leader broadcast heartbeat
func (rf *Raft) broadcastHeartbeat(isHeartbeat bool) {

	for peer := range rf.peers {
		if peer == rf.me {
			continue
		}
		if isHeartbeat {
			go rf.replicateOneRound(peer) // every server should receive, use routine.
			DPrintf("%v%v send heartbeat to server %v\n", rf.status, rf.me, peer)
		} else {
			rf.replicatorCond[peer].Signal()
		}
	}
}

// check preLogindex to send appendEntries or send heartbeat
// Leader use
func (rf *Raft) replicateOneRound(peer int) {
	rf.mu.Lock()
	if rf.status != LEADER {
		DPrintf("%v%v NOT leader\n", rf.status, rf.me)
		rf.mu.Unlock()
		return
	}

	// DPrintf("%v%v into replicaOneRound\n", rf.status, rf.me)
	prevLogIndex := rf.nextIndex[peer] - 1
	DPrintf("peer%v prevlogIndex: %v", peer, prevLogIndex)
	appendEntryRequest := rf.generateAppendEntryRequest(prevLogIndex)

	if prevLogIndex < rf.getFirstLog().Index {
		DPrintf("error, not complete installSnapshot")
		rf.mu.Unlock()
		snapshotRequest := rf.generateSnapshotRequest()
		snapshotResponse := new(InstallSnapshotResponse)
		if rf.sendInstallSnapshotRequest(peer, snapshotRequest, snapshotResponse) {
			rf.mu.Lock()
			rf.handleInstallSnapshotResponse(peer, snapshotRequest, snapshotResponse)
			rf.mu.Unlock()
		}
	} else {
		rf.mu.Unlock()
		response := new(AppendEntriesResponse)
		if rf.sendAppendEntryReqest(peer, appendEntryRequest, response) {
			// handle AppendEntry Response...
			rf.mu.Lock()
			// nextlogIndex cause bug.... prevlogIndex < 0
			rf.HandleAppendEntriesResponse(peer, appendEntryRequest, response)
			DPrintf("peer%v nextlogIndex: %v", peer, rf.nextIndex[peer])
			rf.mu.Unlock()
		}
	}

}

// generate appendEntriesRequest
func (rf *Raft) generateAppendEntryRequest(prevlogIndex int) *AppendEntriesRequest {
	firstIndex := rf.getFirstLog().Index

	// should send [nextlogIndex-firstlogIndex: ... ] instead of [firstlogIndex: nextlogIndex]
	entries := make([]Entry, len(rf.logs[prevlogIndex+1-firstIndex:]))

	// Init time: leader.logs[0] don't have [1...], but still can use slice
	// Logically it's right, and it will not cross-border(Slice)
	copy(entries, rf.logs[prevlogIndex+1-firstIndex:]) // [ nextIndex ... end ]
	DPrintf("peer%v generate len(entry) = %v", rf.me, len(entries))
	return &AppendEntriesRequest{
		Term:         rf.currentTerm,
		LeaderId:     rf.me,
		PrevLogIndex: prevlogIndex,
		PrevLogTerm:  rf.logs[prevlogIndex-firstIndex].Term,
		Entries:      entries,
		LeaderCommit: rf.commitIndex,
	}
}

// Not leader RPC receive AppendEntry
func (rf *Raft) AppendEntries(request *AppendEntriesRequest, response *AppendEntriesResponse) {
	// return fasle:
	// 	request.Term < rf.currentTerm 请求不是最新的term
	// 	rf.getLastLog().Index < request.PrevLogIndex leader记录延时，follower落后较多
	// 	rf.logs[rf.getLastLog().Index-index].Term != request.Term 多个leader出现导致未提交的日志暂存，需要删除

	rf.mu.Lock()
	defer rf.mu.Unlock()
	defer rf.persist()
	//DPrintf("server %v receive appendentry from %v, status %v\n", rf.me, request.LeaderId, rf.status)

	if request.Term < rf.currentTerm { // AppendEntry not valid
		response.Term, response.Success = rf.currentTerm, false
		return
	}

	if request.Term > rf.currentTerm { // Valid AppendEntry
		rf.currentTerm, rf.voteFor = request.Term, -1
	}
	rf.changeStatus(FOLLOWER)
	rf.electionTimer.Reset(RandomElectionTimeout())

	// If an existing entry conflicts with a new one (same index
	// but different terms), delete the existing entry and all that
	// follow it (§5.3)
	if !rf.matchLog(request.PrevLogIndex, request.PrevLogTerm) {
		response.Term, response.Success = rf.currentTerm, false

		if rf.getLastLog().Index < request.PrevLogIndex {
			response.ConflictTerm, response.ConflictIndex = -1, rf.getLastLog().Index
		} else {
			firstIndex := rf.getFirstLog().Index
			response.ConflictTerm = rf.logs[request.PrevLogIndex-firstIndex].Term
			index := request.PrevLogIndex - 1
			for index >= firstIndex && rf.logs[index-firstIndex].Term == response.ConflictTerm {
				index--
			}
			DPrintf("peer%v old_log index:%v term:%v roll_back_log index:%v term:%v", rf.me, request.PrevLogIndex, request.Term, index, rf.logs[index].Term)
			response.ConflictIndex = index
		}
		return
	}

	// Append any new entries not already in the log
	// consider two parts:
	// 1. follower conflicts with leader, and it replica from response.ConflictIndex + 1 in the NEXT REPLICA
	// 2. no conflicts, everything goes right. just localIndex > len(..) , it needs resize longer.
	firstIndex := rf.getFirstLog().Index
	for index, entry := range request.Entries {
		if entry.Index-firstIndex >= len(rf.logs) || rf.logs[entry.Index-firstIndex].Term != entry.Term {
			rf.logs = append(rf.logs[:entry.Index-firstIndex], request.Entries[index:]...)
			break
		}
	}

	// If leaderCommit > commitIndex, set commitIndex =
	// min(leaderCommit, index of last new entry)
	rf.advanceComitIndex(request.LeaderCommit)
	DPrintf("peer%v commitindex: %v", rf.me, rf.commitIndex)
	response.Success, response.Term = true, rf.currentTerm
}

func (rf *Raft) HandleAppendEntriesResponse(peer int, request *AppendEntriesRequest, response *AppendEntriesResponse) {
	//DPrintf("Before handle, peer%v prevlogIndex: %v", peer, rf.nextIndex[peer]-1)
	if rf.status == LEADER && request.Term == rf.currentTerm {
		if response.Success {
			rf.matchIndex[peer] = request.PrevLogIndex + len(request.Entries)
			rf.nextIndex[peer] = rf.matchIndex[peer] + 1
			// calculate math votes available ... and update rf.commitIndex if it needs.
			//rf.advanceComitIndexForLeader()
			rf.advanceComitIndexForLeader()
		} else {
			if response.Term > request.Term {
				rf.changeStatus(FOLLOWER)
				rf.currentTerm, rf.voteFor = response.Term, -1
				rf.persist()
			} else if response.Term == rf.currentTerm {
				rf.nextIndex[peer] = response.ConflictIndex
				if response.ConflictTerm != -1 {
					rf.nextIndex[peer] = response.ConflictIndex + 1
				}
			}
		}
	}
	//DPrintf("After handle, peer%v prevlogIndex: %v", peer, rf.nextIndex[peer]-1)
}

// update leader's commitIndex if majority of follower's matchIndex[] is true.
// Leader trigger
func (rf *Raft) advanceComitIndexForLeader() {
	n := len(rf.matchIndex)
	sortEntries := make([]int, n)
	copy(sortEntries, rf.matchIndex)
	sort.Slice(sortEntries, func(i, j int) bool {
		return sortEntries[i] < sortEntries[j]
	})

	majorityIndex := sortEntries[int(n/2)+1]
	if majorityIndex > rf.commitIndex {
		if rf.matchLog(majorityIndex, rf.currentTerm) {
			rf.commitIndex = majorityIndex
			DPrintf("leader%v update commitIndex to %v", rf.me, rf.commitIndex)
			rf.applyCond.Signal() // signal applyCh
		} else {
			DPrintf("leader%v calculate majority commitIndex is %v, not update", rf.me, rf.commitIndex)
		}
	}

}

func (rf *Raft) advanceComitIndex(leaderCommitIndex int) {
	minnerCommitindex := Min(leaderCommitIndex, rf.getLastLog().Index)
	if minnerCommitindex > rf.commitIndex { // BUG final!!!!!! not minnerCommitindex > leadercommitIndex
		rf.commitIndex = minnerCommitindex
		rf.applyCond.Signal()
		DPrintf("peer%v update minner commitIndex to %v", rf.me, minnerCommitindex)
	}
}

// upper function calls Start() and leader.logs[] appended, call this point signal
func (rf *Raft) replicator(peer int) {
	rf.replicatorCond[peer].L.Lock()
	defer rf.replicatorCond[peer].L.Unlock()

	for !rf.killed() {
		for !rf.needtoReplica(peer) {
			rf.replicatorCond[peer].Wait()
		}
		rf.replicateOneRound(peer)
	}
}

func (rf *Raft) needtoReplica(peer int) bool {
	rf.mu.Lock()
	defer rf.mu.Unlock()
	return rf.status == LEADER && rf.matchIndex[peer] < rf.getLastLog().Index
}

func (rf *Raft) sendAppendEntryReqest(peer int, request *AppendEntriesRequest, reply *AppendEntriesResponse) bool {
	ok := rf.peers[peer].Call("Raft.AppendEntries", request, reply)
	return ok
}

func (rf *Raft) matchLog(index int, term int) bool {
	return index <= rf.getLastLog().Index && rf.logs[index-rf.getFirstLog().Index].Term == term
}

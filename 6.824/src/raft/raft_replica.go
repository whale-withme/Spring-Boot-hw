package raft

import "fmt"

type AppendEntriesRequest struct {
	Term         int
	LeaderId     int
	PrevLogIndex int
	PrevLogTerm  int
	Entries      []Entry
	LeaderCommit int
}

type AppendEntriesResponse struct {
	term    int
	success bool
}

// leader broadcast heartbeat
func (rf *Raft) broadcastHeartbeat(isHeartbeat bool) {
	for peer := range rf.peers {
		if peer == rf.me {
			continue
		}
		if isHeartbeat {
			rf.replicateOneRound(peer)
			fmt.Printf("leaderId %v send heartbeat to server %v\n", rf.me, peer)
		}
	}
}

// check preLogindex to send appendEntries or send heartbeat
// Leader use
func (rf *Raft) replicateOneRound(peer int) {
	rf.mu.Lock()
	defer rf.mu.Unlock()
	if rf.status != LEADER {
		return
	}

	prevLogIndex := rf.nextIndex[peer] - 1
	appendEntry := rf.generateAppendEntryRequest(prevLogIndex)
	rf.mu.Unlock()

	// rf.mu.unlock() before send RPC
	response := AppendEntriesResponse{}
	if rf.sendAppendEntryReqest(peer, &appendEntry, &response) {
		rf.mu.Lock()
		// handle AppendEntryResponse...
	}
}

// generate appendEntriesRequest
func (rf *Raft) generateAppendEntryRequest(prevlogIndex int) AppendEntriesRequest {
	entry := make([]Entry, 1)

	// if in range of rf.logs[], then fill up
	// if no client command, entry should be empty.
	if prevlogIndex+1 < len(rf.logs) {
		entry = append(entry, rf.logs[prevlogIndex+1])
	}

	appendRequest := AppendEntriesRequest{
		Term:         rf.currentTerm,
		LeaderId:     rf.me,
		PrevLogIndex: prevlogIndex,
		PrevLogTerm:  rf.logs[prevlogIndex].Term,
		Entries:      entry,
		LeaderCommit: rf.commitIndex,
	}
	fmt.Printf("genreate appendentriesRequest, leaderId: %v, prevLogIndex:%v, prevlogTerm:%v\n", rf.me, prevlogIndex, appendRequest.PrevLogTerm)

	return appendRequest
}

// Not leader RPC receive AppendEntry
func (rf *Raft) AppendEntries(request *AppendEntriesRequest, response *AppendEntriesResponse) {
	rf.mu.Lock()
	defer rf.mu.Unlock()
	fmt.Printf("server %v receive appendentry from %v, status %v\n", rf.me, request.LeaderId, rf.status)

	// check request.Term acceptable
	if request.Term < rf.currentTerm {
		response.term, response.success = rf.currentTerm, false
		return
	}

	if request.Term > rf.currentTerm {
		rf.currentTerm = request.Term
	}

	// If an existing entry conflicts with a new one (same index
	// but different terms), delete the existing entry and all that
	// follow it (§5.3)
	if request.PrevLogIndex >= 0 && rf.logs[request.PrevLogIndex].Term != request.Term {
		rf.logs = rf.logs[:request.PrevLogIndex]
	}

	if len(request.Entries) > 0 {
		// If leaderCommit > commitIndex, set commitIndex =
		// min(leaderCommit, index of last new entry)
		rf.commitIndex = max(rf.commitIndex, request.LeaderCommit)
	}

	rf.changeServerStatus(FOLLOWER)
	rf.electionRest(RandomElectionTimeout())

	// replica log
	// ...

	response.success, response.term = true, rf.currentTerm
}

func (rf *Raft) sendAppendEntryReqest(peer int, request *AppendEntriesRequest, reply *AppendEntriesResponse) bool {
	ok := rf.peers[peer].Call("Raft.AppendEntries", request, reply)
	return ok
}

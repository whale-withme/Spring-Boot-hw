package raft

type AppendEntriesRequest struct {
	Term         int
	leaderId     int
	prevLogIndex int
	PrevLogTerm  int
	entries      []entry
	leaderCommit int
}

type AppendEntriesResponse struct {
	term    int
	success bool
}

// leader broadcast heartbeat
func (rf *Raft) broadcastHeartbeat(isHeartbeat bool) {
	for peer, _ := range rf.peers {
		if isHeartbeat {
			rf.replicateOneRound(peer)
		}
	}
}

// check preLogindex to send appendEntries or send heartbeat
func (rf *Raft) replicateOneRound(peer int) {

}

// send appendEntries
func (rf *Raft) AppendEntries(request *AppendEntriesRequest, response *AppendEntriesResponse) {
	rf.mu.Lock()
	defer rf.mu.Unlock()
	DPrintf("server %v receive appendentry from %v, status %v", rf.me, request.leaderId, rf.status)

	if request.Term < rf.currentTerm {
		response.term, response.success = rf.currentTerm, false
		return
	}

	if request.Term > rf.currentTerm {
		rf.currentTerm = request.Term
	}

	rf.changeServerStatus(FOLLOWER)
	rf.electionRest(RandomElectionTimeout())

	// replica log
	// ...

	response.success, response.term = true, rf.currentTerm
}

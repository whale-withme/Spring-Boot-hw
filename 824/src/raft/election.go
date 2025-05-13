package raft

// import "sync/atomic"

// send RequestVote RPC for all other servers.
// record response votes and check if get majority votes
// change state to leader/follower.
func (rf *Raft) startElection() {
	request := RequestVoteArgs{
		Term:        rf.currentTerm,
		CandidateId: rf.me,
	}

	var grantedVotes int = 1
	rf.voteFor = rf.me
	rf.persist()

	DPrintf("%v%v start election, term%v\n", rf.status, rf.me, rf.currentTerm)
	for serverId := range rf.peers {
		if serverId == rf.me {
			continue
		}

		go func(id int) {
			response := RequestVoteReply{}
			if rf.sendRequestVote(id, &request, &response) {
				rf.mu.Lock()
				defer rf.mu.Unlock()
				if rf.currentTerm == request.Term && rf.status == CANDIDATE {
					if response.VoteGranted {
						grantedVotes += 1
						if grantedVotes > len(rf.peers)/2 {
							DPrintf("{Node %v} receives majority votes in term %v", rf.me, rf.currentTerm)
							rf.changeStatus(LEADER)
							rf.broadcastHeartbeat(true)
						}
					} else if response.Term > rf.currentTerm {
						DPrintf("{Node %v} finds a new leader {Node %v} with term %v and steps down in term %v", rf.me, id, response.Term, rf.currentTerm)
						rf.changeStatus(FOLLOWER)
						rf.currentTerm, rf.voteFor = response.Term, -1
						rf.persist()
					}
				}
			}
		}(serverId)
	}
}

// example RequestVote RPC handler.
// Other server calls handler, rf gives resonse
func (rf *Raft) RequestVote(request *RequestVoteArgs, response *RequestVoteReply) {
	// Your code here (2A, 2B).
	rf.mu.Lock()
	defer rf.mu.Unlock()
	defer rf.persist()

	// DPrintf("%v%v handle requestVote from Candidate%v...\n", rf.status, rf.me, request.CandidateId)
	if request.Term < rf.currentTerm || (request.Term == rf.currentTerm && rf.voteFor != -1) {
		response.Term, response.VoteGranted = rf.currentTerm, false
		return
	}

	if request.Term > rf.currentTerm {
		rf.changeStatus(FOLLOWER)
		rf.currentTerm = request.Term
		rf.voteFor = -1 // clean voteFor
	}

	// Reply false if log doesn’t contain an entry at prevLogIndex
	// whose term matches prevLogTerm (§5.3)
	if !rf.isLogupdate(request.LastLogTerm, request.LastLogIndex) {
		response.Term = rf.currentTerm
		response.VoteGranted = false
	}

	rf.voteFor = request.CandidateId
	rf.electionTimer.Reset(RandomElectionTimeout())
	// DPrintf("%v%v reset election timer\n", rf.status, rf.me)

	response.Term, response.VoteGranted = request.Term, true
	if response.VoteGranted {
		DPrintf("%v%v votefor candidate%v\n", rf.status, rf.me, request.CandidateId)
	} else {
		DPrintf("%v%v NOT votefor candidate%v\n", rf.status, rf.me, request.CandidateId)
	}
	// DPrintf("Condidate id %v, current term %v, follower %v voteGranted: %v\n", request.CandidateId, request.Term, rf.me, response.VoteGranted)
}

func (rf *Raft) isLogupdate(term, index int) bool {
	// lastLogIndex := rf.getLastLog().Index
	// return term > rf.logs[lastLogIndex].Term || ((term == rf.logs[lastLogIndex].Term) && (index >= lastLogIndex))
	lastlog := rf.getLastLog()
	return term > lastlog.Term || (term == lastlog.Term && index >= lastlog.Index)
}

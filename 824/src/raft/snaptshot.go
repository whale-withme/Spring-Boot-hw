package raft

func (rf *Raft) generateSnapshotRequest() *InstallSnapshotRequest {
	firstlog := rf.getFirstLog()

	return &InstallSnapshotRequest{
		Term:              rf.currentTerm,
		LeaderId:          rf.me,
		LastIncludedIndex: firstlog.Index,
		LastIncludedTerm:  firstlog.Term,
		Data:              rf.persister.ReadSnapshot(),
	}
}

func (rf *Raft) InstallSnapshot(request *InstallSnapshotRequest, response *InstallSnapshotResponse) {
	rf.mu.Lock()
	defer rf.mu.Unlock()

	response.Term = rf.currentTerm

	// leader.term is outdate
	if request.Term < rf.currentTerm {
		return
	}

	if request.Term > rf.currentTerm {
		rf.currentTerm = request.Term
		rf.persist()
	}
	rf.changeStatus(FOLLOWER)
	rf.electionTimer.Reset(RandomElectionTimeout())

	// if snapshot.lastIncludeIndex < rf.commitIndex, not modify state machine
	if request.LastIncludedIndex < rf.commitIndex {
		return
	}

	// apply snapshot to state machine
	go func() {
		rf.applyCh <- ApplyMsg{
			SnapshotValid: true,
			Snapshot:      request.Data,
			SnapshotTerm:  request.LastIncludedTerm,
			SnapshotIndex: request.LastIncludedIndex,
		}
	}()
}

func (rf *Raft) handleInstallSnapshotResponse(peer int, request *InstallSnapshotRequest, response *InstallSnapshotResponse) {
	if rf.status == LEADER && request.Term == rf.currentTerm {
		if response.Term > rf.currentTerm {
			rf.changeStatus(FOLLOWER)
			rf.voteFor, rf.currentTerm = -1, response.Term
			rf.persist()
			rf.electionTimer.Reset(RandomElectionTimeout())
		} else {
			rf.matchIndex[peer], rf.nextIndex[peer] = request.LastIncludedIndex, request.LastIncludedIndex+1
		}
	}
}

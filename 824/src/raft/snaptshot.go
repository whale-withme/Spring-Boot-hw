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
		rf.currentTerm, rf.voteFor = request.Term, -1
		rf.persist()
	}
	rf.changeStatus(FOLLOWER)
	rf.electionTimer.Reset(RandomElectionTimeout())

	// if snapshot.lastIncludeIndex < rf.commitIndex, not modify state machine
	if request.LastIncludedIndex <= rf.commitIndex {
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
			// rf.electionTimer.Reset(RandomElectionTimeout())
		} else {
			rf.matchIndex[peer], rf.nextIndex[peer] = request.LastIncludedIndex, request.LastIncludedIndex+1
			DPrintf("peer%v receive snapshot, nextIndex:%v, matchIndex:%v", rf.me, request.LastIncludedIndex, request.LastIncludedIndex+1)
		}
	}
}

// A service wants to switch to snapshot.  Only do so if Raft hasn't
// have more recent info since it communicate the snapshot on applyCh.
func (rf *Raft) CondInstallSnapshot(lastIncludedTerm int, lastIncludedIndex int, snapshot []byte) bool {
	rf.mu.Lock()
	defer rf.mu.Unlock()

	if lastIncludedIndex <= rf.commitIndex {
		DPrintf("peer%v's condInstallSnapshot outdate, lastIncludeIndex %v commmitIndex %v", rf.me, lastIncludedIndex, rf.commitIndex)
		return false
	}

	if lastIncludedIndex > rf.getLastLog().Index {
		rf.logs = make([]Entry, 1)
	} else {
		rf.logs = shrinkEntriesArray(rf.logs[lastIncludedIndex-rf.getFirstLog().Index:])
		rf.logs[0].Command = nil
	}

	rf.logs[0].Term, rf.logs[0].Index = lastIncludedTerm, lastIncludedIndex
	rf.lastApplied, rf.commitIndex = lastIncludedIndex, lastIncludedIndex
	rf.persister.SaveStateAndSnapshot(rf.encodeState(), snapshot)

	DPrintf("peer%v receive condInstallSnapshot to lastincludeIndex %v ", rf.me, lastIncludedIndex)
	return true
}

// the service says it has created a snapshot that has
// all info up to and including index. this means the
// service no longer needs the log through (and including)
// that index. Raft should now trim its log as much as possible.
func (rf *Raft) Snapshot(index int, snapshot []byte) {
	// Your code here (2D).
	rf.mu.Lock()
	defer rf.mu.Unlock()

	firstlog := rf.getFirstLog()
	if index < firstlog.Index {
		DPrintf("peer%v receive outdate snapshot index %v, firstlogIndex %v", rf.me, index, firstlog.Index)
		return
	}

	rf.logs = shrinkEntriesArray(rf.logs[index-firstlog.Index:])
	rf.logs[0].Command = nil
	rf.persister.SaveStateAndSnapshot(rf.encodeState(), snapshot)

}

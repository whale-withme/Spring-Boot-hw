package raft

// the service using Raft (e.g. a k/v server) wants to start
// agreement on the next command to be appended to Raft's log. if this
// server isn't the leader, returns false. otherwise start the
// agreement and return immediately. there is no guarantee that this
// command will ever be committed to the Raft log, since the leader
// may fail or lose an election. even if the Raft instance has been killed,
// this function should return gracefully.
//
// the first return value is the index that the command will appear at
// if it's ever committed. the second return value is the current
// term. the third return value is true if this server believes it is
// the leader.
func (rf *Raft) Start(command interface{}) (int, int, bool) {
	rf.mu.Lock()
	defer rf.mu.Unlock()

	// Your code here (2B).
	if rf.status != LEADER {
		return -1, -1, false
	}

	newEntry := Entry{rf.currentTerm, command, rf.getLastLog().Index + 1}
	rf.logs = append(rf.logs, newEntry)
	rf.matchIndex[rf.me], rf.nextIndex[rf.me] = newEntry.Index, newEntry.Index+1
	rf.persist()
	rf.broadcastHeartbeat(false)

	return newEntry.Index, newEntry.Term, true
}

func (rf *Raft) applier() {
	for !rf.killed() {
		rf.mu.Lock()
		for rf.lastApplied >= rf.commitIndex {
			rf.applyCond.Wait()
		}

		commitIndex, appliedIndex, firstIndex := rf.commitIndex, rf.lastApplied, rf.getFirstLog().Index
		entries := make([]Entry, commitIndex-appliedIndex)
		copy(entries, rf.logs[appliedIndex+1-firstIndex:commitIndex+1-firstIndex])
		rf.mu.Unlock()

		for _, entry := range entries {
			rf.applyCh <- ApplyMsg{
				CommandValid: true,
				Command:      entry.Command,
				CommandIndex: entry.Index,
				CommandTerm:  entry.Term,
			}
		}

		rf.mu.Lock()
		DPrintf("leader%v send ApplyMsg to channel", rf.me)
		rf.lastApplied = max(rf.lastApplied, commitIndex)
		rf.mu.Unlock()
	}
}

package raft

// The ticker go routine starts a new election if this peer hasn't received
// heartsbeats recently.
func (rf *Raft) ticker() {
	for rf.killed() == false {

		// Your code here to check if a leader election should
		// be started and to randomize sleeping time using
		// time.Sleep().
		// rf.electionMutex.Lock()
		// defer rf.electionMutex.Unlock()
		select {
		case <-rf.electionTimer.C:
			{
				// if peer is leader, it should not election?
				rf.mu.Lock()
				DPrintf("election timeout tigger. follower%v (term %v)ready to change to candidate\n", rf.me, rf.currentTerm)
				rf.changeStatus(CANDIDATE)
				rf.currentTerm += 1
				rf.startElection()
				rf.electionTimer.Reset(RandomElectionTimeout())
				rf.mu.Unlock()
			}
		case <-rf.heartbeatTimer.C:
			{
				rf.mu.Lock()
				// DPrintf("leader heartbeat timout\n")
				if rf.status == LEADER {
					rf.broadcastHeartbeat(true)
					rf.heartbeatTimer.Reset(FixedHeartbeatTimeout()) // leader reset timer after send heartbeat
				}
				// rf.heartbeatTimer.Reset(FixedHeartbeatTimeout())
				rf.mu.Unlock()
			}
		}
	}
}

// change state for server state
// you should clean rf.voteFor and reset rf.currentTerm manually
func (rf *Raft) changeStatus(targetStatus serverStatus) {
	if rf.status == targetStatus {
		return
	}

	rf.status = targetStatus
	switch targetStatus {
	case FOLLOWER:
		{
			rf.heartbeatTimer.Stop()
			rf.electionTimer.Reset(RandomElectionTimeout())
		}
	case LEADER:
		{
			// Hint: One way to fail to reach agreement in the early Lab 2B
			// tests is to hold repeated elections even though the leader is alive.
			// rf.electionTimer.Stop()
			// rf.heartbeatTimer.Reset(FixedHeartbeatTimeout())
			lastlog := rf.getLastLog()
			for i := 0; i < len(rf.peers); i++ {
				rf.matchIndex[i], rf.nextIndex[i] = 0, lastlog.Index+1
			}
			rf.electionTimer.Stop()
			rf.heartbeatTimer.Reset(FixedHeartbeatTimeout())
		}
	}
}

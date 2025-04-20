package raft

import "time"
import "math/rand"

// The ticker go routine starts a new election if this peer hasn't received
// heartsbeats recently.
func (rf *Raft) ticker() {
	for rf.killed() == false {

		// Your code here to check if a leader election should
		// be started and to randomize sleeping time using
		// time.Sleep().
		select {
		case <-rf.electionTimer.C:
			{
				rf.mu.Lock()
				DPrintf("election timeout tigger. %v follower(term %v)ready to change to candidate", rf.me, rf.currentTerm)
				rf.changeServerStatus(CANDIDATE)
				rf.currentTerm += 1
				// rf.electionTimer.Reset(RandomElectionTimeout())
				rf.startElection()
				rf.electionRest(RandomElectionTimeout())
				rf.mu.Unlock()
			}
		case <-rf.heartbeatTimer.C:
			{
				rf.mu.Lock()
				DPrintf("leader heartbeat timout")
			}
		}
	}
}

// no mutex lock
func RandomElectionTimeout() time.Duration {
	// random 200-300 ms election timeout.
	randomDuration := time.Duration(rand.Intn(200)+150) * time.Millisecond
	return randomDuration
}

// start a new election timer, it sends tick to chan
// no mutex lock
func (rf *Raft) electionRest(timeout time.Duration) {
	rf.electionTimer = time.NewTimer(timeout)
	go func() {
		<-rf.electionTimer.C
	}()
}

// no mutex lock
func (rf *Raft) FixedHeartbeatTimeout() time.Duration {
	fixedTimeout := time.Duration(100) * time.Millisecond
	return fixedTimeout
}

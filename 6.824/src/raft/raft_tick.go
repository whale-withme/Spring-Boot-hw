package raft

import (
	"fmt"
	"math/rand"
	"time"
)

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
				fmt.Printf("election timeout tigger. follower%v (term %v)ready to change to candidate\n", rf.me, rf.currentTerm)
				// rf.changeServerStatus(CANDIDATE)
				rf.status = CANDIDATE
				rf.currentTerm += 1
				// rf.electionTimer.Reset(RandomElectionTimeout())
				// fmt.Printf("checkpoint: before starteelection()")
				rf.startElection()
				// fmt.Printf("checkpoint: after starteelection()")
				rf.electionRest(RandomElectionTimeout())
				rf.mu.Unlock()
			}
		case <-rf.heartbeatTimer.C:
			{
				rf.mu.Lock()
				// fmt.Printf("leader heartbeat timout\n")
				if rf.status == LEADER {
					rf.broadcastHeartbeat(true)
					rf.heartbeatRest(FixedHeartbeatTimeout()) // leader reset timer after send heartbeat
				}
				rf.mu.Unlock()
			}
		}
	}
}

// no mutex lock
func RandomElectionTimeout() time.Duration {
	// random 200-300 ms election timeout.
	randomDuration := time.Duration(rand.Intn(250)+150) * time.Millisecond
	return randomDuration
}

// start a new election timer, it sends tick to chan
// no mutex lock
func (rf *Raft) electionRest(timeout time.Duration) {
	rf.electionMutex.Lock()
	defer rf.electionMutex.Unlock()
	rf.electionTimer = time.NewTimer(timeout)
	go func() {
		<-rf.electionTimer.C
	}()
}

// reset heartbeatTimer with fixed timeout.
func (rf *Raft) heartbeatRest(timeout time.Duration) {
	rf.mu.Lock()
	defer rf.mu.Unlock()
	rf.heartbeatTimer = time.NewTimer(timeout)
	go func() {
		<-rf.heartbeatTimer.C
	}()
}

// no mutex lock
func FixedHeartbeatTimeout() time.Duration {
	fixedTimeout := time.Duration(100) * time.Millisecond
	return fixedTimeout
}

package raft

import "sync/atomic"

// send RequestVote RPC for all other servers.
// record response votes and check if get majority votes
// change state to leader/follower.
func (rf *Raft) startElection() {
	request := RequestVoteArgs{
		Term:        rf.currentTerm,
		CandidateId: rf.me,
	}

	var grantedVotes int32 = 1
	rf.voteFor = rf.me

	for serverId, _ := range rf.peers {
		if serverId == rf.me {
			continue
		}

		go func(id int) {
			response := RequestVoteReply{}
			if rf.sendRequestVote(serverId, &request, &response) {
				DPrintf("server %v response to candidate %v \n", serverId, request.CandidateId)
				if response.Term > rf.currentTerm && !response.VoteGranted {
					DPrintf("Candidate %v fail, receive bigger term %v\n", rf.me, response.Term)
					rf.currentTerm = response.Term // change status to follower?
					rf.changeServerStatus(FOLLOWER)
					rf.voteFor = -1
				}

				if response.Term == rf.currentTerm && rf.status == CANDIDATE {
					if response.VoteGranted {
						atomic.AddInt32(&grantedVotes, 1)
						if grantedVotes > int32(len(rf.peers)/2) {
							// Be a leader
							rf.changeServerStatus(LEADER)
							DPrintf("candiadte %v become leader, term %v", rf.me, rf.currentTerm)
							rf.broadcastHeartbeat(true)
						}
					}
				}
			}
		}(serverId)
	}
}

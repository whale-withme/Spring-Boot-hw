package raft

import "sync/atomic"
import "fmt"

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

	fmt.Printf("%v%v start election, term%v\n", rf.status, rf.me, rf.currentTerm)
	for serverId := range rf.peers {
		if serverId == rf.me {
			continue
		}

		go func(id int) {
			response := RequestVoteReply{}
			if rf.sendRequestVote(id, &request, &response) {
				rf.mu.Lock()
				defer rf.mu.Unlock()
				// fmt.Printf("server %v response to candidate %v \n", id, request.CandidateId)
				if response.Term > rf.currentTerm && !response.VoteGranted {
					fmt.Printf("Candidate %v fail, receive bigger term %v\n", rf.me, response.Term)
					rf.currentTerm = response.Term // change status to follower?
					rf.status = FOLLOWER
					rf.voteFor = -1
				}

				if response.Term == rf.currentTerm && rf.status == CANDIDATE {
					if response.VoteGranted {
						atomic.AddInt32(&grantedVotes, 1)
						fmt.Printf("server%v vote for candidate%v\n", id, rf.me)
						if grantedVotes > int32(len(rf.peers)/2) {
							// Be a leader
							// rf.changeServerStatus(LEADER)
							rf.status = LEADER
							fmt.Printf("candiadte %v become leader, term %v\n", rf.me, rf.currentTerm)
							rf.broadcastHeartbeat(true)
						}
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

	fmt.Printf("%v%v handle requestVote from Candidate%v...\n", rf.status, rf.me, request.CandidateId)
	if request.Term < rf.currentTerm || (request.Term == rf.currentTerm && rf.status == CANDIDATE && rf.voteFor != -1) {
		response.Term, response.VoteGranted = rf.currentTerm, false
		return
	}

	if request.Term > rf.currentTerm {
		rf.status = FOLLOWER
		rf.currentTerm = request.Term
		rf.voteFor = -1 // clean voteFor
	}

	// Reply false if log doesn’t contain an entry at prevLogIndex
	// whose term matches prevLogTerm (§5.3)

	rf.voteFor = request.CandidateId
	rf.electionRest(RandomElectionTimeout())
	fmt.Printf("%v%v reset election timer\n", rf.status, rf.me)
	response.Term, response.VoteGranted = request.Term, true
	if response.VoteGranted {
		fmt.Printf("%v%v votefor candidate%v\n", rf.status, rf.me, request.CandidateId)
	} else {
		fmt.Printf("%v%v NOT votefor candidate%v\n", rf.status, rf.me, request.CandidateId)
	}
	// fmt.Printf("Condidate id %v, current term %v, follower %v voteGranted: %v\n", request.CandidateId, request.Term, rf.me, response.VoteGranted)
}

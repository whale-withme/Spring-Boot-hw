package raft

import "log"
import (
	"math/rand"
	"sync"
	"time"
)

// Debugging
const Debug = false

func DPrintf(format string, a ...interface{}) (n int, err error) {
	if Debug {
		log.Printf(format, a...)
	}
	return
}

func insertionSort(sl []int) {
	a, b := 0, len(sl)
	for i := a + 1; i < b; i++ {
		for j := i; j > a && sl[j] < sl[j-1]; j-- {
			sl[j], sl[j-1] = sl[j-1], sl[j]
		}
	}
}

func Min(i int, j int) int {
	if i < j {
		return i
	} else {
		return j
	}
}

func Max(i int, j int) int {
	if i < j {
		return j
	} else {
		return i
	}
}

type lockedRand struct {
	mu   sync.Mutex
	rand *rand.Rand
}

func (r *lockedRand) Intn(n int) int {
	r.mu.Lock()
	defer r.mu.Unlock()
	return r.rand.Intn(n)
}

var globalRand = &lockedRand{
	rand: rand.New(rand.NewSource(time.Now().UnixNano())),
}

const (
	HeartbeatTimeout = 100
	ElectionTimeout  = 1200
)

// no mutex lock
func RandomElectionTimeout() time.Duration {
	// random 200-300 ms election timeout.
	// randomDuration := time.Duration(rand.Intn(1200)+200) * time.Millisecond
	// return randomDuration
	return time.Duration(ElectionTimeout+globalRand.Intn(ElectionTimeout)) * time.Millisecond
}

// no mutex lock
func FixedHeartbeatTimeout() time.Duration {
	// fixedTimeout := time.Duration(50) * time.Millisecond
	// return fixedTimeout
	return time.Duration(HeartbeatTimeout) * time.Millisecond
}

func shrinkEntriesArray(entries []Entry) []Entry {
	// We replace the array if we're using less than half of the space in
	// it. This number is fairly arbitrary, chosen as an attempt to balance
	// memory usage vs number of allocations. It could probably be improved
	// with some focused tuning.
	const lenMultiple = 2
	if len(entries)*lenMultiple < cap(entries) {
		newEntries := make([]Entry, len(entries))
		copy(newEntries, entries)
		return newEntries
	}
	return entries
}

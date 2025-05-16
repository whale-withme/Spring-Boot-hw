package kvraft

type KVStateMachine interface {
	Get(key string) (string, Err)
	Append(key string, value string) Err
	Put(key string, value string) Err
}

type MemoryKV struct {
	KV map[string]string
}

func NewMemoryKV() *MemoryKV {
	return &MemoryKV{make(map[string]string)}
}

func (memorykv *MemoryKV) Get(key string) (string, Err) {
	if value, ok := memorykv.KV[key]; ok {
		return value, Ok
	}
	return "", Err_Nokey
}

func (memorykv *MemoryKV) Append(key string, value string) Err {
	memorykv.KV[key] += value
	return Ok
}

func (memorykv *MemoryKV) Put(key string, value string) Err {
	memorykv.KV[key] = value
	return Ok
}

// package AtomicReference;

public class User {
    volatile String status;

    public User(String s){
        this.status = s;
    }

    public void set(String status){
        this.status = status;
    }

    public String get(){
        return status;
    }
}

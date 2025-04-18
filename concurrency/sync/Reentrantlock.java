import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Reentrantlock {
    final static ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException{
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 6, 2, TimeUnit.SECONDS, new ArrayBlockingQueue<>(3));
        Task task = new Task();
        threadPoolExecutor.execute(task);
        // ... 更多的线程
    }

    static class Task implements Runnable{
        @Override
        public void run(){
            Boolean trylock = false;
            try{
                trylock = REENTRANT_LOCK.tryLock(2, TimeUnit.SECONDS);
            }catch(InterruptedException err){

            }
            
            if(trylock){
                try{
                    System.out.println("获取锁成功");
                    Thread.sleep(200);
                }catch(InterruptedException err){
                    System.err.println(err);
                }finally{
                    REENTRANT_LOCK.unlock();
                }
            }
        }
    }
}

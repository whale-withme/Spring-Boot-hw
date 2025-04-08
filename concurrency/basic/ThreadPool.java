import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    public static void main(String[] args) throws InterruptedException, ExecutionException{
        CustomizeThread();
    }

    public static void scheduleWithFixedRate(){
        // 定时线程池
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
        scheduledExecutorService.scheduleAtFixedRate(()->{
            System.out.println("定时发送消息，间隔2s");
        }, 0, 2, TimeUnit.SECONDS);
        scheduledExecutorService.shutdown();
    }

    public static void CustomizeThread() throws InterruptedException, ExecutionException{
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            5,  // corePoolSize
            10, // maxiumPoolSize
            5,  // keepAliveTime
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5),    // workQueue
            new ThreadPoolExecutor.AbortPolicy() // handler
        );

        // submit 用于提交callable返回值的
        Future<String> future = threadPoolExecutor.submit(
            ()->{
                return "threadName : " + Thread.currentThread().getName();
            }
        );
        System.out.println(future.get());

        threadPoolExecutor.shutdown();
    }
}

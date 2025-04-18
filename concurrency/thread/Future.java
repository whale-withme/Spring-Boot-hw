import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Future{
    public static void main(String[] args) throws InterruptedException, ExecutionException{
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception{
                return "hello, callable and future." + "Thread name : " + Thread.currentThread().getName();
            }
        };

        FutureTask<String> future = new FutureTask<>(callable);  // 提交到线程池，利用futuretask 的特点，否则直接提交实现runnable、callable接口也可以
        executor.submit(future);

        System.out.println(future.get());
    }
}
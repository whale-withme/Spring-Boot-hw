import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HelloConcurrency {

    // 都是重写run 方法的
    public static class Mythread extends Thread{
        // 启动之后会调用run方法
        @Override
        public void run(){
            System.out.println("Start thread");
        }
    }

    static class Mythread2 implements Runnable{
        @Override
        public void run(){
            System.out.println("Runnable interface realize");
        }
    }

    // 实现callable接口的
    static class Task implements Callable<Integer>{
        @Override
        public Integer call() throws Exception{
            return 0;
        }
    }

    public static void main(String[] args){
        // 继承thread重写run函数
        Mythread thread = new Mythread();
        thread.start();

        // 函数式编程：匿名类
        new Thread(() ->{
            System.out.println("another thread start");
        }).start();

        // runnable 接口
        new Thread(new Mythread2()).start();

        // callable接口
        ExecutorService executorService = Executors.newCachedThreadPool();  // 线程池管理工具
        Task task = new Task();
        Future<Integer> result =  executorService.submit(task);
        try{
            System.out.println("callable interface result:" + result.get(2, TimeUnit.SECONDS));
        }catch(InterruptedException | TimeoutException | ExecutionException e){
            System.err.println(e);
        }
        executorService.shutdown();
    }
}

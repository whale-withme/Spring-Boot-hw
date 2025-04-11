import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CyclicBarrierTest {

    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 6, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));
    
    private static CyclicBarrier PRD_COUNT = new CyclicBarrier(1, new startDevelop());  // 回调函数必须是实现runnable接口的类
    private static CyclicBarrier DEVELOP_COUNT = new CyclicBarrier(4);
    // private CyclicBarrier TEST_COUNT = new CyclicBarrier(1);
    
    public static void main(String[] args) throws InterruptedException, BrokenBarrierException{
        System.out.println("产品经理正在准备...");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("产品经理完成");
        PRD_COUNT.await();
    }

    static class startDevelop implements Runnable{
        // 开启线程执行代码
        @Override
        public void run(){
            threadPoolExecutor.execute(new developCode("soar"));
            threadPoolExecutor.execute(new developCode("ids"));
            threadPoolExecutor.execute(new developCode("ai"));
            threadPoolExecutor.execute(new developCode("slips"));
        }
    }

    static class developCode implements Runnable{
        private String name;

        public developCode(String name){
            this.name = name;
        }

        @Override
        public void run(){
            try{
                System.out.println(name + "正在开发代码...");
                TimeUnit.SECONDS.sleep(1);
                System.out.println(name + "开发完毕");
                // 完成--
                DEVELOP_COUNT.await();
            }catch(InterruptedException | BrokenBarrierException err){
                System.err.println(err);
            } 
        }
    }
}
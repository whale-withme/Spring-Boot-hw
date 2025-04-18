import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadlocalIsolation {
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(5, 10, 6, TimeUnit.SECONDS, new ArrayBlockingQueue<>(3));


    public static void main(String[] args) {
        for(int i = 0; i < 10; i++){
            int finali = i;
            THREAD_POOL_EXECUTOR.execute(
                ()->{
                    // 每个线程初始化都会拿到一个thread_local
                    SimpleDateFormat simpleDateFormat = simpleDateCache.getSimpleDateFormat();
                    System.out.println(simpleDateFormat.format(new Date(finali * 1000)));
                }
            );
        }

        THREAD_POOL_EXECUTOR.shutdown();
    }

    class simpleDateCache{
        // getter setter remove 设置
        private final static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMATE_CACHE = ThreadLocal.withInitial(
            ()->{
                return new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            }
        );

        public static SimpleDateFormat getSimpleDateFormat(){
            return SIMPLE_DATE_FORMATE_CACHE.get();
        }

        public static void setSimpleDateFormat(SimpleDateFormat date){
            SIMPLE_DATE_FORMATE_CACHE.set(date);
        }

        public static void removeSimpleDateformat(){
            SIMPLE_DATE_FORMATE_CACHE.remove();
        }
    }
}

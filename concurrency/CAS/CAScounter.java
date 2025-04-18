// Removed package declaration to match the directory structure
import java.util.concurrent.atomic.AtomicInteger;

public class CAScounter {
    private AtomicInteger count = new AtomicInteger(0);

    // 每次调用 +1，要求线程安全，无锁
    public void increment() {
        // 自旋锁方式实现
        int now = count.get();
        int next = now + 1;
        while(!count.compareAndSet(now, next)){

        }
    }

    // 获取当前值
    public int get() {
        return count.get();
    }
}

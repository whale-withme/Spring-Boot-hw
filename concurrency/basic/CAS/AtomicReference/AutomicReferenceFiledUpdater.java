import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class AutomicReferenceFiledUpdater {
    private static final AtomicReferenceFieldUpdater<User, String> AutomicReferenceFiledUpdater = AtomicReferenceFieldUpdater.newUpdater(
        User.class,
        String.class,
        "status"
    );

    public static void main(String[] args){
        User user = new User("start");
        System.out.println(user.get());

        boolean updated = AutomicReferenceFiledUpdater.compareAndSet(user, "start", "PROCESSING");

        System.out.println("更新成功了吗？" + updated); // true
        System.out.println("当前状态：" + user.get()); // PROCESSING
    }
}

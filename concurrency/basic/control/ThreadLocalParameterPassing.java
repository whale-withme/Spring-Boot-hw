public class ThreadLocalParameterPassing {
    private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) {
        InterceptorTest interceptorTest = new InterceptorTest();
        ControllerTest controllerTest = new ControllerTest();
        EXECUTOR.execute(() ->{
            try{
                //调用前置拦截
                interceptorTest.beforeInterceptor(1);
                //执行controller逻辑
                controllerTest.deleteById(10);
            }finally {
                //调用后置拦截
                interceptorTest.afterInterceptor();
            }

        });
        EXECUTOR.execute(() ->{
            try{
                //调用前置拦截
                interceptorTest.beforeInterceptor(2);
                //执行controller逻辑
                controllerTest.deleteById(11);
            }finally {
                //调用后置拦截
                interceptorTest.afterInterceptor();
            }
        });
    }

}

class InterceptorTest {
    /**
     *
     * 前置拦截
     * 模拟从请求头中获取userId
     * @param userId 用户的名称
     */
    public void beforeInterceptor(int userId){
        //模拟查询数据库或者缓存
        User user;
        if(1 == userId) {
            user = new User("小红", userId, "北京市朝阳区");
        }else {
            user = new User("小绿", userId, "北京市海淀区");
        }
        UserContext.setUser(user);
    }

    /**
     * 后置拦截
     */
    public void afterInterceptor(){
        //删除本次线程缓存
        UserContext.removeUser();
    }
}

class ControllerTest {
    public void deleteById(Integer id) {
        ServiceTest serviceTest = new ServiceTest();
        serviceTest.deleteById(id);
    }
}

class ServiceTest {

    public void deleteById(Integer id) {
        User user = UserContext.getUser();

        try {
            System.out.println(user.getName() + "开始删除数据...");
            TimeUnit.SECONDS.sleep(2);
            System.out.println(user.getName() + "删除成功...");
            writeLog(true, "成功");
        } catch (InterruptedException e) {
            e.printStackTrace();
            writeLog(false, e.getMessage());
        }

    }

    public void writeLog(boolean success, String errorMessage){
        User user = UserContext.getUser();
        System.out.printf("开始记录日志：用户%s根据id删除了某个东西，结果是:%s, 信息是:%s%n", user.getName(), success, errorMessage);
    }
}

/**
 * 构建用户上下文
 */
class UserContext {
    private static final ThreadLocal<User> USER_CONTEXT = new ThreadLocal<>();

    public static User getUser(){
        return USER_CONTEXT.get();
    }
    public static void setUser(User user){
        USER_CONTEXT.set(user);
    }
    public static void removeUser(){
        USER_CONTEXT.remove();
    }

}

class User {
    private String name;
    private Integer userId;
    private String address;

    public User(String name, Integer userId, String address) {
        this.name = name;
        this.userId = userId;
        this.address = address;
    }

}
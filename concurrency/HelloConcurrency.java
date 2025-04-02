public class HelloConcurrency {

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
    
    public static void main(String[] args){
        // 继承thread重写run函数
        Mythread thread = new Mythread();
        thread.start();

        // 函数式编程：匿名类
        new Thread(() ->{
            System.out.println("another thread start");
        }).start();

        new Thread(new Mythread2()).start();
    }
}

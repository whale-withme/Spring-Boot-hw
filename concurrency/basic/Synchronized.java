public class Synchronized {
    public static void main(String[] args) throws InterruptedException{
        Task task = new Task();
        Thread kim = new Thread(task, "one");
        Thread tom = new Thread(task, "two");

        kim.start();
        tom.start();
        kim.join();
        // tom.join();
        System.out.println(task.get());
    }

    static class Task implements Runnable{
        static int count;

        @Override
        public void run(){
            add();
        }

        // 锁住整个类
        synchronized static public void add(){
            for(int i = 0; i < 1000; i++)
                count++;
        }

        static int get(){
            return count;
        }
    }
}

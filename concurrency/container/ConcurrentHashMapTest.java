import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapTest {
    private static ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        map.put("k", 1);
        System.out.println(map.get("k"));
    }
}

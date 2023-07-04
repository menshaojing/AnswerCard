import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CollectionGroupUtil
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/8/9 16:45
 * @Version 1.0
 */
public class CollectionGroupUtil {
    /**
     * 将list按照固定大小分组
     *
     * @param list
     * @param quantity
     * @return
     */
    public static List groupListByQuantity(List list, int quantity) {
        if (list == null || list.size() == 0) {
            return list;
        }
        List wrapList = new ArrayList();
        int count = 0;
        while (count < list.size()) {
            wrapList.add(list.subList(count, (count + quantity) > list.size() ? list.size() : count + quantity));
            count += quantity;
        }
        return wrapList;
    }


    public static void main(String[] args) {
        List<String> arr = new ArrayList<String>();
        arr.add("1");
        arr.add("2");
        arr.add("3");
        List ss = CollectionGroupUtil.groupListByQuantity(arr,2);
        System.out.println(ss);

    }
}

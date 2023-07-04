package answercard;

/**
 * @ClassName Test12
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/11 15:10
 * @Version 1.0
 */
public class Test12 {
    public static void main(String[] args) {
        String str="1234_455";
        System.out.println(str.lastIndexOf("_"));
        System.out.println(str.substring(str.lastIndexOf("_")+1));
    }
}

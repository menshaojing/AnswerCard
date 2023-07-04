package answercard;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;

/**
 * @ClassName Test1
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/11 9:17
 * @Version 1.0
 */
public class Test1 {
    public static void main(String[] args) {
      File file=new File("D:\\data\\resource\\data\\answercard\\10795\\1\\1.jpg");
        System.out.println(file.getName());
        System.out.println(file.getAbsolutePath());
    }
}

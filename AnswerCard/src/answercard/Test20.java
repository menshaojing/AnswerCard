package answercard;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName Test20
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/7/1 15:44
 * @Version 1.0
 */
public class Test20 {
    public static void main(String[] args) {
        System.out.println("T".codePointAt(0));
        Map<Integer, String> optionsMap=new HashMap<>(26);
        //≥ı º÷µ
        int index=26;
        int initialValue = 65;
        for (int i = 0; i < index; i++) {
            optionsMap.put(i+1,String.valueOf((char)(initialValue++)));
        }
        System.out.println(optionsMap);
    }
}

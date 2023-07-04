package answercard;

import sun.misc.BASE64Encoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * @ClassName ImageBase64
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/9/13 9:49
 * @Version 1.0
 */
public class ImageBase64 {
    public static void main(String[] args) {
        System.out.println( ImageToBase64("https://fxfile.ischool365.com:7382/group1/M00/02/9D/CgoKSWE5vuKACYi2AAAzV_GjB3w.cgoksw"));
    }
    private static String ImageToBase64(String imgPath) {

        byte[] data = null;
        // ��ȡͼƬ�ֽ�����
        try {
            URL url = new URL(imgPath);
            final byte[] by = new byte[1024];
            // ��������
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream in = conn.getInputStream();
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
           // log.error("ͼƬתbase64ʧ�� msg = {}", e.getMessage());
        }
        // ���ֽ�����Base64����
        BASE64Encoder encoder = new BASE64Encoder();
        // ����Base64������ֽ������ַ���
        String imageString = "data:image/png;base64," + encoder.encode(Objects.requireNonNull(data));

        return imageString;
    }
}

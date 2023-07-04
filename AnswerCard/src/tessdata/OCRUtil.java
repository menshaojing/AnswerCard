package tessdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author lsw
 *
 */
public class OCRUtil {

    //ʹ�ü�������
    public static final String LANG_USE = "chi_sim";


    public static void runOCR(String realPath, String imagePath, String outPath, boolean chooseLang,String lang) throws Exception {
        try {

            Runtime r = Runtime.getRuntime();
            //String cmd = "\""+realPath+"Tesseract-OCR\\tesseract.exe\" \""+imagePath+"\" \""+outPath+"\" -l "+(chooseLang?lang:"");
            String cmd =  "tesseract "+imagePath+" "+outPath+"  -l "+(chooseLang?lang:"");
            Process process = r.exec(cmd);
            System.out.println(cmd);

            //�� Windowsƽ̨�ϣ����б����ó����DOS�����ڳ���ִ����Ϻ������������Զ��رգ�
            // �Ӷ�����JavaӦ�ó���������waitfor()��䡣���¸������һ�����ܵ�ԭ���ǣ��ÿ�ִ�г���ı�׼����Ƚ϶࣬
            // �����д��ڵı�׼��������������󡣽���İ취�ǣ�����Java��Process���ṩ�ķ�����
            // Java������ػ񱻵��ó����DOS���д��ڵı�׼�������waitfor()����֮ǰ�������ڵı�׼����������е����ݡ�
            String s;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while((s=bufferedReader.readLine()) != null);
            System.out.println(s);


            process.waitFor();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

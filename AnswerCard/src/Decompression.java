
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *��ѹ�ļ�
 *@author wxisme
 *@time 2015-10-5 ����9:18:57
 */
public class Decompression {
     private String url;


     @Test
     public void test() throws Exception {
         unZip("D:\\data\\resource\\data\\answercard\\10629.zip", "D:\\data\\resource\\data\\answercard\\menshaojing",false);

     }
    /**
     * ��ѹzip�ļ�
     *
     * @param srcFilePath  Դ�ļ�·��
     * @param destFilePath Ŀ��·��
     * @param deleteSource �Ƿ�ɾ��Դ�ļ�
     * @return
     * @author Lvxianya
     * @create 2018/4/9
     */
    public static Boolean unZip(String srcFilePath, String destFilePath, Boolean deleteSource) throws IOException {
        if (!new File(srcFilePath).exists()) {
           // logger.error("��ѹ���ļ������ڣ�·��Ϊ:" + srcFilePath);
            return false;
        }

        String suffix = srcFilePath.substring(srcFilePath.lastIndexOf("."), srcFilePath.length());
        if (!".zip".equals(suffix)) {
           // logger.error("��ѹ���ļ���ʽ����ȷ��·��Ϊ:" + srcFilePath);
            return false;
        }

        OutputStream os = null;
        InputStream is = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFilePath, Charset.forName("GBK"));
            String directoryPath = "";
            if (null == destFilePath || "".equals(destFilePath)) {
                directoryPath = srcFilePath.substring(0, srcFilePath
                        .lastIndexOf("."));
            } else {
                directoryPath = destFilePath;
            }

            //�д�����
            File directoryPathFile = new File(directoryPath);
            if (!directoryPathFile.exists()) {
                directoryPathFile.mkdirs();
            }

            Enumeration<?> entryEnum = zipFile.entries();
            if (null != entryEnum) {
                ZipEntry zipEntry = null;
                while (entryEnum.hasMoreElements()) {
                    zipEntry = (ZipEntry) entryEnum.nextElement();
                    if (zipEntry.getSize() > 0) {
                        // �ļ�
                        File targetFile = new File(directoryPath + File.separator + zipEntry.getName());
                        os = new BufferedOutputStream(new FileOutputStream(targetFile));
                        is = zipFile.getInputStream(zipEntry);
                        byte[] buffer = new byte[4096];
                        int readLen = 0;
                        while ((readLen = is.read(buffer, 0, 4096)) >= 0) {
                            os.write(buffer, 0, readLen);
                            os.flush();
                        }
                        is.close();
                        os.close();
                    }
                    if (zipEntry.isDirectory()) {
                        String pathTemp = directoryPath + File.separator + zipEntry.getName();
                        File file = new File(pathTemp);
                        file.mkdirs();
                    }
                }
            }
            if (deleteSource) {
                File srcFile = new File(srcFilePath);
                if (srcFile.delete()) {
                  //  logger.info("file delete success");
                }
            }
            return true;
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
                if (null != os) {
                    os.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                    zipFile = null;
                }
            } catch (IOException ex) {
                //logger.error(ex.getMessage());
            }
        }
    }

}
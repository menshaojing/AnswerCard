
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *解压文件
 *@author wxisme
 *@time 2015-10-5 下午9:18:57
 */
public class Decompression {
     private String url;


     @Test
     public void test() throws Exception {
         unZip("D:\\data\\resource\\data\\answercard\\10629.zip", "D:\\data\\resource\\data\\answercard\\menshaojing",false);

     }
    /**
     * 解压zip文件
     *
     * @param srcFilePath  源文件路径
     * @param destFilePath 目标路径
     * @param deleteSource 是否删除源文件
     * @return
     * @author Lvxianya
     * @create 2018/4/9
     */
    public static Boolean unZip(String srcFilePath, String destFilePath, Boolean deleteSource) throws IOException {
        if (!new File(srcFilePath).exists()) {
           // logger.error("解压的文件不存在，路径为:" + srcFilePath);
            return false;
        }

        String suffix = srcFilePath.substring(srcFilePath.lastIndexOf("."), srcFilePath.length());
        if (!".zip".equals(suffix)) {
           // logger.error("解压的文件格式不正确，路径为:" + srcFilePath);
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

            //有待调整
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
                        // 文件
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
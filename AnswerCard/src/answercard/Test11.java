package answercard;

import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName Test11
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/7/23 13:09
 * @Version 1.0
 */
public class Test11 {
    public String jsonEx="D:\\data\\resource\\data\\";
    public int id=7804;
    public int examid=10688;
    //要读取的文件夹目录
    public String jsonpath="D:\\data\\resource\\data\\answercard\\10979";
    @Test
    public void test(){
        Vector<String> list = getAllPath(jsonpath);
        //去除zip
        List<String> listImage = list.stream().filter(s -> {
            return !s.substring(s.indexOf(".")+1).equals("zip");
        }).collect(Collectors.toList());
        Set stringVector = new HashSet<String>();
        listImage.forEach(string ->{
            stringVector.add(string.substring(0,string.lastIndexOf("\\")));
        });
        ArrayList<String> urlList = new ArrayList<>();

        StringBuilder builder=new StringBuilder();
        listImage.forEach(s -> {
          //  System.out.println(s.substring(jsonEx.length()));
            String sql="INSERT INTO t16043_teaching_marksystem.exam_scan (scanid, examid, schoolid, campusid, batchid, no, devicecode,\n" +
                    "                                                  filenames, createby, created_at, status, studentid, cards, updated_at,\n" +
                    "                                                  abnormalString)\n" +
                    "VALUES ("+id+", "+examid+", 2, 2, 1, 1, '', '"+s.substring(jsonEx.length())+"', 4,\n" +
                    "        sysdate(), 1, null, 'group1/M00/0E/2A/CgoKsmDcZxyABOhEAAMlikWuJMY877.jpg,', sysdate(),\n" +
                    "        null);";
            id++;
            builder.append(sql);
        });
        System.out.println(builder);
    }

    //获取所有文件
    public Vector<File> getAllFile(String datasetpath, Vector<File> vecFile) {
        File file = new File(datasetpath);
        File[] subFile = file.listFiles();
        for (int i = 0; i < subFile.length; i++) {
            if (subFile[i].isDirectory()) {
                getAllFile(subFile[i].getAbsolutePath(),vecFile);
            } else {
                vecFile.add(subFile[i]);
            }
        }
        return vecFile;
    }
    Vector<String> vecPath=new Vector<String>();
    //获取所有Json的绝对路径
    public Vector<String> getAllPath(String path) {
        File file = new File(path);
        File[] subFile = file.listFiles();
        //返回一个抽象路径名数组，这些路径名表示此抽象路径名表示的目录中的文件。

        for (int i = 0; i < subFile.length; i++) {
            if (subFile[i].isDirectory()) {
                getAllPath(subFile[i].getAbsolutePath());
            } else {
                vecPath.add(subFile[i].getAbsolutePath());
            }
        }
        return vecPath;
    }

}

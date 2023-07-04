package answercard;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;


/**
 * @author  answercard
 * @email lsw_demail@163.com
 */
public class SmartRecognitionHH {
	/**
	 * ������з���:  0=����
	 */
	static int TtoDir=0;
	/**
	 * ѡ�����з���:  0=���� 
	 */
	static int CtoDir=0;
    static {
    	System.out.println("Welcome to OpenCV " + Core.VERSION);
    	//String root=System.getProperty("user.dir");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
       // System.load("D:\\OpenCV\\opencv4.5\\build\\java\\x64\\opencv_java450.dll");
    }

    public static void main(String []args) {
        String sheet = "D:\\workspaces\\AnswerCard\\img\\A3.jpg";

        //A4 ��ֵ�����ͺ����ɵ�ͼƬ·��
        String results = "D:\\workspaces\\AnswerCard\\img\\resultHH.jpg";
        String msg = rowsAndCols(sheet, results);
        System.out.println(msg);
    }

    public static void Canny(String oriImg, String dstImg, int threshold) {
        //װ��ͼƬ
        Mat img = Imgcodecs.imread(oriImg);
        Mat srcImage2 = new Mat();
        Mat srcImage3 = new Mat();
        Mat srcImage4 = new Mat();
        Mat srcImage5 = new Mat();

        //ͼƬ��ɻҶ�ͼƬ
        Imgproc.cvtColor(img, srcImage2, Imgproc.COLOR_RGB2GRAY);
        //ͼƬ��ֵ��
        Imgproc.adaptiveThreshold(srcImage2, srcImage3, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 255, 1);
        //ȷ����ʴ�����ͺ˵Ĵ�С
        Mat element = Imgproc.getStructuringElement(MORPH_RECT, new Size(1, 6));
        //��ʴ����
        Imgproc.erode(srcImage3, srcImage4, element);
        //���Ͳ���
        Imgproc.dilate(srcImage4, srcImage5, element);
       // Imgcodecs.imwrite("D:\\workspaces\\AnswerCard\\img\\enresults.jpg", srcImage4);

        //ȷ��ÿ�Ŵ��⿨��ROI����--ע�⿪ʼ�ͽ�������
         Mat imag_ch1 = srcImage4.submat(new Rect(292,455,765, 145));
        //ʶ����������
        Vector<MatOfPoint> chapter1 = new Vector<>();
        Imgproc.findContours(imag_ch1, chapter1, new Mat(), 2, 3);
        Mat result = new Mat(imag_ch1.size(), CV_8U, new Scalar(255));
        Imgproc.drawContours(result, chapter1, -1, new Scalar(0), 2);

        Imgcodecs.imwrite("D:\\workspaces\\AnswerCard\\img\\resultHH.jpg", result);



        //newһ�� ���μ��� ����װ ������ע���ȱ���ɱ�
        List<RectComp> RectCompList = new ArrayList<>();
        for (int i = 0; i < chapter1.size(); i++) {
            Rect rm = Imgproc.boundingRect(chapter1.get(i));
            RectComp ti = new RectComp(rm);
            //��������������� 50 - 80 ��Χ�ڵ�����װ�����μ���

            if (ti.rm.width > 15 && ti.rm.width < 25) {
                RectCompList.add(ti);
                System.out.println(ti.rm.x+" "+ti.rm.y+" "+ti.rm.width+" "+ti.rm.height);
            }
        }

        //newһ�� map �����洢���⿨����Ĵ� (A\B\C\D)
        TreeMap<Integer, String> listenAnswer = new TreeMap<>();
        /**
         * �ر�ע����⿨ѡ��������Ҫ����X������
         * ѡ������ ����Ǻ���
         * ���
         */
      
    
    	  RectCompList.sort((o1, o2) -> {
    	  if (o1.rm.x > o2.rm.x) {
              return 1;
          }
          if (o1.rm.x == o2.rm.x) {
              return 0;
          }
          if (o1.rm.x < o2.rm.x) {
              return -1;
          }
          return 1;
    	  });
         System.out.println("=========");
      for(int i=0;i<RectCompList.size();i++)
      {
    	  RectComp ti = RectCompList.get(i);
    	  System.out.println(i+"= "+ti.rm.x+" "+ti.rm.y+" "+ti.rm.width+" "+ti.rm.height);
      }
            /*
            ������ȸߣ�����ͨ�����ؼ���
          for (RectComp rc : RectCompList) {
            int x = RectCompList.get(t).getRm().x - 16;
            int y = RectCompList.get(t).getRm().y - 94;

            //����x���ϵķָ� �������5�⣬��ô������һ����ָ�
            int xSplit = x/85 /5;
            //��Ϊ��һ�� x=21 ���������Ŀ��0��ʼ�㣬��ʵ�Ǵ�1��ʼ ����+1
            int xTitleNum = x/85 + 1;

            //���ھ�������  x��������ݼ�  �ݼ�����һ����ȥ �����������������ϣ���û����  ������⿨x��40������ �������
            if(x%85>20){
                System.out.println("x��ݼ��̶�" + x%85);
                xTitleNum++;
            }
            xTitleNum = xTitleNum - xSplit;
            System.out.println(xTitleNum);
            }
            */

      
        /**
         * ��ź���
         * ѡ�����
         * ������
         * (1)������ rows=15
         * (2)ÿ������: tn=3
         * (3)ÿ�������ta=4
         * (4)��ʼ��� sr=31 ������� er=81
         * (5)��ʼ����A:(x,y) ==��һ�е�һ�е����Ͻ�����
         * (6)    B:(x2,y2)==��һ�е�һ�е� ���½�����
         *      ͨ����������Ĵ�С����ÿһ��Ϳ�����Ŀ���
         * (7)    ��=x2-x
         * (8)    ��=y2-y
         * (9)     C:(x3,y3) ��һ�еڶ��е����Ͻ�����
         *    ͨ��BC����������Ϳ���ļ��
         * (10) stepX=x3-x2  
         * (11)     D:(x4,y4)�ڶ��еĿ�ʼ���Ͻ�����
         *      ͨ��B��D���������֮��ļ��
         * (12)     stepY=y4-y2
         *      E:(x5,y5)  ��ĩ�е����һ����Ϳ�������½�����
         *     ͨ��AE�����������Ϳ���ľ�ȷ��С����
         * (13) w=x5-x  h=y5-y  
         *   
         */
        int startX=0;
        int startY=2;
        int rows=5;//������
        int tnum=4;//ÿ�е���Ŀ��
        int ta=4;//�𰸸���
        int stn=1;//��ʼ���
        int etn=25;//�������
        int gapT=18;//��ѡ���ľ���
        int gapW=62;//�������д𰸼�ľ��루�����ڶ������Ų��֣�
        int gapH=12;//���д𰸼�ľ���
        int gapLine=0;
        int allW=880;
        int allH=567;
        int w=20;//Ϳ������Ŀ��
        int h=16;//Ϳ������ĸ߶�
        
        
        //���� X�� ȷ����ѡ��� (A B C D)--����ʱ�Ĵ�ʶ��
        
        if(TtoDir==0&&CtoDir==0)
        for (RectComp rc : RectCompList) {
        	  for (int c = 0; c < tnum; c++) {
                	for (int r = 0; r < rows; r++) {
                    	//����ÿ�������Ϳ����
	                        if (rc.rm.contains(new Point(startX + w/2, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum, "A");
	                        } 
	                        if (rc.rm.contains(new Point(startX  + w+w/2+gapT, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum, "B");
	                        } 
	                        if (rc.rm.contains(new Point(startX +2*w+w/2+2*gapT, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum, "C");
	                        } 
	                        if (rc.rm.contains(new Point(startX +3*w+w/2+3*gapT, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum, "D");
	                        }
	                        if (rc.rm.contains(new Point(startX +4*w+w/2+3*gapT,(gapH+h)*r+ startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum, "E");
	                        }
	                        if (rc.rm.contains(new Point(startX +5*w+w/2+3*gapT, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum, "F");
	                        }
                    }
                    for (int r = 0; r < rows; r++) {
                    	//����ÿ�������Ϳ����
	                        if (rc.rm.contains(new Point(startX + w/2+58+139, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+1, "A");
	                        } 
	                        if (rc.rm.contains(new Point(startX  + w+w/2+gapT+58+139, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+1, "B");
	                        } 
	                        if (rc.rm.contains(new Point(startX +2*w+w/2+2*gapT+58+139, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+1, "C");
	                        } 
	                        if (rc.rm.contains(new Point(startX +3*w+w/2+3*gapT+58+139, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+1, "D");
	                        }
	                        if (rc.rm.contains(new Point(startX +4*w+w/2+3*gapT+58+139,(gapH+h)*r+ startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+1, "E");
	                        }
	                        if (rc.rm.contains(new Point(startX +5*w+w/2+3*gapT+58+139, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+1, "F");
	                        }
                    }
                    //������
                    for (int r = 0; r < rows; r++) {
                    	//����ÿ�������Ϳ����
	                        if (rc.rm.contains(new Point(startX + w/2+431, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+2, "A");
	                        } 
	                        if (rc.rm.contains(new Point(startX  + w+w/2+gapT+431, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+2, "B");
	                        } 
	                        if (rc.rm.contains(new Point(startX +2*w+w/2+2*gapT+431, (gapH+h)*r+startY+h/2))) {
	                            String str=listenAnswer.get(stn+r*tnum+2);
	                           if(str!=null)
	                           {
	                           // listenAnswer.put(stn+r*tnum+2, str+":C");
	                            }else{
	                              listenAnswer.put(stn+r*tnum+2, "C");
	                            }
	                        } 
	                        if (rc.rm.contains(new Point(startX +3*w+w/2+3*gapT+431, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+2, "D");
	                        }
	                        if (rc.rm.contains(new Point(startX +4*w+w/2+3*gapT+431,(gapH+h)*r+ startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+2, "E");
	                        }
	                        if (rc.rm.contains(new Point(startX +5*w+w/2+3*gapT+431, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+2, "F");
	                        }
                    }
                     for (int r = 0; r < rows; r++) {
                    	//����ÿ�������Ϳ����
	                        if (rc.rm.contains(new Point(startX + w/2+626, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+3, "A");
	                        } 
	                        if (rc.rm.contains(new Point(startX  + w+w/2+gapT+626, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+3, "B");
	                        } 
	                        if (rc.rm.contains(new Point(startX +2*w+w/2+2*gapT+626, (gapH+h)*r+startY+h/2))) {
	                            String str=listenAnswer.get(stn+r*tnum+2);
	                           if(str!=null)
	                           {
	                             
	                          //  listenAnswer.put(stn+r*tnum+2, str+":C");
	                            }else{
	                              listenAnswer.put(stn+r*tnum+3, "C");
	                            }
	                        } 
	                        if (rc.rm.contains(new Point(startX +3*w+w/2+3*gapT+626, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+3, "D");
	                        }
	                        if (rc.rm.contains(new Point(startX +4*w+w/2+3*gapT+626,(gapH+h)*r+ startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+3, "E");
	                        }
	                        if (rc.rm.contains(new Point(startX +5*w+w/2+3*gapT+626, (gapH+h)*r+startY+h/2))) {
	                            listenAnswer.put(stn+r*tnum+3, "F");
	                        }
                    }
            }
        }
      
        //���ʶ��Ľ��
        Iterator iter = listenAnswer.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            System.out.println("��" + key + "��,����:" + val);
        }

    }

    public static String rowsAndCols(String oriImg, String dstImg) {
        String msg = "";

        Canny(oriImg, dstImg, 50);

        Mat mat = Imgcodecs.imread(dstImg);
        msg += "\n����:" + mat.rows();
        msg += "\n����:" + mat.cols();
        msg += "\nheight:" + mat.height();
        msg += "\nwidth:" + mat.width();
        msg += "\nelemSide:" + mat.elemSize();
        //CvType contourSeq = null;

        return msg;
    }
}
